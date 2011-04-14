/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.join.exec.ExecNode;
import com.espertech.esper.epl.join.plan.*;
import com.espertech.esper.epl.join.table.*;
import com.espertech.esper.epl.spec.OuterJoinDesc;
import com.espertech.esper.epl.spec.SelectClauseStreamSelectorEnum;
import com.espertech.esper.client.EventType;
import com.espertech.esper.type.OuterJoinType;
import com.espertech.esper.util.AuditPath;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.DependencyGraph;
import com.espertech.esper.view.HistoricalEventViewable;
import com.espertech.esper.view.Viewable;
import com.espertech.esper.core.StreamJoinAnalysisResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Factory for building a {@link JoinSetComposer} from analyzing filter nodes, for
 * fast join tuple result set composition.
 */
public class JoinSetComposerFactoryImpl implements JoinSetComposerFactory
{
    private static final Log queryPlanLog = LogFactory.getLog(AuditPath.QUERYPLAN_LOG);

    /**
     * Builds join tuple composer.
     * @param outerJoinDescList - list of descriptors for outer join criteria
     * @param optionalFilterNode - filter tree for analysis to build indexes for fast access
     * @param streamTypes - types of streams
     * @param streamNames - names of streams
     * @param streamViews - leaf view per stream
     * @param selectStreamSelectorEnum - indicator for rstream or istream-only, for optimization
     * @return composer implementation
     * @throws ExprValidationException is thrown to indicate that
     * validation of view use in joins failed.
     */
    public JoinSetComposer makeComposer(List<OuterJoinDesc> outerJoinDescList,
                                                   ExprNode optionalFilterNode,
                                                   EventType[] streamTypes,
                                                   String[] streamNames,
                                                   Viewable[] streamViews,
                                                   SelectClauseStreamSelectorEnum selectStreamSelectorEnum,
                                                   StreamJoinAnalysisResult streamJoinAnalysisResult,
                                                   ExprEvaluatorContext exprEvaluatorContext,
                                                   boolean queryPlanLogging)
            throws ExprValidationException
    {
        // Determine if there is a historical stream, and what dependencies exist
        DependencyGraph historicalDependencyGraph = new DependencyGraph(streamTypes.length);
        boolean[] isHistorical = new boolean[streamViews.length];
        boolean hasHistorical = false;
        for (int i = 0; i < streamViews.length; i++)
        {
            if (streamViews[i] instanceof HistoricalEventViewable)
            {
                HistoricalEventViewable historicalViewable = (HistoricalEventViewable) streamViews[i];
                isHistorical[i] = true;
                hasHistorical = true;
                historicalDependencyGraph.addDependency(i, historicalViewable.getRequiredStreams());
            }
        }

        EventTable[][] indexes;
        QueryStrategy[] queryStrategies;

        // Handle a join with a database or other historical data source for 2 streams
        if ((hasHistorical) && (streamViews.length == 2))
        {
            return makeComposerHistorical2Stream(outerJoinDescList, optionalFilterNode, streamTypes, streamViews, exprEvaluatorContext, queryPlanLogging);
        }

        // Query graph for graph relationships between streams/historicals
        // For outer joins the query graph will just contain outer join relationships
        QueryGraph queryGraph = new QueryGraph(streamTypes.length);
        if (!outerJoinDescList.isEmpty())
        {
            OuterJoinAnalyzer.analyze(outerJoinDescList, queryGraph);
            if (log.isDebugEnabled())
            {
                log.debug(".makeComposer After outer join queryGraph=\n" + queryGraph);
            }
        }
        else
        {
            // Let the query graph reflect the where-clause
            if (optionalFilterNode != null)
            {
                // Analyze relationships between streams using the optional filter expression.
                // Relationships are properties in AND and EQUALS nodes of joins.
                FilterExprAnalyzer.analyze(optionalFilterNode, queryGraph);
                if (log.isDebugEnabled())
                {
                    log.debug(".makeComposer After filter expression queryGraph=\n" + queryGraph);
                }

                // Add navigation entries based on key and index property equivalency (a=b, b=c follows a=c)
                QueryGraph.fillEquivalentNav(queryGraph);
                if (log.isDebugEnabled())
                {
                    log.debug(".makeComposer After fill equiv. nav. queryGraph=\n" + queryGraph);
                }
            }
        }

        // Historical index lists
        HistoricalStreamIndexList[] historicalStreamIndexLists = new HistoricalStreamIndexList[streamTypes.length];

        QueryPlan queryPlan = QueryPlanBuilder.getPlan(streamTypes, outerJoinDescList, queryGraph, streamNames,
                hasHistorical, isHistorical, historicalDependencyGraph, historicalStreamIndexLists, exprEvaluatorContext);
        if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
            queryPlanLog.info("Query plan: " + queryPlan);
        }

        // Build indexes
        QueryPlanIndex[] indexSpecs = queryPlan.getIndexSpecs();
        indexes = new EventTable[indexSpecs.length][];
        for (int streamNo = 0; streamNo < indexSpecs.length; streamNo++)
        {
            if (indexSpecs[streamNo] == null)
            {
                continue;
            }
            
            String[][] indexProps = indexSpecs[streamNo].getIndexProps();
            Class[][] coercionTypes = indexSpecs[streamNo].getCoercionTypesPerIndex();
            indexes[streamNo] = new EventTable[indexProps.length];
            for (int indexNo = 0; indexNo < indexProps.length; indexNo++)
            {
                indexes[streamNo][indexNo] = buildIndex(streamNo, indexProps[indexNo], coercionTypes[indexNo], streamTypes[streamNo]);
            }
        }

        // Build strategies
        QueryPlanNode[] queryExecSpecs = queryPlan.getExecNodeSpecs();
        queryStrategies = new QueryStrategy[queryExecSpecs.length];
        for (int i = 0; i < queryExecSpecs.length; i++)
        {
            QueryPlanNode planNode = queryExecSpecs[i];
            if (planNode == null)
            {
                log.debug(".makeComposer No execution node for stream " + i + " '" + streamNames[i] + "'");
                continue;
            }

            ExecNode executionNode = planNode.makeExec(indexes, streamTypes, streamViews, historicalStreamIndexLists);

            if (log.isDebugEnabled())
            {
                log.debug(".makeComposer Execution nodes for stream " + i + " '" + streamNames[i] +
                    "' : \n" + ExecNode.print(executionNode));
            }

            queryStrategies[i] = new ExecNodeQueryStrategy(i, streamTypes.length, executionNode);
        }

        // If this is not unidirectional and not a self-join (excluding self-outer-join)
        if ((!streamJoinAnalysisResult.isUnidirectional()) &&
            (!streamJoinAnalysisResult.isPureSelfJoin() || !outerJoinDescList.isEmpty()))
        {
            if (hasHistorical)
            {
                return new JoinSetComposerHistoricalImpl(indexes, queryStrategies, streamViews, exprEvaluatorContext);
            }
            else
            {
                return new JoinSetComposerImpl(indexes, queryStrategies, streamJoinAnalysisResult.isPureSelfJoin(), exprEvaluatorContext);
            }
        }
        else
        {
            QueryStrategy driver;
            int unidirectionalStream;
            if (streamJoinAnalysisResult.getUnidirectionalStreamNumber() != -1)
            {
                unidirectionalStream = streamJoinAnalysisResult.getUnidirectionalStreamNumber();
                driver = queryStrategies[unidirectionalStream];
            }
            else
            {
                unidirectionalStream = 0;
                driver = queryStrategies[0];
            }
            return new JoinSetComposerStreamToWinImpl(indexes, streamJoinAnalysisResult.isPureSelfJoin(),
                    unidirectionalStream, driver, streamJoinAnalysisResult.getUnidirectionalNonDriving());
        }
    }

    private JoinSetComposer makeComposerHistorical2Stream(List<OuterJoinDesc> outerJoinDescList,
                                                   ExprNode optionalFilterNode,
                                                   EventType[] streamTypes,
                                                   Viewable[] streamViews,
                                                   ExprEvaluatorContext exprEvaluatorContext,
                                                   boolean queryPlanLogging)
            throws ExprValidationException
    {
        QueryStrategy[] queryStrategies;

        // No tables for any streams
        queryStrategies = new QueryStrategy[streamTypes.length];

        int polledViewNum = 0;
        int streamViewNum = 1;
        if (streamViews[1] instanceof HistoricalEventViewable)
        {
            streamViewNum = 0;
            polledViewNum = 1;
        }

        // if all-historical join, check dependency
        boolean isAllHistoricalNoSubordinate = false;
        if ((streamViews[0] instanceof HistoricalEventViewable) && (streamViews[1] instanceof HistoricalEventViewable))
        {
            DependencyGraph graph = new DependencyGraph(2);
            graph.addDependency(0, ((HistoricalEventViewable) streamViews[0]).getRequiredStreams());
            graph.addDependency(1, ((HistoricalEventViewable) streamViews[1]).getRequiredStreams());
            if (graph.getFirstCircularDependency() != null)
            {
                throw new ExprValidationException("Circular dependency detected between historical streams");
            }

            // if both streams are independent
            if (graph.getRootNodes().size() == 2)
            {
                isAllHistoricalNoSubordinate = true; // No parameters used by either historical
            }
            else
            {
                if ((graph.getDependenciesForStream(0).size() == 0))
                {
                    streamViewNum = 0;
                    polledViewNum = 1;
                }
                else
                {
                    streamViewNum = 1;
                    polledViewNum = 0;
                }                
            }
        }

        // Build an outer join expression node
        boolean isOuterJoin = false;
        ExprNode outerJoinEqualsNode = null;
        if (!outerJoinDescList.isEmpty())
        {
            OuterJoinDesc outerJoinDesc = outerJoinDescList.get(0);
            if (outerJoinDesc.getOuterJoinType().equals(OuterJoinType.FULL))
            {
                isOuterJoin = true;
            }
            else if ((outerJoinDesc.getOuterJoinType().equals(OuterJoinType.LEFT)) &&
                    (streamViewNum == 0))
            {
                    isOuterJoin = true;
            }
            else if ((outerJoinDesc.getOuterJoinType().equals(OuterJoinType.RIGHT)) &&
                    (streamViewNum == 1))
            {
                    isOuterJoin = true;
            }

            outerJoinEqualsNode  = outerJoinDesc.makeExprNode(exprEvaluatorContext);
        }

        // Determine filter for indexing purposes
        ExprNode filterForIndexing = null;
        if ((outerJoinEqualsNode != null) && (optionalFilterNode != null))  // both filter and outer join, add
        {
            filterForIndexing = new ExprAndNode();
            filterForIndexing.addChildNode(optionalFilterNode);
            filterForIndexing.addChildNode(outerJoinEqualsNode);
        }
        else if ((outerJoinEqualsNode == null) && (optionalFilterNode != null))
        {
            filterForIndexing = optionalFilterNode;
        }
        else if (outerJoinEqualsNode != null)
        {
            filterForIndexing = outerJoinEqualsNode;
        }

        Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy> indexStrategies =
                determineIndexing(filterForIndexing, streamTypes[polledViewNum], streamTypes[streamViewNum], polledViewNum, streamViewNum);

        HistoricalEventViewable viewable = (HistoricalEventViewable) streamViews[polledViewNum];
        ExprEvaluator outerJoinEqualsNodeEval = outerJoinEqualsNode == null ? null : outerJoinEqualsNode.getExprEvaluator();
        queryStrategies[streamViewNum] = new HistoricalDataQueryStrategy(streamViewNum, polledViewNum, viewable, isOuterJoin, outerJoinEqualsNodeEval,
                indexStrategies.getFirst(), indexStrategies.getSecond());

        // for strictly historical joins, create a query strategy for the non-subordinate historical view
        if (isAllHistoricalNoSubordinate)
        {
            isOuterJoin = false;
            if (!outerJoinDescList.isEmpty())
            {
                OuterJoinDesc outerJoinDesc = outerJoinDescList.get(0);
                if (outerJoinDesc.getOuterJoinType().equals(OuterJoinType.FULL))
                {
                    isOuterJoin = true;
                }
                else if ((outerJoinDesc.getOuterJoinType().equals(OuterJoinType.LEFT)) &&
                        (polledViewNum == 0))
                {
                        isOuterJoin = true;
                }
                else if ((outerJoinDesc.getOuterJoinType().equals(OuterJoinType.RIGHT)) &&
                        (polledViewNum == 1))
                {
                        isOuterJoin = true;
                }
            }

            viewable = (HistoricalEventViewable) streamViews[streamViewNum];
            queryStrategies[polledViewNum] = new HistoricalDataQueryStrategy(polledViewNum, streamViewNum, viewable, isOuterJoin, outerJoinEqualsNodeEval,
                    new HistoricalIndexLookupStrategyNoIndex(), new PollResultIndexingStrategyNoIndex());
        }

        return new JoinSetComposerHistoricalImpl(null, queryStrategies, streamViews, exprEvaluatorContext);
    }

    private static Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy> determineIndexing(ExprNode filterForIndexing,
                                                                                              EventType polledViewType,
                                                                                              EventType streamViewType,
                                                                                              int polledViewStreamNum,
                                                                                              int streamViewStreamNum)
    {
        // No filter means unindexed event tables
        if (filterForIndexing == null)
        {
            return new Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy>(
                            new HistoricalIndexLookupStrategyNoIndex(), new PollResultIndexingStrategyNoIndex());
        }

        // analyze query graph; Whereas stream0=named window, stream1=delete-expr filter
        QueryGraph queryGraph = new QueryGraph(2);
        FilterExprAnalyzer.analyze(filterForIndexing, queryGraph);

        return determineIndexing(queryGraph, polledViewType, streamViewType, polledViewStreamNum, streamViewStreamNum);
    }

    /**
     * Constructs indexing and lookup strategy for a given relationship that a historical stream may have with another
     * stream (historical or not) that looks up into results of a poll of a historical stream.
     * <p>
     * The term "polled" refers to the assumed-historical stream.
     * @param queryGraph relationship representation of where-clause filter and outer join on-expressions
     * @param polledViewType the event type of the historical that is indexed
     * @param streamViewType the event type of the stream looking up in indexes
     * @param polledViewStreamNum the stream number of the historical that is indexed
     * @param streamViewStreamNum the stream number of the historical that is looking up
     * @return indexing and lookup strategy pair
     */
    public static Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy> determineIndexing(QueryGraph queryGraph,
                                                                                                  EventType polledViewType,
                                                                                                  EventType streamViewType,
                                                                                                  int polledViewStreamNum,
                                                                                                  int streamViewStreamNum)
        {
        // index and key property names
        String[] keyPropertiesJoin = queryGraph.getKeyProperties(streamViewStreamNum, polledViewStreamNum);
        String[] indexPropertiesJoin = queryGraph.getIndexProperties(streamViewStreamNum, polledViewStreamNum);

        // If the analysis revealed no join columns, must use the brute-force full table scan
        if ((keyPropertiesJoin == null) || (keyPropertiesJoin.length == 0))
        {
            return new Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy>(
                            new HistoricalIndexLookupStrategyNoIndex(), new PollResultIndexingStrategyNoIndex());
        }

        // Build a set of index descriptors with property name and coercion type
        boolean mustCoerce = false;
        Class[] coercionTypes = new Class[indexPropertiesJoin.length];
        for (int i = 0; i < keyPropertiesJoin.length; i++)
        {
            Class keyPropType = JavaClassHelper.getBoxedType(streamViewType.getPropertyType(keyPropertiesJoin[i]));
            Class indexedPropType = JavaClassHelper.getBoxedType(polledViewType.getPropertyType(indexPropertiesJoin[i]));
            Class coercionType = indexedPropType;
            if (keyPropType != indexedPropType)
            {
                coercionType = JavaClassHelper.getCompareToCoercionType(keyPropType, keyPropType);
                mustCoerce = true;
            }

            coercionTypes[i] = coercionType;
        }

        // No coercion
        if (!mustCoerce)
        {
            PollResultIndexingStrategyIndex indexing = new PollResultIndexingStrategyIndex(polledViewStreamNum, polledViewType, indexPropertiesJoin);
            HistoricalIndexLookupStrategy strategy = new HistoricalIndexLookupStrategyIndex(streamViewType, keyPropertiesJoin);
            return new Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy>(strategy, indexing);
        }

        // With coercion, same lookup strategy as the index coerces
        PollResultIndexingStrategy indexing = new PollResultIndexingStrategyIndexCoerce(polledViewStreamNum, polledViewType, indexPropertiesJoin, coercionTypes);
        HistoricalIndexLookupStrategy strategy = new HistoricalIndexLookupStrategyIndex(streamViewType, keyPropertiesJoin);
        return new Pair<HistoricalIndexLookupStrategy, PollResultIndexingStrategy>(strategy, indexing);
    }

    /**
     * Build an index/table instance using the event properties for the event type.
     * @param indexedStreamNum - number of stream indexed
     * @param indexProps - properties to index
     * @param optCoercionTypes - optional array of coercion types, or null if no coercion is required
     * @param eventType - type of event to expect
     * @return table build
     */
    protected static EventTable buildIndex(int indexedStreamNum, String[] indexProps, Class[] optCoercionTypes, EventType eventType)
    {
        EventTable table;
        if (indexProps.length == 0)
        {
            table = new UnindexedEventTable(indexedStreamNum);
        }
        else
        {
            if (optCoercionTypes == null)
            {
                table = new PropertyIndexedEventTable(indexedStreamNum, eventType, indexProps, null);
            }
            else
            {
                table = new PropertyIndTableCoerceAll(indexedStreamNum, eventType, indexProps, optCoercionTypes);
            }

        }
        return table;
    }

    private static final Log log = LogFactory.getLog(JoinSetComposerFactoryImpl.class);
}

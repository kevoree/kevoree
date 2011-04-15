/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.agg.AggregationServiceFactory;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.event.NativeEventType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Factory for output processors. Output processors process the result set of a join or of a view
 * and apply aggregation/grouping, having and some output limiting logic.
 * <p>
 * The instance produced by the factory depends on the presence of aggregation functions in the select list,
 * the presence and nature of the group-by clause.
 * <p>
 * In case (1) and (2) there are no aggregation functions in the select clause.
 * <p>
 * Case (3) is without group-by and with aggregation functions and without non-aggregated properties
 * in the select list: <pre>select sum(volume) </pre>.
 * Always produces one row for new and old data, aggregates without grouping.
 * <p>
 * Case (4) is without group-by and with aggregation functions but with non-aggregated properties
 * in the select list: <pre>select price, sum(volume) </pre>.
 * Produces a row for each event, aggregates without grouping.
 * <p>
 * Case (5) is with group-by and with aggregation functions and all selected properties are grouped-by.
 * in the select list: <pre>select customerId, sum(volume) group by customerId</pre>.
 * Produces a old and new data row for each group changed, aggregates with grouping, see
 * {@link ResultSetProcessorRowPerGroup}
 * <p>
 * Case (6) is with group-by and with aggregation functions and only some selected properties are grouped-by.
 * in the select list: <pre>select customerId, supplierId, sum(volume) group by customerId</pre>.
 * Produces row for each event, aggregates with grouping.
 */
public class ResultSetProcessorFactory
{
    /**
     * Returns the result set process for the given select expression, group-by clause and
     * having clause given a set of types describing each stream in the from-clause.
     * @param statementSpecCompiled - the statement specification
     * @param stmtContext - engine and statement level services
     * @param typeService - for information about the streams in the from clause
     * @param viewResourceDelegate - delegates views resource factory to expression resources requirements
     * @param isUnidirectionalStream - true if unidirectional join for any of the streams
     * @param allowAggregation - indicator whether to allow aggregation functions in any expressions
     * @return result set processor instance
     * @throws ExprValidationException when any of the expressions is invalid
     */
    public static ResultSetProcessor getProcessor(StatementSpecCompiled statementSpecCompiled,
                                                  StatementContext stmtContext,
                                                  StreamTypeService typeService,
                                                  ViewResourceDelegate viewResourceDelegate,
                                                  boolean[] isUnidirectionalStream,
                                                  boolean allowAggregation
    )
            throws ExprValidationException
    {
        SelectClauseSpecCompiled selectClauseSpec = statementSpecCompiled.getSelectClauseSpec();
        InsertIntoDesc insertIntoDesc = statementSpecCompiled.getInsertIntoDesc();
        List<ExprNode> groupByNodes = statementSpecCompiled.getGroupByExpressions();
        ExprNode optionalHavingNode = statementSpecCompiled.getHavingExprRootNode();
        OutputLimitSpec outputLimitSpec = statementSpecCompiled.getOutputLimitSpec();
        List<OrderByItem> orderByList = statementSpecCompiled.getOrderByList();

        if (log.isDebugEnabled())
        {
            log.debug(".getProcessor Getting processor for " +
                    " selectionList=" + selectClauseSpec.getSelectExprList() +
                    " groupByNodes=" + Arrays.toString(groupByNodes.toArray()) +
                    " optionalHavingNode=" + optionalHavingNode);
        }

        boolean isUnidirectional = false;
        for (int i = 0; i < isUnidirectionalStream.length; i++)
        {
            isUnidirectional |= isUnidirectionalStream[i];
        }

        // Expand any instances of select-clause names in the
        // order-by clause with the full expression
        expandColumnNames(selectClauseSpec.getSelectExprList(), orderByList);

        // Validate selection expressions, if any (could be wildcard i.e. empty list)
        List<SelectClauseExprCompiledSpec> namedSelectionList = new LinkedList<SelectClauseExprCompiledSpec>();
        for (int i = 0; i < selectClauseSpec.getSelectExprList().size(); i++)
        {
            // validate element
            SelectClauseElementCompiled element = selectClauseSpec.getSelectExprList().get(i);
            if (element instanceof SelectClauseExprCompiledSpec)
            {
                SelectClauseExprCompiledSpec expr = (SelectClauseExprCompiledSpec) element;
                ExprNode validatedExpression = expr.getSelectExpression().getValidatedSubtree(typeService, stmtContext.getMethodResolutionService(), viewResourceDelegate, stmtContext.getSchedulingService(), stmtContext.getVariableService(),stmtContext);

                // determine an element name if none assigned
                String asName = expr.getAssignedName();
                if (asName == null)
                {
                    asName = validatedExpression.toExpressionString();
                }

                expr.setAssignedName(asName);
                expr.setSelectExpression(validatedExpression);
                namedSelectionList.add(expr);
            }
        }
        boolean isUsingWildcard = selectClauseSpec.isUsingWildcard();

        // Validate stream selections, if any (such as stream.*)
        boolean isUsingStreamSelect = false;
        for (SelectClauseElementCompiled compiled : selectClauseSpec.getSelectExprList())
        {
            if (!(compiled instanceof SelectClauseStreamCompiledSpec))
            {
                continue;
            }
            SelectClauseStreamCompiledSpec streamSelectSpec = (SelectClauseStreamCompiledSpec) compiled;
            int streamNum = Integer.MIN_VALUE;
            boolean isFragmentEvent = false;
            boolean isProperty = false;
            Class propertyType = null;
            isUsingStreamSelect = true;
            for (int i = 0; i < typeService.getStreamNames().length; i++)
            {
                String streamName = streamSelectSpec.getStreamName();
                if (typeService.getStreamNames()[i].equals(streamName))
                {
                    streamNum = i;
                    break;
                }

                // see if the stream name is known as a nested event type
                EventType candidateProviderOfFragments = typeService.getEventTypes()[i];
                // for the native event type we don't need to fragment, we simply use the property itself since all wrappers understand Java objects
                if (!(candidateProviderOfFragments instanceof NativeEventType) && (candidateProviderOfFragments.getFragmentType(streamName) != null))
                {
                    streamNum = i;
                    isFragmentEvent = true;
                    break;
                }
            }

            // stream name not found
            if (streamNum == Integer.MIN_VALUE)
            {
                // see if the stream name specified resolves as a property
                PropertyResolutionDescriptor desc = null;
                try
                {
                    desc = typeService.resolveByPropertyName(streamSelectSpec.getStreamName());
                }
                catch (StreamTypesException e)
                {
                    // not handled
                }

                if (desc == null)
                {
                    throw new ExprValidationException("Stream selector '" + streamSelectSpec.getStreamName() + ".*' does not match any stream name in the from clause");
                }
                isProperty = true;
                propertyType = desc.getPropertyType();
                streamNum = desc.getStreamNum();
            }

            streamSelectSpec.setStreamNumber(streamNum);
            streamSelectSpec.setFragmentEvent(isFragmentEvent);
            streamSelectSpec.setProperty(isProperty, propertyType);
        }

        // Validate group-by expressions, if any (could be empty list for no group-by)
        Class[] groupByTypes = new Class[groupByNodes.size()];
        for (int i = 0; i < groupByNodes.size(); i++)
        {
            // Ensure there is no subselects
            ExprNodeSubselectVisitor visitor = new ExprNodeSubselectVisitor();
            groupByNodes.get(i).accept(visitor);
            if (visitor.getSubselects().size() > 0)
            {
                throw new ExprValidationException("Subselects not allowed within group-by");
            }

            ExprNode validatedGroupBy = groupByNodes.get(i).getValidatedSubtree(typeService, stmtContext.getMethodResolutionService(), viewResourceDelegate, stmtContext.getSchedulingService(), stmtContext.getVariableService(),stmtContext);
            groupByNodes.set(i, validatedGroupBy);
            groupByTypes[i] = validatedGroupBy.getExprEvaluator().getType();
        }
        stmtContext.getMethodResolutionService().setGroupKeyTypes(groupByTypes);

        // Validate having clause, if present
        if (optionalHavingNode != null)
        {
            // Ensure there is no subselects
            ExprNodeSubselectVisitor visitor = new ExprNodeSubselectVisitor();
            optionalHavingNode.accept(visitor);
            if (visitor.getSubselects().size() > 0)
            {
                throw new ExprValidationException("Subselects not allowed within having-clause");
            }

            optionalHavingNode = optionalHavingNode.getValidatedSubtree(typeService, stmtContext.getMethodResolutionService(), viewResourceDelegate, stmtContext.getSchedulingService(), stmtContext.getVariableService(), stmtContext);
        }

        // Validate order-by expressions, if any (could be empty list for no order-by)
        for (int i = 0; i < orderByList.size(); i++)
        {
        	ExprNode orderByNode = orderByList.get(i).getExprNode();

            // Ensure there is no subselects
            ExprNodeSubselectVisitor visitor = new ExprNodeSubselectVisitor();
            orderByNode.accept(visitor);
            if (visitor.getSubselects().size() > 0)
            {
                throw new ExprValidationException("Subselects not allowed within order-by clause");
            }

            Boolean isDescending = orderByList.get(i).isDescending();
        	OrderByItem validatedOrderBy = new OrderByItem(orderByNode.getValidatedSubtree(typeService, stmtContext.getMethodResolutionService(), viewResourceDelegate, stmtContext.getSchedulingService(), stmtContext.getVariableService(), stmtContext), isDescending);
        	orderByList.set(i, validatedOrderBy);
        }

        // Get the select expression nodes
        List<ExprNode> selectNodes = new ArrayList<ExprNode>();
        for(SelectClauseExprCompiledSpec element : namedSelectionList)
        {
        	selectNodes.add(element.getSelectExpression());
        }

        // Get the order-by expression nodes
        List<ExprNode> orderByNodes = new ArrayList<ExprNode>();
        for(OrderByItem element : orderByList)
        {
        	orderByNodes.add(element.getExprNode());
        }

        // Determine aggregate functions used in select, if any
        List<ExprAggregateNode> selectAggregateExprNodes = new LinkedList<ExprAggregateNode>();
        for (SelectClauseExprCompiledSpec element : namedSelectionList)
        {
            ExprAggregateNode.getAggregatesBottomUp(element.getSelectExpression(), selectAggregateExprNodes);
        }
        if (!allowAggregation && !selectAggregateExprNodes.isEmpty())
        {
            throw new ExprValidationException("Aggregation functions are not allowed in this context");
        }

        // Determine if we have a having clause with aggregation
        List<ExprAggregateNode> havingAggregateExprNodes = new LinkedList<ExprAggregateNode>();
        Set<Pair<Integer, String>> propertiesAggregatedHaving = new HashSet<Pair<Integer, String>>();
        if (optionalHavingNode != null)
        {
            ExprAggregateNode.getAggregatesBottomUp(optionalHavingNode, havingAggregateExprNodes);
            propertiesAggregatedHaving = ExprNodeUtility.getAggregatedProperties(havingAggregateExprNodes);
        }
        if (!allowAggregation && !havingAggregateExprNodes.isEmpty())
        {
            throw new ExprValidationException("Aggregation functions are not allowed in this context");
        }

        // Determine if we have a order-by clause with aggregation
        List<ExprAggregateNode> orderByAggregateExprNodes = new LinkedList<ExprAggregateNode>();
        if (orderByNodes != null)
        {
            for (ExprNode orderByNode : orderByNodes)
            {
                ExprAggregateNode.getAggregatesBottomUp(orderByNode, orderByAggregateExprNodes);
            }
            if (!allowAggregation && !orderByAggregateExprNodes.isEmpty())
            {
                throw new ExprValidationException("Aggregation functions are not allowed in this context");
            }
        }

        // Construct the appropriate aggregation service
        boolean hasGroupBy = !groupByNodes.isEmpty();
        AggregationService aggregationService = AggregationServiceFactory.getService(selectAggregateExprNodes, havingAggregateExprNodes, orderByAggregateExprNodes, hasGroupBy, stmtContext.getMethodResolutionService(), stmtContext, statementSpecCompiled.getAnnotations(), stmtContext.getVariableService(), stmtContext.getStatementStopService(), typeService.getEventTypes().length > 1,
                statementSpecCompiled.getFilterRootNode(), statementSpecCompiled.getHavingExprRootNode());

        boolean useCollatorSort = false;
        if (stmtContext.getConfigSnapshot() != null)
        {
            useCollatorSort = stmtContext.getConfigSnapshot().getEngineDefaults().getLanguage().isSortUsingCollator();
        }

        // Construct the processor for sorting output events
        OrderByProcessor orderByProcessor = OrderByProcessorFactory.getProcessor(namedSelectionList,
                groupByNodes, orderByList, aggregationService, statementSpecCompiled.getRowLimitSpec(), stmtContext.getVariableService(),useCollatorSort);

        // Construct the processor for evaluating the select clause
        SelectExprEventTypeRegistry selectExprEventTypeRegistry = new SelectExprEventTypeRegistry(stmtContext.getDynamicReferenceEventTypes());
        SelectExprProcessor selectExprProcessor = SelectExprProcessorFactory.getProcessor(selectClauseSpec.getSelectExprList(), isUsingWildcard, insertIntoDesc, statementSpecCompiled.getForClauseSpec(), typeService, stmtContext.getEventAdapterService(), stmtContext.getStatementResultService(), stmtContext.getValueAddEventService(), selectExprEventTypeRegistry, stmtContext.getMethodResolutionService(), stmtContext,
                stmtContext.getVariableService(), stmtContext.getTimeProvider(), stmtContext.getEngineURI(), stmtContext.getStatementId());

        // Get a list of event properties being aggregated in the select clause, if any
        Set<Pair<Integer, String>> propertiesGroupBy = getGroupByProperties(groupByNodes);
        // Figure out all non-aggregated event properties in the select clause (props not under a sum/avg/max aggregation node)
        Set<Pair<Integer, String>> nonAggregatedProps = ExprNodeUtility.getNonAggregatedProps(selectNodes);

        // Validate that group-by is filled with sensible nodes (identifiers, and not part of aggregates selected, no aggregates)
        validateGroupBy(groupByNodes);

        // Validate the having-clause (selected aggregate nodes and all in group-by are allowed)
        if (optionalHavingNode != null)
        {
            validateHaving(propertiesGroupBy, optionalHavingNode);
        }

        // We only generate Remove-Stream events if they are explicitly selected, or the insert-into requires them
        boolean isSelectRStream = (statementSpecCompiled.getSelectStreamSelectorEnum() == SelectClauseStreamSelectorEnum.RSTREAM_ISTREAM_BOTH
                || statementSpecCompiled.getSelectStreamSelectorEnum() == SelectClauseStreamSelectorEnum.RSTREAM_ONLY);
        if ((statementSpecCompiled.getInsertIntoDesc() != null) && (!statementSpecCompiled.getInsertIntoDesc().isIStream()))
        {
            isSelectRStream = true;
        }

        // Determine if any output rate limiting must be performed early while processing results
        boolean isOutputLimiting = outputLimitSpec != null;
        if ((outputLimitSpec != null) && outputLimitSpec.getDisplayLimit() == OutputLimitLimitType.SNAPSHOT)
        {
            isOutputLimiting = false;   // Snapshot output does not count in terms of limiting output for grouping/aggregation purposes
        }

        ExprEvaluator optionHavingEval = optionalHavingNode == null ? null : optionalHavingNode.getExprEvaluator();

        // (1)
        // There is no group-by clause and no aggregate functions with event properties in the select clause and having clause (simplest case)
        if ((groupByNodes.isEmpty()) && (selectAggregateExprNodes.isEmpty()) && (havingAggregateExprNodes.isEmpty()))
        {
            // (1a)
            // There is no need to perform select expression processing, the single view itself (no join) generates
            // events in the desired format, therefore there is no output processor. There are no order-by expressions.
            if (orderByNodes.isEmpty() && optionalHavingNode == null && !isOutputLimiting && statementSpecCompiled.getRowLimitSpec() == null)
            {
                log.debug(".getProcessor Using no result processor");
                return new ResultSetProcessorHandThrough(selectExprProcessor, isSelectRStream);
            }

            // (1b)
            // We need to process the select expression in a simple fashion, with each event (old and new)
            // directly generating one row, and no need to update aggregate state since there is no aggregate function.
            // There might be some order-by expressions.
            log.debug(".getProcessor Using ResultSetProcessorSimple");
            return new ResultSetProcessorSimple(selectExprProcessor, orderByProcessor, optionHavingEval, isSelectRStream, stmtContext);
        }

        // (2)
        // A wildcard select-clause has been specified and the group-by is ignored since no aggregation functions are used, and no having clause
        boolean isLast = statementSpecCompiled.getOutputLimitSpec() != null && statementSpecCompiled.getOutputLimitSpec().getDisplayLimit() == OutputLimitLimitType.LAST;
        if ((namedSelectionList.isEmpty()) && (propertiesAggregatedHaving.isEmpty()) && (havingAggregateExprNodes.isEmpty()) && (!isLast))
        {
            log.debug(".getProcessor Using ResultSetProcessorSimple");
            return new ResultSetProcessorSimple(selectExprProcessor, orderByProcessor, optionHavingEval, isSelectRStream, stmtContext);
        }

        boolean hasAggregation = (!selectAggregateExprNodes.isEmpty()) || (!havingAggregateExprNodes.isEmpty()) || (!orderByAggregateExprNodes.isEmpty()) || (!propertiesAggregatedHaving.isEmpty());
        if ((groupByNodes.isEmpty()) && hasAggregation)
        {
            // (3)
            // There is no group-by clause and there are aggregate functions with event properties in the select clause (aggregation case)
            // or having class, and all event properties are aggregated (all properties are under aggregation functions).
            if ((nonAggregatedProps.isEmpty()) && (!isUsingWildcard) && (!isUsingStreamSelect))
            {
                log.debug(".getProcessor Using ResultSetProcessorRowForAll");
                return new ResultSetProcessorRowForAll(selectExprProcessor, aggregationService, orderByProcessor, optionHavingEval, isSelectRStream, isUnidirectional, stmtContext);
            }

            // (4)
            // There is no group-by clause but there are aggregate functions with event properties in the select clause (aggregation case)
            // or having clause and not all event properties are aggregated (some properties are not under aggregation functions).
            log.debug(".getProcessor Using ResultSetProcessorAggregateAll");
            return new ResultSetProcessorAggregateAll(selectExprProcessor, orderByProcessor, aggregationService, optionHavingEval, isSelectRStream, isUnidirectional, stmtContext);
        }

        // Handle group-by cases
        if (groupByNodes.isEmpty())
        {
            throw new IllegalStateException("Unexpected empty group-by expression list");
        }

        // Figure out if all non-aggregated event properties in the select clause are listed in the group by
        Set<Pair<Integer, String>> nonAggregatedPropsSelect = ExprNodeUtility.getNonAggregatedProps(selectNodes);
        boolean allInGroupBy = true;
        if (isUsingStreamSelect) {
            allInGroupBy = false;
        }
        for (Pair<Integer, String> nonAggregatedProp : nonAggregatedPropsSelect)
        {
            if (!propertiesGroupBy.contains(nonAggregatedProp))
            {
                allInGroupBy = false;
            }
        }

        // Wildcard select-clause means we do not have all selected properties in the group
        if (isUsingWildcard)
        {
            allInGroupBy = false;
        }

        // Figure out if all non-aggregated event properties in the order-by clause are listed in the select expression
        Set<Pair<Integer, String>> nonAggregatedPropsOrderBy = ExprNodeUtility.getNonAggregatedProps(orderByNodes);

        boolean allInSelect = true;
        for (Pair<Integer, String> nonAggregatedProp : nonAggregatedPropsOrderBy)
        {
            if (!nonAggregatedPropsSelect.contains(nonAggregatedProp))
            {
                allInSelect = false;
            }
        }

        // Wildcard select-clause means that all order-by props in the select expression
        if (isUsingWildcard)
        {
            allInSelect = true;
        }

        // (4)
        // There is a group-by clause, and all event properties in the select clause that are not under an aggregation
        // function are listed in the group-by clause, and if there is an order-by clause, all non-aggregated properties
        // referred to in the order-by clause also appear in the select (output one row per group, not one row per event)
        ExprEvaluator[] groupByEval = ExprNodeUtility.getEvaluators(groupByNodes);
        if (allInGroupBy && allInSelect)
        {
            log.debug(".getProcessor Using ResultSetProcessorRowPerGroup");
            return new ResultSetProcessorRowPerGroup(selectExprProcessor, orderByProcessor, aggregationService, groupByEval, optionHavingEval, isSelectRStream, isUnidirectional, stmtContext, outputLimitSpec);
        }

        // (6)
        // There is a group-by clause, and one or more event properties in the select clause that are not under an aggregation
        // function are not listed in the group-by clause (output one row per event, not one row per group)
        log.debug(".getProcessor Using ResultSetProcessorAggregateGrouped");
        return new ResultSetProcessorAggregateGrouped(selectExprProcessor, orderByProcessor, aggregationService, groupByEval, optionHavingEval, isSelectRStream, isUnidirectional, stmtContext, outputLimitSpec);
    }

    private static void validateHaving(Set<Pair<Integer, String>> propertiesGroupedBy,
                                       ExprNode havingNode)
        throws ExprValidationException
    {
        List<ExprAggregateNode> aggregateNodesHaving = new LinkedList<ExprAggregateNode>();
        if (aggregateNodesHaving != null)
        {
            ExprAggregateNode.getAggregatesBottomUp(havingNode, aggregateNodesHaving);
        }

        // Any non-aggregated properties must occur in the group-by clause (if there is one)
        if (!propertiesGroupedBy.isEmpty())
        {
            ExprNodeIdentifierVisitor visitor = new ExprNodeIdentifierVisitor(true);
            havingNode.accept(visitor);
            List<Pair<Integer, String>> allPropertiesHaving = visitor.getExprProperties();
            Set<Pair<Integer, String>> aggPropertiesHaving = ExprNodeUtility.getAggregatedProperties(aggregateNodesHaving);
            allPropertiesHaving.removeAll(aggPropertiesHaving);
            allPropertiesHaving.removeAll(propertiesGroupedBy);

            if (!allPropertiesHaving.isEmpty())
            {
                String name = allPropertiesHaving.iterator().next().getSecond();
                throw new ExprValidationException("Non-aggregated property '" + name + "' in the HAVING clause must occur in the group-by clause");
            }
        }
    }

    private static void validateGroupBy(List<ExprNode> groupByNodes)
        throws ExprValidationException
    {
        // Make sure there is no aggregate function in group-by
        List<ExprAggregateNode> aggNodes = new LinkedList<ExprAggregateNode>();
        for (ExprNode groupByNode : groupByNodes)
        {
            ExprAggregateNode.getAggregatesBottomUp(groupByNode, aggNodes);
            if (!aggNodes.isEmpty())
            {
                throw new ExprValidationException("Group-by expressions cannot contain aggregate functions");
            }
        }
    }

    private static Set<Pair<Integer, String>> getGroupByProperties(List<ExprNode> groupByNodes)
        throws ExprValidationException
    {
        // Get the set of properties refered to by all group-by expression nodes.
        Set<Pair<Integer, String>> propertiesGroupBy = new HashSet<Pair<Integer, String>>();

        for (ExprNode groupByNode : groupByNodes)
        {
            ExprNodeIdentifierVisitor visitor = new ExprNodeIdentifierVisitor(true);
            groupByNode.accept(visitor);
            List<Pair<Integer, String>> propertiesNode = visitor.getExprProperties();
            propertiesGroupBy.addAll(propertiesNode);

            // For each group-by expression node, require at least one property.
            if (propertiesNode.isEmpty())
            {
                throw new ExprValidationException("Group-by expressions must refer to property names");
            }
        }

        return propertiesGroupBy;
    }

    private static void expandColumnNames(List<SelectClauseElementCompiled> selectionList, List<OrderByItem> orderByList)
    {
    	for(SelectClauseElementCompiled selectElement : selectionList)
    	{
            // process only expressions
            if (!(selectElement instanceof SelectClauseExprCompiledSpec))
            {
                continue;
            }
            SelectClauseExprCompiledSpec selectExpr = (SelectClauseExprCompiledSpec) selectElement;

            String name = selectExpr.getAssignedName();
    		if(name != null)
    		{
    			ExprNode fullExpr = selectExpr.getSelectExpression();
    			for(ListIterator<OrderByItem> iterator = orderByList.listIterator(); iterator.hasNext(); )
    			{
    				OrderByItem orderByElement = iterator.next();
    				ExprNode swapped = ColumnNamedNodeSwapper.swap(orderByElement.getExprNode(), name, fullExpr);
    				OrderByItem newOrderByElement = new OrderByItem(swapped, orderByElement.isDescending());
    				iterator.set(newOrderByElement);
    			}
    		}
    	}
    }

    private static final Log log = LogFactory.getLog(ResultSetProcessorFactory.class);
}

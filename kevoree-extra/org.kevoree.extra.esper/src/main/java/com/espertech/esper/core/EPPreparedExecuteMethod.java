/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.core.ResultSetProcessor;
import com.espertech.esper.epl.core.ResultSetProcessorFactory;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.StreamTypeServiceImpl;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.join.JoinSetComposer;
import com.espertech.esper.epl.join.JoinSetFilter;
import com.espertech.esper.epl.named.NamedWindowProcessor;
import com.espertech.esper.epl.spec.NamedWindowConsumerStreamSpec;
import com.espertech.esper.epl.spec.SelectClauseStreamSelectorEnum;
import com.espertech.esper.epl.spec.StatementSpecCompiled;
import com.espertech.esper.epl.spec.StreamSpecCompiled;
import com.espertech.esper.event.EventBeanReader;
import com.espertech.esper.event.EventBeanReaderDefaultImpl;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.filter.FilterSpecCompiler;
import com.espertech.esper.util.AuditPath;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Starts and provides the stop method for EPL statements.
 */
public class EPPreparedExecuteMethod
{
    private static final Log queryPlanLog = LogFactory.getLog(AuditPath.QUERYPLAN_LOG);
    private static final Log log = LogFactory.getLog(EPPreparedExecuteMethod.class);

    private final StatementSpecCompiled statementSpec;
    private final ResultSetProcessor resultSetProcessor;
    private final NamedWindowProcessor[] processors;
    private final JoinSetComposer joinComposer;
    private final JoinSetFilter joinFilter;
    private final ExprEvaluatorContext exprEvaluatorContext;
    private EventBeanReader eventBeanReader;
    private final FilterSpecCompiled[] filters;

    /**
     * Ctor.
     * @param statementSpec is a container for the definition of all statement constructs that
     * may have been used in the statement, i.e. if defines the select clauses, insert into, outer joins etc.
     * @param services is the service instances for dependency injection
     * @param statementContext is statement-level information and statement services
     * @throws ExprValidationException if the preparation failed
     */
    public EPPreparedExecuteMethod(StatementSpecCompiled statementSpec,
                                EPServicesContext services,
                                StatementContext statementContext)
            throws ExprValidationException
    {
        boolean queryPlanLogging = services.getConfigSnapshot().getEngineDefaults().getLogging().isEnableQueryPlan();
        if (queryPlanLogging) {
            queryPlanLog.info("Query plans for Fire-and-forget query '" + statementContext.getExpression() + "'");
        }

        this.statementSpec = statementSpec;
        this.exprEvaluatorContext = statementContext;

        validateExecuteQuery();

        int numStreams = statementSpec.getStreamSpecs().size();
        EventType[] typesPerStream = new EventType[numStreams];
        String[] namesPerStream = new String[numStreams];
        processors = new NamedWindowProcessor[numStreams];
        StreamJoinAnalysisResult streamJoinAnalysisResult = new StreamJoinAnalysisResult(numStreams);
        Arrays.fill(streamJoinAnalysisResult.getNamedWindow(), true);

        for (int i = 0; i < numStreams; i++)
        {
            final StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);
            NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) streamSpec;

            String streamName = namedSpec.getWindowName();
            if (namedSpec.getOptionalStreamName() != null)
            {
                streamName = namedSpec.getOptionalStreamName();
            }
            namesPerStream[i] = streamName;

            processors[i] = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());
            typesPerStream[i] = processors[i].getTailView().getEventType();
        }

        // compile filter to optimize access to named window
        filters = new FilterSpecCompiled[numStreams];
        if (statementSpec.getFilterRootNode() != null) {
            LinkedHashMap<String, Pair<EventType, String>> tagged = new LinkedHashMap<String, Pair<EventType, String>>();
            for (int i = 0; i < numStreams; i++) {
                try {
                    StreamTypeServiceImpl types = new StreamTypeServiceImpl(typesPerStream, namesPerStream, new boolean[numStreams], services.getEngineURI(), false);
                    filters[i] = FilterSpecCompiler.makeFilterSpec(typesPerStream[i], namesPerStream[i],
                            Collections.singletonList(statementSpec.getFilterRootNode()), null,
                            tagged, tagged, types,
                            statementContext.getMethodResolutionService(),
                            statementContext.getTimeProvider(),
                            statementContext.getVariableService(),
                            statementContext.getEventAdapterService(),
                            services.getEngineURI(), null, statementContext);
                }
                catch (Exception ex) {
                    log.warn("Unexpected exception analyzing filter paths: " + ex.getMessage(), ex);
                }
            }
        }

        boolean[] isIStreamOnly = new boolean[namesPerStream.length];
        Arrays.fill(isIStreamOnly, true);
        StreamTypeService typeService = new StreamTypeServiceImpl(typesPerStream, namesPerStream, isIStreamOnly, services.getEngineURI(), true);
        EPStatementStartMethod.validateNodes(statementSpec, statementContext, typeService, null);

        resultSetProcessor = ResultSetProcessorFactory.getProcessor(statementSpec, statementContext, typeService, null, new boolean[0], true);

        if (statementSpec.getSelectClauseSpec().isDistinct())
        {
            if (resultSetProcessor.getResultEventType() instanceof EventTypeSPI)
            {
                eventBeanReader = ((EventTypeSPI) resultSetProcessor.getResultEventType()).getReader();
            }
            if (eventBeanReader == null)
            {
                eventBeanReader = new EventBeanReaderDefaultImpl(resultSetProcessor.getResultEventType());
            }
        }

        if (numStreams > 1)
        {
            Viewable[] viewablePerStream = new Viewable[numStreams];
            for (int i = 0; i < numStreams; i++)
            {
                viewablePerStream[i] = processors[i].getTailView();
            }
            joinComposer = statementContext.getJoinSetComposerFactory().makeComposer(statementSpec.getOuterJoinDescList(), statementSpec.getFilterRootNode(), typesPerStream, namesPerStream, viewablePerStream, SelectClauseStreamSelectorEnum.ISTREAM_ONLY, streamJoinAnalysisResult, statementContext, queryPlanLogging);
            if (statementSpec.getFilterRootNode() != null) {
                joinFilter = new JoinSetFilter(statementSpec.getFilterRootNode().getExprEvaluator());
            }
            else {
                joinFilter = null;
            }
        }
        else
        {
            joinComposer = null;
            joinFilter = null;
        }
    }

    /**
     * Returns the event type of the prepared statement.
     * @return event type
     */
    public EventType getEventType()
    {
        return resultSetProcessor.getResultEventType();
    }

    /**
     * Executes the prepared query.
     * @return query results
     */
    public EPPreparedQueryResult execute()
    {
        int numStreams = processors.length;

        Collection<EventBean>[] snapshots = new Collection[numStreams];
        for (int i = 0; i < numStreams; i++)
        {
            final StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);
            NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) streamSpec;
            snapshots[i] = processors[i].getTailView().snapshot(filters[i]);

            if (namedSpec.getFilterExpressions().size() != 0)
            {
                snapshots[i] = getFiltered(snapshots[i], namedSpec.getFilterExpressions());
            }
        }

        resultSetProcessor.clear();

        UniformPair<EventBean[]> results;
        if (numStreams == 1)
        {
            if (statementSpec.getFilterRootNode() != null)
            {
                snapshots[0] = getFiltered(snapshots[0], Arrays.asList(statementSpec.getFilterRootNode()));
            }
            EventBean[] rows = snapshots[0].toArray(new EventBean[snapshots[0].size()]);
            results = resultSetProcessor.processViewResult(rows, null, true);
        }
        else
        {
            EventBean[][] oldDataPerStream = new EventBean[numStreams][];
            EventBean[][] newDataPerStream = new EventBean[numStreams][];
            for (int i = 0; i < numStreams; i++)
            {
                newDataPerStream[i] = snapshots[i].toArray(new EventBean[snapshots[i].size()]);
            }
            UniformPair<Set<MultiKey<EventBean>>> result = joinComposer.join(newDataPerStream, oldDataPerStream, exprEvaluatorContext);
            if (joinFilter != null) {
                joinFilter.process(result.getFirst(), null, exprEvaluatorContext);
            }
            results = resultSetProcessor.processJoinResult(result.getFirst(), null, true);
        }

        if (statementSpec.getSelectClauseSpec().isDistinct())
        {
            results.setFirst(EventBeanUtility.getDistinctByProp(results.getFirst(), eventBeanReader));
        }

        return new EPPreparedQueryResult(resultSetProcessor.getResultEventType(), results.getFirst());
    }

    private void validateExecuteQuery() throws ExprValidationException
    {
        if (statementSpec.getSubSelectExpressions().size() > 0)
        {
            throw new ExprValidationException("Subqueries are not a supported feature of on-demand queries");
        }
        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            if (!(statementSpec.getStreamSpecs().get(i) instanceof NamedWindowConsumerStreamSpec))
            {
                throw new ExprValidationException("On-demand queries require named windows and do not allow event streams or patterns");
            }
            if (statementSpec.getStreamSpecs().get(i).getViewSpecs().size() != 0)
            {
                throw new ExprValidationException("Views are not a supported feature of on-demand queries");
            }
        }
        if (statementSpec.getOutputLimitSpec() != null)
        {
            throw new ExprValidationException("Output rate limiting is not a supported feature of on-demand queries");
        }
        if (statementSpec.getInsertIntoDesc() != null)
        {
            throw new ExprValidationException("Insert-into is not a supported feature of on-demand queries");
        }
    }

    private List<EventBean> getFiltered(Collection<EventBean> snapshot, List<ExprNode> filterExpressions)
    {
        EventBean[] eventsPerStream = new EventBean[1];
        List<EventBean> filteredSnapshot = new ArrayList<EventBean>();
        ExprEvaluator[] evaluators = ExprNodeUtility.getEvaluators(filterExpressions);
        for (EventBean row : snapshot)
        {
            boolean pass = true;
            eventsPerStream[0] = row;
            for (ExprEvaluator filter : evaluators)
            {
                Boolean result = (Boolean) filter.evaluate(eventsPerStream, true, exprEvaluatorContext);
                if (result != null)
                {
                    if (!result)
                    {
                        pass = false;
                        break;
                    }
                }
            }

            if (pass)
            {
                filteredSnapshot.add(row);
            }
        }

        return filteredSnapshot;
    }
}

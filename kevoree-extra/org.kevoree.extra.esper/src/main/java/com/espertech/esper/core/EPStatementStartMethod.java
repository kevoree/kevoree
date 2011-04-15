/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.*;
import com.espertech.esper.client.annotation.HintEnum;
import com.espertech.esper.client.annotation.HookType;
import com.espertech.esper.client.hook.SQLColumnTypeConversion;
import com.espertech.esper.client.hook.SQLOutputRowConversion;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.agg.AggregationServiceFactory;
import com.espertech.esper.epl.core.*;
import com.espertech.esper.epl.db.DatabasePollingViewableFactory;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.join.*;
import com.espertech.esper.epl.join.plan.FilterExprAnalyzer;
import com.espertech.esper.epl.join.plan.QueryGraph;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.epl.join.table.PropertyIndTableCoerceAdd;
import com.espertech.esper.epl.join.table.PropertyIndexedEventTable;
import com.espertech.esper.epl.join.table.UnindexedEventTable;
import com.espertech.esper.epl.lookup.*;
import com.espertech.esper.epl.named.*;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.epl.subquery.SubqueryStopCallback;
import com.espertech.esper.epl.subquery.SubselectAggregatorView;
import com.espertech.esper.epl.subquery.SubselectBufferObserver;
import com.espertech.esper.epl.variable.CreateVariableView;
import com.espertech.esper.epl.variable.OnSetVariableView;
import com.espertech.esper.epl.variable.VariableDeclarationException;
import com.espertech.esper.epl.variable.VariableExistsException;
import com.espertech.esper.epl.view.FilterExprView;
import com.espertech.esper.epl.view.OutputConditionExpression;
import com.espertech.esper.epl.view.OutputProcessView;
import com.espertech.esper.epl.view.OutputProcessViewFactory;
import com.espertech.esper.event.EventAdapterException;
import com.espertech.esper.event.vaevent.ValueAddEventProcessor;
import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.pattern.EvalRootNode;
import com.espertech.esper.pattern.PatternContext;
import com.espertech.esper.pattern.PatternMatchCallback;
import com.espertech.esper.pattern.PatternStopCallback;
import com.espertech.esper.rowregex.EventRowRegexNFAViewFactory;
import com.espertech.esper.util.AuditPath;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.StopCallback;
import com.espertech.esper.view.*;
import com.espertech.esper.view.internal.BufferView;
import com.espertech.esper.view.internal.RouteResultView;
import com.espertech.esper.view.internal.SingleStreamDispatchView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Starts and provides the stop method for EPL statements.
 */
public class EPStatementStartMethod
{
    private static final Log log = LogFactory.getLog(EPStatementStartMethod.class);
    private static final Log queryPlanLog = LogFactory.getLog(AuditPath.QUERYPLAN_LOG);

    private final StatementSpecCompiled statementSpec;
    private final EPServicesContext services;
    private final StatementContext statementContext;
    private final boolean queryPlanLogging;

    /**
     * Ctor.
     * @param statementSpec is a container for the definition of all statement constructs that
     * may have been used in the statement, i.e. if defines the select clauses, insert into, outer joins etc.
     * @param services is the service instances for dependency injection
     * @param statementContext is statement-level information and statement services
     */
    public EPStatementStartMethod(StatementSpecCompiled statementSpec,
                                EPServicesContext services,
                                StatementContext statementContext)
    {
        this.statementSpec = statementSpec;
        this.services = services;
        this.statementContext = statementContext;
        this.queryPlanLogging = services.getConfigSnapshot().getEngineDefaults().getLogging().isEnableQueryPlan();
        if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
            queryPlanLog.info("Query plans for statement '" + statementContext.getStatementName() + "' expression '" + statementContext.getExpression() + "'");
        }
    }

    /**
     * Starts the EPL statement.
     * @return a viewable to attach to for listening to events, and a stop method to invoke to clean up
     * @param isNewStatement indicator whether the statement is new or a stop-restart statement
     * @param isRecoveringStatement true to indicate the statement is in the process of being recovered
     * @param isRecoveringResilient true to indicate the statement is in the process of being recovered and that statement is resilient
     * @throws ExprValidationException when the expression validation fails
     * @throws ViewProcessingException when views cannot be started
     */
    public EPStatementStartResult start(boolean isNewStatement, boolean isRecoveringStatement, boolean isRecoveringResilient)
        throws ExprValidationException, ViewProcessingException
    {
        statementContext.getVariableService().setLocalVersion();    // get current version of variables

        if (statementSpec.getUpdateSpec() != null)
        {
            return startUpdate();
        }
        if (statementSpec.getOnTriggerDesc() != null)
        {
            return startOnTrigger();
        }
        else if (statementSpec.getCreateWindowDesc() != null)
        {
            return startCreateWindow(isNewStatement, isRecoveringStatement);
        }
        else if (statementSpec.getCreateIndexDesc() != null)
        {
            return startCreateIndex();
        }
        else if (statementSpec.getCreateSchemaDesc() != null)
        {
            return startCreateSchema();
        }
        else if (statementSpec.getCreateVariableDesc() != null)
        {
            return startCreateVariable(isNewStatement);
        }
        else
        {
            return startSelect(isRecoveringResilient);
        }
    }

    private EPStatementStartResult startCreateIndex()
        throws ExprValidationException, ViewProcessingException
    {
        final CreateIndexDesc spec = statementSpec.getCreateIndexDesc();
        final NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(spec.getWindowName());
        processor.getRootView().addExplicitIndex(spec.getWindowName(), spec.getIndexName(), spec.getColumns());

        EPStatementStopMethod stopMethod = new EPStatementStopMethod() {
            public void stop()
            {
                processor.getRootView().removeExplicitIndex(spec.getIndexName());
            }
        };
        Viewable viewable = new ViewableDefaultImpl(processor.getNamedWindowType());
        EPStatementStartResult result = new EPStatementStartResult(viewable, stopMethod, null);
        return result;
    }

    private EPStatementStartResult startCreateSchema() throws ExprValidationException
    {
        final CreateSchemaDesc spec = statementSpec.getCreateSchemaDesc();
        EventType eventType = null;

        try {
            if (!spec.isVariant()) {
                if (spec.getTypes().isEmpty()) {
                    Map<String, Object> typing = TypeBuilderUtil.buildType(spec.getColumns());
                    eventType = services.getEventAdapterService().addNestableMapType(spec.getSchemaName(), typing, spec.getInherits(), false, false, true, false, false);
                }
                else {
                    if (spec.getTypes().size() == 1) {
                        String typeName = spec.getTypes().iterator().next();
                        try {
                            eventType = services.getEventAdapterService().addBeanType(spec.getSchemaName(), spec.getTypes().iterator().next(), false, false, false, true);
                        }
                        catch (EventAdapterException ex) {
                            Class clazz;
                            try {
                                clazz = services.getEngineImportService().resolveClass(typeName);
                                eventType = services.getEventAdapterService().addBeanType(spec.getSchemaName(), clazz, false, false, true);
                            }
                            catch (EngineImportException e) {
                                log.debug("Engine import failed to resolve event type '" + typeName + "'");
                                throw ex;
                            }
                        }
                    }
                }
            }
            else {
                boolean isAny = false;
                ConfigurationVariantStream config = new ConfigurationVariantStream();
                for (String typeName : spec.getTypes()) {
                    if (typeName.trim().equals("*")) {
                        isAny = true;
                        break;
                    }
                    config.addEventTypeName(typeName);
                }
                if (!isAny) {
                    config.setTypeVariance(ConfigurationVariantStream.TypeVariance.PREDEFINED);
                }
                else {
                    config.setTypeVariance(ConfigurationVariantStream.TypeVariance.ANY);
                }
                services.getValueAddEventService().addVariantStream(spec.getSchemaName(), config, services.getEventAdapterService());
                eventType = services.getValueAddEventService().getValueAddProcessor(spec.getSchemaName()).getValueAddEventType();
            }
        }
        catch (RuntimeException ex) {
            throw new ExprValidationException(ex.getMessage(), ex);
        }

        // enter a reference
        services.getStatementEventTypeRefService().addReferences(statementContext.getStatementName(), Collections.singleton(spec.getSchemaName()));

        final EventType allocatedEventType = eventType;
        EPStatementStopMethod stopMethod = new EPStatementStopMethod() {
            public void stop()
            {
                services.getStatementEventTypeRefService().removeReferencesStatement(statementContext.getStatementName());
                if (services.getStatementEventTypeRefService().getStatementNamesForType(spec.getSchemaName()).isEmpty()) {
                    services.getEventAdapterService().removeType(allocatedEventType.getName());
                }
            }
        };
        Viewable viewable = new ViewableDefaultImpl(eventType);
        EPStatementStartResult result = new EPStatementStartResult(viewable, stopMethod, null);
        return result;
    }

    private EPStatementStartResult startOnTrigger()
        throws ExprValidationException, ViewProcessingException
    {
        final List<StopCallback> stopCallbacks = new LinkedList<StopCallback>();

        SubSelectStreamCollection subSelectStreamDesc = createSubSelectStreams(true, statementSpec.getAnnotations());
        
        // Create streams
        Viewable eventStreamParentViewable;
        final StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(0);
        String triggereventTypeName = null;

        if (streamSpec instanceof FilterStreamSpecCompiled)
        {
            FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) streamSpec;
            triggereventTypeName = filterStreamSpec.getFilterSpec().getFilterForEventTypeName();

            // Since only for non-joins we get the existing stream's lock and try to reuse it's views
            Pair<EventStream, StatementLock> streamLockPair = services.getStreamService().createStream(statementContext.getStatementId(), filterStreamSpec.getFilterSpec(),
                    statementContext.getFilterService(), statementContext.getEpStatementHandle(), false, false, statementContext, true);
            eventStreamParentViewable = streamLockPair.getFirst();

            // Use the re-used stream's lock for all this statement's locking needs
            if (streamLockPair.getSecond() != null)
            {
                statementContext.getEpStatementHandle().setStatementLock(streamLockPair.getSecond());
            }
        }
        else if (streamSpec instanceof PatternStreamSpecCompiled)
        {
            PatternStreamSpecCompiled patternStreamSpec = (PatternStreamSpecCompiled) streamSpec;
            boolean usedByChildViews = !streamSpec.getViewSpecs().isEmpty() || (statementSpec.getInsertIntoDesc() != null);
            final EventType eventType = services.getEventAdapterService().createSemiAnonymousMapType(patternStreamSpec.getTaggedEventTypes(), patternStreamSpec.getArrayEventTypes(), usedByChildViews);
            final EventStream sourceEventStream = new ZeroDepthStream(eventType);
            eventStreamParentViewable = sourceEventStream;

            EvalRootNode rootNode = services.getPatternNodeFactory().makeRootNode();
            rootNode.addChildNode(patternStreamSpec.getEvalNode());

            PatternMatchCallback callback = new PatternMatchCallback() {
                public void matchFound(Map<String, Object> matchEvent)
                {
                    EventBean compositeEvent = statementContext.getEventAdapterService().adaptorForTypedMap(matchEvent, eventType);
                    sourceEventStream.insert(compositeEvent);
                }
            };

            PatternContext patternContext = statementContext.getPatternContextFactory().createContext(statementContext, 0, rootNode, !patternStreamSpec.getArrayEventTypes().isEmpty());
            PatternStopCallback patternStopCallback = rootNode.start(callback, patternContext);
            stopCallbacks.add(patternStopCallback);
        }
        else if (streamSpec instanceof NamedWindowConsumerStreamSpec)
        {
            NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) streamSpec;
            NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());
            eventStreamParentViewable = processor.addConsumer(namedSpec.getFilterExpressions(), namedSpec.getOptPropertyEvaluator(), statementContext.getEpStatementHandle(), statementContext.getStatementStopService());
            triggereventTypeName = namedSpec.getWindowName();
        }
        else
        {
            throw new ExprValidationException("Unknown stream specification type: " + streamSpec);
        }

        View onExprView;
        final EventType streamEventType = eventStreamParentViewable.getEventType();

        ResultSetProcessor resultSetProcessor;
        // For on-delete and on-select and on-update triggers
        if (statementSpec.getOnTriggerDesc() instanceof OnTriggerWindowDesc)
        {
            // Determine event types
            OnTriggerWindowDesc onTriggerDesc = (OnTriggerWindowDesc) statementSpec.getOnTriggerDesc();
            NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(onTriggerDesc.getWindowName());
            EventType namedWindowType = processor.getNamedWindowType();
            statementContext.getDynamicReferenceEventTypes().add(onTriggerDesc.getWindowName());

            String namedWindowName = onTriggerDesc.getOptionalAsName();
            if (namedWindowName == null)
            {
                namedWindowName = "stream_0";
            }
            String streamName = streamSpec.getOptionalStreamName();
            if (streamName == null)
            {
                streamName = "stream_1";
            }
            String namedWindowTypeName = onTriggerDesc.getWindowName();

            // Materialize sub-select views
            // 0 - named window stream
            // 1 - arriving stream
            startSubSelect(subSelectStreamDesc, new String[]{namedWindowName, streamSpec.getOptionalStreamName()}, new EventType[] {processor.getNamedWindowType(), streamEventType}, new String[]{namedWindowTypeName, triggereventTypeName}, stopCallbacks, statementSpec.getAnnotations());

            StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[] {namedWindowType, streamEventType}, new String[] {namedWindowName, streamName}, new boolean[] {false, true}, services.getEngineURI(), false);
            if (onTriggerDesc instanceof OnTriggerWindowUpdateDesc) {
                OnTriggerWindowUpdateDesc updateDesc = (OnTriggerWindowUpdateDesc) onTriggerDesc;
                for (OnTriggerSetAssignment assignment : updateDesc.getAssignments())
                {
                    ExprNode validated = assignment.getExpression().getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                    assignment.setExpression(validated);
                    validateNoAggregations(validated, "Aggregation functions may not be used within an on-update-clause");
                }
            }
            if (onTriggerDesc instanceof OnTriggerMergeDesc) {
                OnTriggerMergeDesc mergeDesc = (OnTriggerMergeDesc) onTriggerDesc;
                StreamTypeService twoStream = new StreamTypeServiceImpl(new EventType[] {processor.getNamedWindowType(), streamEventType},
                        new String[] {namedWindowName, streamName}, new boolean[] {true, true}, statementContext.getEngineURI(), false);
                StreamTypeService triggerStream = new StreamTypeServiceImpl(streamEventType, streamName, true, statementContext.getEngineURI());
                validateMergeDesc(mergeDesc, statementContext, twoStream, triggerStream);
            }

            // validate join expression
            ExprNode validatedJoin = validateJoinNamedWindow(statementSpec.getFilterRootNode(),
                    namedWindowType, namedWindowName, namedWindowTypeName,
                    streamEventType, streamName, triggereventTypeName);

            // validate filter, output rate limiting
            validateNodes(statementSpec, statementContext, typeService, null);

            // Construct a processor for results; for use in on-select to process selection results
            // Use a wildcard select if the select-clause is empty, such as for on-delete.
            // For on-select the select clause is not empty.
            if (statementSpec.getSelectClauseSpec().getSelectExprList().size() == 0)
            {
                statementSpec.getSelectClauseSpec().add(new SelectClauseElementWildcard());
            }
            resultSetProcessor = ResultSetProcessorFactory.getProcessor(
                    statementSpec, statementContext, typeService, null, new boolean[0], true);

            InternalEventRouter routerService = null;
            boolean addToFront = false;
            if (statementSpec.getInsertIntoDesc() != null) {
                routerService = services.getInternalEventRouter();
                addToFront = statementContext.getNamedWindowService().isNamedWindow(statementSpec.getInsertIntoDesc().getEventTypeName());
            }
            onExprView = processor.addOnExpr(onTriggerDesc, validatedJoin, streamEventType, statementContext.getStatementStopService(), routerService, addToFront, resultSetProcessor, statementContext.getEpStatementHandle(), statementContext.getStatementResultService(), statementContext, statementSpec.getSelectClauseSpec().isDistinct());
            eventStreamParentViewable.addView(onExprView);
        }
        // variable assignments
        else if (statementSpec.getOnTriggerDesc() instanceof OnTriggerSetDesc)
        {
            OnTriggerSetDesc desc = (OnTriggerSetDesc) statementSpec.getOnTriggerDesc();
            StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[] {streamEventType}, new String[] {streamSpec.getOptionalStreamName()}, new boolean[] {true}, services.getEngineURI(), false);

            // Materialize sub-select views
            startSubSelect(subSelectStreamDesc, new String[]{streamSpec.getOptionalStreamName()}, new EventType[] {streamEventType}, new String[]{triggereventTypeName}, stopCallbacks, statementSpec.getAnnotations());

            for (OnTriggerSetAssignment assignment : desc.getAssignments())
            {
                ExprNode validated = assignment.getExpression().getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                assignment.setExpression(validated);
            }

            try {
                onExprView = new OnSetVariableView(desc, statementContext.getEventAdapterService(), statementContext.getVariableService(), statementContext.getStatementResultService(), statementContext);
            }
            catch (VariableValueException ex) {
                throw new ExprValidationException("Error in variable assignment: " + ex.getMessage(), ex);
            }
            eventStreamParentViewable.addView(onExprView);
        }
        // split-stream use case
        else 
        {
            OnTriggerSplitStreamDesc desc = (OnTriggerSplitStreamDesc) statementSpec.getOnTriggerDesc();
            String streamName = streamSpec.getOptionalStreamName();
            if (streamName == null)
            {
                streamName = "stream_0";
            }
            StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[] {streamEventType}, new String[] {streamName}, new boolean[] {true}, services.getEngineURI(), false);
            if (statementSpec.getInsertIntoDesc() == null)
            {
                throw new ExprValidationException("Required insert-into clause is not provided, the clause is required for split-stream syntax");
            }
            if ((!statementSpec.getGroupByExpressions().isEmpty()) || (statementSpec.getHavingExprRootNode() != null) || (!statementSpec.getOrderByList().isEmpty()))
            {
                throw new ExprValidationException("A group-by clause, having-clause or order-by clause is not allowed for the split stream syntax");
            }

            // Materialize sub-select views
            startSubSelect(subSelectStreamDesc, new String[]{streamSpec.getOptionalStreamName()}, new EventType[] {streamEventType}, new String[]{triggereventTypeName}, stopCallbacks, statementSpec.getAnnotations());

            validateNodes(statementSpec, statementContext, typeService, null);

            ResultSetProcessor[] processors = new ResultSetProcessor[desc.getSplitStreams().size() + 1];
            ExprNode[] whereClauses = new ExprNode[desc.getSplitStreams().size() + 1];
            processors[0] = ResultSetProcessorFactory.getProcessor(
                    statementSpec, statementContext, typeService, null, new boolean[0], false);
            whereClauses[0] = statementSpec.getFilterRootNode();
            boolean[] isNamedWindowInsert = new boolean[desc.getSplitStreams().size() + 1];
            isNamedWindowInsert[0] = false;

            int index = 1;
            for (OnTriggerSplitStream splits : desc.getSplitStreams())
            {
                StatementSpecCompiled splitSpec = new StatementSpecCompiled();
                splitSpec.setInsertIntoDesc(splits.getInsertInto());
                splitSpec.setSelectClauseSpec(StatementLifecycleSvcImpl.compileSelectAllowSubselect(splits.getSelectClause()));
                splitSpec.setFilterExprRootNode(splits.getWhereClause());
                validateNodes(splitSpec, statementContext, typeService, null);

                processors[index] = ResultSetProcessorFactory.getProcessor(
                    splitSpec, statementContext, typeService, null, new boolean[0], false);
                whereClauses[index] = splitSpec.getFilterRootNode();
                isNamedWindowInsert[index] = statementContext.getNamedWindowService().isNamedWindow(splits.getInsertInto().getEventTypeName());

                index++;
            }

            onExprView = new RouteResultView(desc.isFirst(), streamEventType, statementContext.getEpStatementHandle(), services.getInternalEventRouter(), isNamedWindowInsert, processors, whereClauses, statementContext);
            eventStreamParentViewable.addView(onExprView);
        }

        // create stop method using statement stream specs
        EPStatementStopMethod stopMethod = new EPStatementStopMethod()
        {
            public void stop()
            {
                statementContext.getStatementStopService().fireStatementStopped();

                if (streamSpec instanceof FilterStreamSpecCompiled)
                {
                    FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) streamSpec;
                    services.getStreamService().dropStream(filterStreamSpec.getFilterSpec(), statementContext.getFilterService(), false, false, true);
                }
                for (StopCallback stopCallback : stopCallbacks)
                {
                    stopCallback.stop();
                }
            }
        };

        // For on-delete, create an output processor that passes on as a wildcard the underlying event
        if ((statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_DELETE) ||
            (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_SET) ||
            (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_UPDATE) ||
            (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_MERGE))
        {
            StatementSpecCompiled defaultSelectAllSpec = new StatementSpecCompiled();
            defaultSelectAllSpec.getSelectClauseSpec().add(new SelectClauseElementWildcard());

            StreamTypeService streamTypeService = new StreamTypeServiceImpl(new EventType[] {onExprView.getEventType()}, new String[] {"trigger_stream"}, new boolean[] {true}, services.getEngineURI(), false);
            ResultSetProcessor outputResultSetProcessor = ResultSetProcessorFactory.getProcessor(
                    defaultSelectAllSpec, statementContext, streamTypeService, null, new boolean[0], true);

            // Attach output view
            OutputProcessView outputView = OutputProcessViewFactory.makeView(outputResultSetProcessor, defaultSelectAllSpec, statementContext, services.getInternalEventRouter());
            onExprView.addView(outputView);
            onExprView = outputView;
        }

        log.debug(".start Statement start completed");

        return new EPStatementStartResult(onExprView, stopMethod);
    }

    private ExprNode validateExprNoAgg(ExprNode exprNode, StreamTypeService streamTypeService, StatementContext statementContext, String errorMsg) throws ExprValidationException {
        ExprNode validated = exprNode.getValidatedSubtree(streamTypeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
        validateNoAggregations(validated, errorMsg);
        return validated;
    }

    private void validateMergeDesc(OnTriggerMergeDesc mergeDesc, StatementContext statementContext, StreamTypeService twoStreamTypeSvc, StreamTypeService singleStreamTypeSvc)
        throws ExprValidationException
    {
        String exprNodeErrorMessage = "Aggregation functions may not be used within an merge-clause";
        for (OnTriggerMergeItem item : mergeDesc.getItems()) {
            if (item instanceof OnTriggerMergeItemDelete) {
                OnTriggerMergeItemDelete delete = (OnTriggerMergeItemDelete) item;
                if (delete.getOptionalMatchCond() != null) {
                    delete.setOptionalMatchCond(validateExprNoAgg(delete.getOptionalMatchCond(), twoStreamTypeSvc, statementContext, exprNodeErrorMessage));
                }
            }
            else if (item instanceof OnTriggerMergeItemUpdate) {
                OnTriggerMergeItemUpdate update = (OnTriggerMergeItemUpdate) item;
                if (update.getOptionalMatchCond() != null) {
                    update.setOptionalMatchCond(validateExprNoAgg(update.getOptionalMatchCond(), twoStreamTypeSvc, statementContext, exprNodeErrorMessage));
                }
                for (OnTriggerSetAssignment assignment : update.getAssignments())
                {
                    assignment.setExpression(validateExprNoAgg(assignment.getExpression(), twoStreamTypeSvc, statementContext, exprNodeErrorMessage));
                }
            }
            else if (item instanceof OnTriggerMergeItemInsert) {
                OnTriggerMergeItemInsert insert = (OnTriggerMergeItemInsert) item;
                List<SelectClauseElementCompiled> compiledSelect = new ArrayList<SelectClauseElementCompiled>();
                if (insert.getOptionalMatchCond() != null) {
                    insert.setOptionalMatchCond(validateExprNoAgg(insert.getOptionalMatchCond(), singleStreamTypeSvc, statementContext, exprNodeErrorMessage));
                }
                int colIndex = 0;
                for (SelectClauseElementRaw raw : insert.getSelectClause())
                {
                    if (raw instanceof SelectClauseStreamRawSpec)
                    {
                        SelectClauseStreamRawSpec rawStreamSpec = (SelectClauseStreamRawSpec) raw;
                        if (!rawStreamSpec.getStreamName().equals(singleStreamTypeSvc.getStreamNames()[0]))
                        {
                            throw new ExprValidationException("Stream by name '" + rawStreamSpec.getStreamName() + "' was not found");
                        }
                        SelectClauseStreamCompiledSpec streamSelectSpec = new SelectClauseStreamCompiledSpec(rawStreamSpec.getStreamName(), rawStreamSpec.getOptionalAsName());
                        streamSelectSpec.setStreamNumber(0);
                        compiledSelect.add(streamSelectSpec);
                    }
                    else if (raw instanceof SelectClauseExprRawSpec)
                    {
                        SelectClauseExprRawSpec exprSpec = (SelectClauseExprRawSpec) raw;
                        ExprNode exprCompiled = exprSpec.getSelectExpression().getValidatedSubtree(singleStreamTypeSvc, statementContext.getMethodResolutionService(), null, statementContext.getTimeProvider(), statementContext.getVariableService(), statementContext);
                        String resultName = exprSpec.getOptionalAsName();
                        if (resultName == null)
                        {
                            if (insert.getColumns().size() > colIndex) {
                                resultName = insert.getColumns().get(colIndex);
                            }
                            else {
                                resultName = exprCompiled.toExpressionString();
                            }
                        }
                        compiledSelect.add(new SelectClauseExprCompiledSpec(exprCompiled, resultName));
                        validateNoAggregations(exprCompiled, "Expression in a merge-selection may not utilize aggregation functions");
                    }
                    else if (raw instanceof SelectClauseElementWildcard)
                    {
                        compiledSelect.add(new SelectClauseElementWildcard());
                    }
                    else
                    {
                        throw new IllegalStateException("Unknown select clause item:" + raw);
                    }
                    colIndex++;
                }
                insert.setSelectClauseCompiled(compiledSelect);
            }
            else {
                throw new IllegalArgumentException("Unrecognized merge item '" + item.getClass().getName() + "'");
            }
        }
    }

    private EPStatementStartResult startUpdate()
        throws ExprValidationException, ViewProcessingException
    {
        final List<StopCallback> stopCallbacks = new LinkedList<StopCallback>();

        // First we create streams for subselects, if there are any
        SubSelectStreamCollection subSelectStreamDesc = createSubSelectStreams(false, statementSpec.getAnnotations());

        final StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(0);
        final UpdateDesc desc = statementSpec.getUpdateSpec();
        String triggereventTypeName;

        if (streamSpec instanceof FilterStreamSpecCompiled)
        {
            FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) streamSpec;
            triggereventTypeName = filterStreamSpec.getFilterSpec().getFilterForEventTypeName();
        }
        else if (streamSpec instanceof NamedWindowConsumerStreamSpec)
        {
            NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) streamSpec;
            triggereventTypeName = namedSpec.getWindowName();
        }
        else
        {
            throw new ExprValidationException("Unknown stream specification streamEventType: " + streamSpec);
        }

        // determine a stream name
        String streamName = triggereventTypeName;
        if (desc.getOptionalStreamName() != null)
        {
            streamName = desc.getOptionalStreamName();
        }

        final EventType streamEventType = services.getEventAdapterService().getExistsTypeByName(triggereventTypeName);
        StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[] {streamEventType}, new String[] {streamName}, new boolean[] {true}, services.getEngineURI(), false);

        // determine subscriber result types
        statementContext.getStatementResultService().setSelectClause(new Class[] {streamEventType.getUnderlyingType()}, new String[] {"*"}, false, null, statementContext);

        // Materialize sub-select views
        startSubSelect(subSelectStreamDesc, new String[]{streamName}, new EventType[] {streamEventType}, new String[]{triggereventTypeName}, stopCallbacks, statementSpec.getAnnotations());

        for (OnTriggerSetAssignment assignment : desc.getAssignments())
        {
            ExprNode validated = assignment.getExpression().getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
            assignment.setExpression(validated);
            validateNoAggregations(validated, "Aggregation functions may not be used within an update-clause");
        }
        if (desc.getOptionalWhereClause() != null)
        {
            ExprNode validated = desc.getOptionalWhereClause().getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
            desc.setOptionalWhereClause(validated);
            validateNoAggregations(validated, "Aggregation functions may not be used within an update-clause");
        }

        InternalRoutePreprocessView onExprView = new InternalRoutePreprocessView(streamEventType, statementContext.getStatementResultService());
        services.getInternalEventRouter().addPreprocessing(streamEventType, desc, statementSpec.getAnnotations(), onExprView);
        stopCallbacks.add(new StopCallback()
        {
            public void stop()
            {
                services.getInternalEventRouter().removePreprocessing(streamEventType, desc);
            }
        });

        EPStatementStopMethod stopMethod = new EPStatementStopMethod()
        {
            public void stop()
            {
                statementContext.getStatementStopService().fireStatementStopped();

                for (StopCallback stopCallback : stopCallbacks)
                {
                    stopCallback.stop();
                }
            }
        };
        return new EPStatementStartResult(onExprView, stopMethod);
    }

    private EPStatementStartResult startCreateWindow(boolean isNewStatement, boolean isRecoveringStatement)
        throws ExprValidationException, ViewProcessingException
    {
        final FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) statementSpec.getStreamSpecs().get(0);
        String windowName = statementSpec.getCreateWindowDesc().getWindowName();
        EventType windowType = filterStreamSpec.getFilterSpec().getFilterForEventType();

        ValueAddEventProcessor optionalRevisionProcessor = statementContext.getValueAddEventService().getValueAddProcessor(windowName);
        boolean isPrioritized = services.getEngineSettingsService().getEngineSettings().getExecution().isPrioritized();
        services.getNamedWindowService().addProcessor(windowName, windowType, statementContext.getEpStatementHandle(), statementContext.getStatementResultService(), optionalRevisionProcessor, statementContext.getExpression(), statementContext.getStatementName(), isPrioritized, statementContext, statementSpec.getAnnotations());

        // Create streams and views
        Viewable eventStreamParentViewable;
        ViewFactoryChain unmaterializedViewChain;

        // Create view factories and parent view based on a filter specification
        // Since only for non-joins we get the existing stream's lock and try to reuse it's views
        Pair<EventStream, StatementLock> streamLockPair = services.getStreamService().createStream(statementContext.getStatementId(), filterStreamSpec.getFilterSpec(),
                statementContext.getFilterService(), statementContext.getEpStatementHandle(), false, false, statementContext, true);
        eventStreamParentViewable = streamLockPair.getFirst();

        // Use the re-used stream's lock for all this statement's locking needs
        if (streamLockPair.getSecond() != null)
        {
            statementContext.getEpStatementHandle().setStatementLock(streamLockPair.getSecond());
        }

        // Create data window view factories
        unmaterializedViewChain = services.getViewService().createFactories(0, eventStreamParentViewable.getEventType(), filterStreamSpec.getViewSpecs(), filterStreamSpec.getOptions(), statementContext);

        // The root view of the named window
        NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(statementSpec.getCreateWindowDesc().getWindowName());
        View rootView = processor.getRootView();
        eventStreamParentViewable.addView(rootView);

        // request remove stream capability from views
        ViewResourceDelegate viewResourceDelegate = new ViewResourceDelegateImpl(new ViewFactoryChain[] {unmaterializedViewChain}, statementContext);
        if (!viewResourceDelegate.requestCapability(0, new RemoveStreamViewCapability(false), null))
        {
            throw new ExprValidationException(NamedWindowService.ERROR_MSG_DATAWINDOWS);
        }

        // create stop method using statement stream specs
        EPStatementStopMethod stopMethod = new EPStatementStopMethod()
        {
            public void stop()
            {
                statementContext.getStatementStopService().fireStatementStopped();
                services.getStreamService().dropStream(filterStreamSpec.getFilterSpec(), statementContext.getFilterService(), false,false, true);
                String windowName = statementSpec.getCreateWindowDesc().getWindowName();
                services.getNamedWindowService().removeProcessor(windowName);
            }
        };

        // Materialize views
        Viewable finalView = services.getViewService().createViews(rootView, unmaterializedViewChain.getViewFactoryChain(), statementContext);

        // Attach tail view
        boolean isBatchView = finalView instanceof BatchingDataWindowView;
        NamedWindowTailView tailView = processor.getTailView();
        tailView.setBatchView(isBatchView);
        processor.getRootView().setBatchView(isBatchView);
        finalView.addView(tailView);
        finalView = tailView;

        // Add a wildcard to the select clause as subscribers received the window contents
        statementSpec.getSelectClauseSpec().getSelectExprList().clear();
        statementSpec.getSelectClauseSpec().add(new SelectClauseElementWildcard());
        statementSpec.setSelectStreamDirEnum(SelectClauseStreamSelectorEnum.RSTREAM_ISTREAM_BOTH);

        StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[] {windowType}, new String[] {windowName}, new boolean[] {true}, services.getEngineURI(), false);
        ResultSetProcessor resultSetProcessor = ResultSetProcessorFactory.getProcessor(
                statementSpec, statementContext, typeService, null, new boolean[0], true);

        // Attach output view
        OutputProcessView outputView = OutputProcessViewFactory.makeView(resultSetProcessor, statementSpec, statementContext, services.getInternalEventRouter());
        finalView.addView(outputView);
        finalView = outputView;

        // Handle insert case
        if (statementSpec.getCreateWindowDesc().isInsert() && !isRecoveringStatement)
        {
            String insertFromWindow = statementSpec.getCreateWindowDesc().getInsertFromWindow();
            NamedWindowProcessor sourceWindow = services.getNamedWindowService().getProcessor(insertFromWindow);
            List<EventBean> events = new ArrayList<EventBean>();
            if (statementSpec.getCreateWindowDesc().getInsertFilter() != null)
            {
                EventBean[] eventsPerStream = new EventBean[1];
                ExprEvaluator filter = statementSpec.getCreateWindowDesc().getInsertFilter().getExprEvaluator();
                for (Iterator<EventBean> it = sourceWindow.getTailView().iterator(); it.hasNext();)
                {
                    EventBean candidate = it.next();
                    eventsPerStream[0] = candidate;
                    Boolean result = (Boolean) filter.evaluate(eventsPerStream, true, statementContext);
                    if ((result == null) || (!result))
                    {
                        continue;
                    }
                    events.add(candidate);
                }
            }
            else
            {
                for (Iterator<EventBean> it = sourceWindow.getTailView().iterator(); it.hasNext();)
                {
                    events.add(it.next());
                }
            }
            if (events.size() > 0)
            {
                EventType rootViewType = rootView.getEventType();
                EventBean[] convertedEvents = services.getEventAdapterService().typeCast(events, rootViewType);
                rootView.update(convertedEvents, null);
            }
        }

        log.debug(".start Statement start completed");

        return new EPStatementStartResult(finalView, stopMethod);
    }

    private EPStatementStartResult startCreateVariable(boolean isNewStatement)
        throws ExprValidationException, ViewProcessingException
    {
        final CreateVariableDesc createDesc = statementSpec.getCreateVariableDesc();

        // Get assignment value
        Object value = null;
        if (createDesc.getAssignment() != null)
        {
            // Evaluate assignment expression
            StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[0], new String[0], new boolean[0], services.getEngineURI(), false);
            ExprNode validated = createDesc.getAssignment().getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
            value = validated.getExprEvaluator().evaluate(null, true, statementContext);
        }

        // Create variable
        try
        {
            services.getVariableService().createNewVariable(createDesc.getVariableName(), createDesc.getVariableType(), value, statementContext.getExtensionServicesContext());
        }
        catch (VariableExistsException ex)
        {
            // for new statement we don't allow creating the same variable
            if (isNewStatement)
            {
                throw new ExprValidationException("Cannot create variable: " + ex.getMessage());
            }
        }
        catch (VariableDeclarationException ex)
        {
            throw new ExprValidationException("Cannot create variable: " + ex.getMessage());
        }

        final CreateVariableView createView = new CreateVariableView(services.getEventAdapterService(), services.getVariableService(), createDesc.getVariableName(), statementContext.getStatementResultService());
        final int variableNum = services.getVariableService().getReader(createDesc.getVariableName()).getVariableNumber();
        services.getVariableService().registerCallback(variableNum, createView);
        statementContext.getStatementStopService().addSubscriber(new StatementStopCallback() {
            public void statementStopped()
            {
                services.getVariableService().unregisterCallback(variableNum, createView);
            }
        });

        // Create result set processor, use wildcard selection
        statementSpec.getSelectClauseSpec().getSelectExprList().clear();
        statementSpec.getSelectClauseSpec().add(new SelectClauseElementWildcard());
        statementSpec.setSelectStreamDirEnum(SelectClauseStreamSelectorEnum.RSTREAM_ISTREAM_BOTH);
        StreamTypeService typeService = new StreamTypeServiceImpl(new EventType[] {createView.getEventType()}, new String[] {"create_variable"}, new boolean[] {true}, services.getEngineURI(), false);
        ResultSetProcessor resultSetProcessor = ResultSetProcessorFactory.getProcessor(
                statementSpec, statementContext, typeService, null, new boolean[0], true);

        // Attach output view
        OutputProcessView outputView = OutputProcessViewFactory.makeView(resultSetProcessor, statementSpec, statementContext, services.getInternalEventRouter());
        createView.addView(outputView);

        services.getStatementVariableRefService().addReferences(statementContext.getStatementName(), Collections.singleton(createDesc.getVariableName()));
        EPStatementDestroyMethod destroyMethod = new EPStatementDestroyMethod() {
            public void destroy() {
                try {
                    services.getStatementVariableRefService().removeReferencesStatement(statementContext.getStatementName());
                }
                catch (RuntimeException ex) {
                    log.error("Error removing variable '" + createDesc.getVariableName() + "': " + ex.getMessage());
                }
            }
        };

        EPStatementStopMethod stopMethod = new EPStatementStopMethod(){
            public void stop()
            {
            }
        };

        return new EPStatementStartResult(outputView, stopMethod, destroyMethod);
    }

    private EPStatementStartResult startSelect(boolean isRecoveringResilient)
        throws ExprValidationException, ViewProcessingException
    {
        // Determine stream names for each stream - some streams may not have a name given
        String[] streamNames = determineStreamNames(statementSpec.getStreamSpecs());
        final boolean isJoin = statementSpec.getStreamSpecs().size() > 1;

        // First we create streams for subselects, if there are any
        SubSelectStreamCollection subSelectStreamDesc = createSubSelectStreams(isJoin, statementSpec.getAnnotations());

        int numStreams = streamNames.length;
        final List<StopCallback> stopCallbacks = new LinkedList<StopCallback>();

        // Create streams and views
        Viewable[] eventStreamParentViewable = new Viewable[numStreams];
        ViewFactoryChain[] unmaterializedViewChain = new ViewFactoryChain[numStreams];
        String[] eventTypeNamees = new String[numStreams];
        boolean[] isNamedWindow = new boolean[numStreams];

        // verify for joins that required views are present
        StreamJoinAnalysisResult joinAnalysisResult = verifyJoinViews(statementSpec.getStreamSpecs());

        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);

            // Create view factories and parent view based on a filter specification
            if (streamSpec instanceof FilterStreamSpecCompiled)
            {
                FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) streamSpec;
                eventTypeNamees[i] = filterStreamSpec.getFilterSpec().getFilterForEventTypeName();

                // Since only for non-joins we get the existing stream's lock and try to reuse it's views
                Pair<EventStream, StatementLock> streamLockPair = services.getStreamService().createStream(statementContext.getStatementId(), filterStreamSpec.getFilterSpec(),
                        statementContext.getFilterService(), statementContext.getEpStatementHandle(), isJoin, false, statementContext, false | !statementSpec.getOrderByList().isEmpty());
                eventStreamParentViewable[i] = streamLockPair.getFirst();

                // Use the re-used stream's lock for all this statement's locking needs
                if (streamLockPair.getSecond() != null)
                {
                    statementContext.getEpStatementHandle().setStatementLock(streamLockPair.getSecond());
                }

                unmaterializedViewChain[i] = services.getViewService().createFactories(i, eventStreamParentViewable[i].getEventType(), streamSpec.getViewSpecs(), streamSpec.getOptions(), statementContext);
            }
            // Create view factories and parent view based on a pattern expression
            else if (streamSpec instanceof PatternStreamSpecCompiled)
            {
                PatternStreamSpecCompiled patternStreamSpec = (PatternStreamSpecCompiled) streamSpec;
                boolean usedByChildViews = !streamSpec.getViewSpecs().isEmpty() || (statementSpec.getInsertIntoDesc() != null);
                final EventType eventType = services.getEventAdapterService().createSemiAnonymousMapType(patternStreamSpec.getTaggedEventTypes(), patternStreamSpec.getArrayEventTypes(), usedByChildViews);
                final EventStream sourceEventStream = new ZeroDepthStream(eventType);
                eventStreamParentViewable[i] = sourceEventStream;
                unmaterializedViewChain[i] = services.getViewService().createFactories(i, sourceEventStream.getEventType(), streamSpec.getViewSpecs(), streamSpec.getOptions(), statementContext);

                EvalRootNode rootNode = services.getPatternNodeFactory().makeRootNode();
                rootNode.addChildNode(patternStreamSpec.getEvalNode());

                PatternMatchCallback callback = new PatternMatchCallback() {
                    public void matchFound(Map<String, Object> matchEvent)
                    {
                        EventBean compositeEvent = statementContext.getEventAdapterService().adaptorForTypedMap(matchEvent, eventType);
                        sourceEventStream.insert(compositeEvent);
                    }
                };

                PatternContext patternContext = statementContext.getPatternContextFactory().createContext(statementContext,
                        i, rootNode, !patternStreamSpec.getArrayEventTypes().isEmpty());
                PatternStopCallback patternStopCallback = rootNode.start(callback, patternContext);
                stopCallbacks.add(patternStopCallback);
            }
            // Create view factories and parent view based on a database SQL statement
            else if (streamSpec instanceof DBStatementStreamSpec)
            {
                if (!streamSpec.getViewSpecs().isEmpty())
                {
                    throw new ExprValidationException("Historical data joins do not allow views onto the data, view '"
                            + streamSpec.getViewSpecs().get(0).getObjectNamespace() + ':' + streamSpec.getViewSpecs().get(0).getObjectName() + "' is not valid in this context");
                }

                DBStatementStreamSpec sqlStreamSpec = (DBStatementStreamSpec) streamSpec;
                SQLColumnTypeConversion typeConversionHook = (SQLColumnTypeConversion) JavaClassHelper.getAnnotationHook(statementSpec.getAnnotations(), HookType.SQLCOL, SQLColumnTypeConversion.class, statementContext.getMethodResolutionService());
                SQLOutputRowConversion outputRowConversionHook = (SQLOutputRowConversion) JavaClassHelper.getAnnotationHook(statementSpec.getAnnotations(), HookType.SQLROW, SQLOutputRowConversion.class, statementContext.getMethodResolutionService());
                HistoricalEventViewable historicalEventViewable = DatabasePollingViewableFactory.createDBStatementView(i, sqlStreamSpec, services.getDatabaseRefService(), services.getEventAdapterService(), statementContext.getEpStatementHandle(), typeConversionHook, outputRowConversionHook,
                        statementContext.getConfigSnapshot().getEngineDefaults().getLogging().isEnableJDBC());
                unmaterializedViewChain[i] = new ViewFactoryChain(historicalEventViewable.getEventType(), new LinkedList<ViewFactory>());
                eventStreamParentViewable[i] = historicalEventViewable;
                stopCallbacks.add(historicalEventViewable);
            }
            else if (streamSpec instanceof MethodStreamSpec)
            {
                if (!streamSpec.getViewSpecs().isEmpty())
                {
                    throw new ExprValidationException("Method data joins do not allow views onto the data, view '"
                            + streamSpec.getViewSpecs().get(0).getObjectNamespace() + ':' + streamSpec.getViewSpecs().get(0).getObjectName() + "' is not valid in this context");
                }

                MethodStreamSpec methodStreamSpec = (MethodStreamSpec) streamSpec;
                HistoricalEventViewable historicalEventViewable = MethodPollingViewableFactory.createPollMethodView(i, methodStreamSpec, services.getEventAdapterService(), statementContext.getEpStatementHandle(), statementContext.getMethodResolutionService(), services.getEngineImportService(), statementContext.getSchedulingService(), statementContext.getScheduleBucket(), statementContext);
                unmaterializedViewChain[i] = new ViewFactoryChain(historicalEventViewable.getEventType(), new LinkedList<ViewFactory>());
                eventStreamParentViewable[i] = historicalEventViewable;
                stopCallbacks.add(historicalEventViewable);
            }
            else if (streamSpec instanceof NamedWindowConsumerStreamSpec)
            {
                NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) streamSpec;
                NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());
                NamedWindowConsumerView consumerView = processor.addConsumer(namedSpec.getFilterExpressions(), namedSpec.getOptPropertyEvaluator(), statementContext.getEpStatementHandle(), statementContext.getStatementStopService());
                eventStreamParentViewable[i] = consumerView;
                unmaterializedViewChain[i] = services.getViewService().createFactories(i, consumerView.getEventType(), namedSpec.getViewSpecs(), namedSpec.getOptions(), statementContext);
                joinAnalysisResult.setNamedWindow(i);
                eventTypeNamees[i] = namedSpec.getWindowName();
                isNamedWindow[i] = true;

                // Consumers to named windows cannot declare a data window view onto the named window to avoid duplicate remove streams
                ViewResourceDelegate viewResourceDelegate = new ViewResourceDelegateImpl(unmaterializedViewChain, statementContext);
                viewResourceDelegate.requestCapability(i, new NotADataWindowViewCapability(), null);
            }
            else
            {
                throw new ExprValidationException("Unknown stream specification type: " + streamSpec);
            }
        }

        if (statementSpec.getMatchRecognizeSpec() != null)
        {
            if (isJoin)
            {
                throw new ExprValidationException("Joins are not allowed when using match recognize");
            }
            boolean isUnbound = (unmaterializedViewChain[0].getViewFactoryChain().isEmpty()) && (!(statementSpec.getStreamSpecs().get(0) instanceof NamedWindowConsumerStreamSpec));
            EventRowRegexNFAViewFactory factory = new EventRowRegexNFAViewFactory(unmaterializedViewChain[0], statementSpec.getMatchRecognizeSpec(), statementContext, isUnbound, statementSpec.getAnnotations());
            unmaterializedViewChain[0].getViewFactoryChain().add(factory);
        }

        // Obtain event types from ViewFactoryChains
        EventType[] streamEventTypes = new EventType[statementSpec.getStreamSpecs().size()];
        for (int i = 0; i < unmaterializedViewChain.length; i++)
        {
            streamEventTypes[i] = unmaterializedViewChain[i].getEventType();
        }

        // Materialize sub-select views
        startSubSelect(subSelectStreamDesc, streamNames, streamEventTypes, eventTypeNamees, stopCallbacks, statementSpec.getAnnotations());

        // List of statement streams
        final List<StreamSpecCompiled> statementStreamSpecs = new ArrayList<StreamSpecCompiled>();
        statementStreamSpecs.addAll(statementSpec.getStreamSpecs());

        // Construct type information per stream
        StreamTypeService typeService = new StreamTypeServiceImpl(streamEventTypes, streamNames, getHasIStreamOnly(isNamedWindow, unmaterializedViewChain), services.getEngineURI(), false);
        ViewResourceDelegate viewResourceDelegate = new ViewResourceDelegateImpl(unmaterializedViewChain, statementContext);

        // boolean multiple expiry policy
        for (int i = 0; i < unmaterializedViewChain.length; i++)
        {
            if (unmaterializedViewChain[i].getDataWindowViewFactoryCount() > 1)
            {
                if (!viewResourceDelegate.requestCapability(i, new RemoveStreamViewCapability(true), null))
                {
                    log.warn("Combination of multiple data window expiry policies with views that do not support remove streams is not allowed");
                }
            }
        }

        // create stop method using statement stream specs
        EPStatementStopMethod stopMethod = new EPStatementStopMethod()
        {
            public void stop()
            {
                statementContext.getStatementStopService().fireStatementStopped();

                for (StreamSpecCompiled streamSpec : statementStreamSpecs)
                {
                    if (streamSpec instanceof FilterStreamSpecCompiled)
                    {
                        FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) streamSpec;
                        services.getStreamService().dropStream(filterStreamSpec.getFilterSpec(), statementContext.getFilterService(), isJoin, false, false | !statementSpec.getOrderByList().isEmpty());
                    }
                }
                for (StopCallback stopCallback : stopCallbacks)
                {
                    stopCallback.stop();
                }
                for (ExprSubselectNode subselect : statementSpec.getSubSelectExpressions())
                {
                    StreamSpecCompiled subqueryStreamSpec = subselect.getStatementSpecCompiled().getStreamSpecs().get(0);
                    if (subqueryStreamSpec instanceof FilterStreamSpecCompiled)
                    {
                        FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) subselect.getStatementSpecCompiled().getStreamSpecs().get(0);
                        services.getStreamService().dropStream(filterStreamSpec.getFilterSpec(), statementContext.getFilterService(), isJoin, true, false);
                    }
                }
            }
        };

        // Validate views that require validation, specifically streams that don't have
        // sub-views such as DB SQL joins
        for (int stream = 0; stream < eventStreamParentViewable.length; stream++)
        {
            Viewable viewable = eventStreamParentViewable[stream];
            if (viewable instanceof ValidatedView)
            {
                ValidatedView validatedView = (ValidatedView) viewable;
                validatedView.validate(services.getEngineImportService(), 
                        typeService,
                        statementContext.getMethodResolutionService(),
                        statementContext.getTimeProvider(),
                        statementContext.getVariableService(), statementContext,
                        services.getConfigSnapshot(), services.getSchedulingService(), services.getEngineURI(),
                        statementSpec.getSqlParameters());
            }
            if (viewable instanceof HistoricalEventViewable)
            {
                HistoricalEventViewable historicalView = (HistoricalEventViewable) viewable;
                if (historicalView.getRequiredStreams().contains(stream))
                {
                    throw new ExprValidationException("Parameters for historical stream " + stream + " indicate that the stream is subordinate to itself as stream parameters originate in the same stream");
                }
            }
        }

        // Construct a processor for results posted by views and joins, which takes care of aggregation if required.
        // May return null if we don't need to post-process results posted by views or joins.
        ResultSetProcessor resultSetProcessor = ResultSetProcessorFactory.getProcessor(
                statementSpec, statementContext, typeService, viewResourceDelegate, joinAnalysisResult.getUnidirectionalInd(), true);

        // Validate where-clause filter tree, outer join clause and output limit expression
        validateNodes(statementSpec, statementContext, typeService, viewResourceDelegate);

        // Materialize views
        Viewable[] streamViews = new Viewable[streamEventTypes.length];
        for (int i = 0; i < streamViews.length; i++)
        {
            streamViews[i] = services.getViewService().createViews(eventStreamParentViewable[i], unmaterializedViewChain[i].getViewFactoryChain(), statementContext);
        }

        // For just 1 event stream without joins, handle the one-table process separatly.
        Viewable finalView;
        JoinPreloadMethod joinPreloadMethod = null;
        if (streamNames.length == 1)
        {
            finalView = handleSimpleSelect(streamViews[0], resultSetProcessor, statementContext);
        }
        else
        {
            Pair<Viewable, JoinPreloadMethod> pair = handleJoin(streamNames, streamEventTypes, streamViews, resultSetProcessor, statementSpec.getSelectStreamSelectorEnum(), statementContext, stopCallbacks, joinAnalysisResult);
            finalView = pair.getFirst();
            joinPreloadMethod = pair.getSecond();
        }

        // Replay any named window data, for later consumers of named data windows
        boolean hasNamedWindow = false;
        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);
            if (streamSpec instanceof NamedWindowConsumerStreamSpec)
            {
                hasNamedWindow = true;
                NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) streamSpec;
                NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());
                NamedWindowTailView consumerView = processor.getTailView();
                NamedWindowConsumerView view = (NamedWindowConsumerView) eventStreamParentViewable[i];

                // preload view for stream unless the expiry policy is batch window
                ArrayList<EventBean> eventsInWindow = new ArrayList<EventBean>();
                if (!consumerView.isParentBatchWindow())
                {
                    for (EventBean aConsumerView : consumerView)
                    {
                        eventsInWindow.add(aConsumerView);
                    }
                }
                if (!eventsInWindow.isEmpty() && !isRecoveringResilient)
                {
                    EventBean[] newEvents = eventsInWindow.toArray(new EventBean[eventsInWindow.size()]);
                    view.update(newEvents, null);
                    if (joinPreloadMethod != null && !joinPreloadMethod.isPreloading() && statementContext.getEpStatementHandle().getOptionalDispatchable() != null) {
                        statementContext.getEpStatementHandle().getOptionalDispatchable().execute(statementContext);
                    }
                }

                // in a join, preload indexes, if any
                if (joinPreloadMethod != null)
                {
                    joinPreloadMethod.preloadFromBuffer(i);
                }
                else
                {
                    if (statementContext.getEpStatementHandle().getOptionalDispatchable() != null) {
                        statementContext.getEpStatementHandle().getOptionalDispatchable().execute(statementContext);                        
                    }
                }
            }
        }
        // last, for aggregation we need to send the current join results to the result set processor
        if ((hasNamedWindow) && (joinPreloadMethod != null) && (!isRecoveringResilient))
        {
            joinPreloadMethod.preloadAggregation(resultSetProcessor);
        }

        log.debug(".start Statement start completed");

        return new EPStatementStartResult(finalView, stopMethod);
    }

    private boolean[] getHasIStreamOnly(boolean[] isNamedWindow, ViewFactoryChain[] unmaterializedViewChain)
    {
        boolean[] result = new boolean[unmaterializedViewChain.length];
        for (int i = 0; i < unmaterializedViewChain.length; i++) {
            if (isNamedWindow[i]) {
                continue;
            }
            result[i] = unmaterializedViewChain[i].getDataWindowViewFactoryCount() == 0;
        }
        return result;
    }

    /**
     * Joins require a remove stream: therefore a view is required for each stream, since all views post a remove stream.
     * <p>
     * If a view is polling or unidirectional, it does not require a view.
     * @param streamSpecs streams
     * @return analysis result
     * @throws ExprValidationException if constraints violated
     */
    private StreamJoinAnalysisResult verifyJoinViews(List<StreamSpecCompiled> streamSpecs)
            throws ExprValidationException
    {
        StreamJoinAnalysisResult analysisResult = new StreamJoinAnalysisResult(streamSpecs.size());
        if (streamSpecs.size() < 2)
        {
            return analysisResult;
        }

        // Determine if any stream has a unidirectional keyword

        // inspect unidirection indicator and named window flags
        int unidirectionalStreamNumber = -1;
        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);
            if (streamSpec.getOptions().isUnidirectional())
            {
                analysisResult.setUnidirectionalInd(i);
                if (unidirectionalStreamNumber != -1)
                {
                    throw new ExprValidationException("The unidirectional keyword can only apply to one stream in a join");
                }
                unidirectionalStreamNumber = i;
            }
            if (!streamSpec.getViewSpecs().isEmpty())
            {
                analysisResult.setHasChildViews(i);
            }
            if (streamSpec instanceof NamedWindowConsumerStreamSpec)
            {
                analysisResult.setNamedWindow(i);
            }
        }
        if ((unidirectionalStreamNumber != -1) && (analysisResult.getHasChildViews()[unidirectionalStreamNumber]))
        {
            throw new ExprValidationException("The unidirectional keyword requires that no views are declared onto the stream");
        }
        analysisResult.setUnidirectionalStreamNumber(unidirectionalStreamNumber);

        // count streams that provide data, excluding streams that poll data (DB and method)
        int countProviderNonpolling = 0;
        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);
            if ((streamSpec instanceof MethodStreamSpec) ||
                (streamSpec instanceof DBStatementStreamSpec))
            {
                continue;
            }
            countProviderNonpolling++;
        }

        // if there is only one stream providing data, the analysis is done 
        if (countProviderNonpolling == 1)
        {
            return analysisResult;
        }
        // there are multiple driving streams, verify the presence of a view for insert/remove stream

        // validation of join views works differently for unidirectional as there can be self-joins that don't require a view
        // see if this is a self-join in which all streams are filters and filter specification is the same. 
        FilterSpecCompiled unidirectionalFilterSpec = null;
        FilterSpecCompiled lastFilterSpec = null;
        boolean pureSelfJoin = true;
        for (StreamSpecCompiled streamSpec : statementSpec.getStreamSpecs())
        {
            if (!(streamSpec instanceof FilterStreamSpecCompiled))
            {
                pureSelfJoin = false;
                continue;
            }

            FilterSpecCompiled filterSpec = ((FilterStreamSpecCompiled) streamSpec).getFilterSpec();
            if ((lastFilterSpec != null) && (!lastFilterSpec.equalsTypeAndFilter(filterSpec)))
            {
                pureSelfJoin = false;
            }
            if (!streamSpec.getViewSpecs().isEmpty())
            {
                pureSelfJoin = false;
            }
            lastFilterSpec = filterSpec;

            if (streamSpec.getOptions().isUnidirectional())
            {
                unidirectionalFilterSpec = filterSpec;
            }
        }        

        // self-join without views and not unidirectional
        if ((pureSelfJoin) && (unidirectionalFilterSpec == null))
        {
            analysisResult.setPureSelfJoin(true);
            return analysisResult;
        }

        // weed out filter and pattern streams that don't have a view in a join
        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            StreamSpecCompiled streamSpec = statementSpec.getStreamSpecs().get(i);
            if (!streamSpec.getViewSpecs().isEmpty())
            {
                continue;
            }

            String name = streamSpec.getOptionalStreamName();
            if ((name == null) && (streamSpec instanceof FilterStreamSpecCompiled))
            {
                name = ((FilterStreamSpecCompiled) streamSpec).getFilterSpec().getFilterForEventTypeName();
            }
            if ((name == null) && (streamSpec instanceof PatternStreamSpecCompiled))
            {
                name = "pattern event stream";
            }

            if (streamSpec.getOptions().isUnidirectional())
            {
                continue;
            }
            // allow a self-join without a child view, in that the filter spec is the same as the unidirection's stream filter
            if ((unidirectionalFilterSpec != null) &&
                (streamSpec instanceof FilterStreamSpecCompiled) &&
                (((FilterStreamSpecCompiled) streamSpec).getFilterSpec().equalsTypeAndFilter(unidirectionalFilterSpec)))
            {
                analysisResult.setUnidirectionalNonDriving(i);
                continue;
            }
            if ((streamSpec instanceof FilterStreamSpecCompiled) ||
                (streamSpec instanceof PatternStreamSpecCompiled))
            {
                throw new ExprValidationException("Joins require that at least one view is specified for each stream, no view was specified for " + name);
            }
        }

        return analysisResult;
    }

    private Pair<Viewable, JoinPreloadMethod> handleJoin(String[] streamNames,
                                                         EventType[] streamTypes,
                                                         Viewable[] streamViews,
                                                         ResultSetProcessor resultSetProcessor,
                                                         SelectClauseStreamSelectorEnum selectStreamSelectorEnum,
                                                         StatementContext statementContext,
                                                         List<StopCallback> stopCallbacks,
                                                         StreamJoinAnalysisResult joinAnalysisResult)
            throws ExprValidationException
    {
        // Handle joins
        final JoinSetComposer composer = statementContext.getJoinSetComposerFactory().makeComposer(statementSpec.getOuterJoinDescList(), statementSpec.getFilterRootNode(), streamTypes, streamNames, streamViews, selectStreamSelectorEnum, joinAnalysisResult, statementContext, queryPlanLogging);

        stopCallbacks.add(new StopCallback(){
            public void stop()
            {
                composer.destroy();
            }
        });

        ExprEvaluator filterEval = statementSpec.getFilterRootNode() == null ? null : statementSpec.getFilterRootNode().getExprEvaluator();
        JoinSetFilter filter = new JoinSetFilter(filterEval);
        OutputProcessView indicatorView = OutputProcessViewFactory.makeView(resultSetProcessor, statementSpec,
                statementContext, services.getInternalEventRouter());

        // Create strategy for join execution
        JoinExecutionStrategy execution = new JoinExecutionStrategyImpl(composer, filter, indicatorView, statementContext);

        // The view needs a reference to the join execution to pull iterator values
        indicatorView.setJoinExecutionStrategy(execution);

        // Hook up dispatchable with buffer and execution strategy
        JoinExecStrategyDispatchable joinStatementDispatch = new JoinExecStrategyDispatchable(execution, statementSpec.getStreamSpecs().size());
        statementContext.getEpStatementHandle().setOptionalDispatchable(joinStatementDispatch);

        JoinPreloadMethod preloadMethod;
        if (joinAnalysisResult.getUnidirectionalStreamNumber() >= 0)
        {
            preloadMethod = new JoinPreloadMethodNull();
        }
        else
        {
            preloadMethod = new JoinPreloadMethodImpl(streamNames.length, composer); 
        }

        // Create buffer for each view. Point buffer to dispatchable for join.
        for (int i = 0; i < statementSpec.getStreamSpecs().size(); i++)
        {
            BufferView buffer = new BufferView(i);
            streamViews[i].addView(buffer);
            buffer.setObserver(joinStatementDispatch);
            preloadMethod.setBuffer(buffer, i);
        }

        return new Pair<Viewable, JoinPreloadMethod>(indicatorView, preloadMethod);
    }

    /**
     * Returns a stream name assigned for each stream, generated if none was supplied.
     * @param streams - stream specifications
     * @return array of stream names
     */
    @SuppressWarnings({"StringContatenationInLoop"})
    protected static String[] determineStreamNames(List<StreamSpecCompiled> streams)
    {
        String[] streamNames = new String[streams.size()];
        for (int i = 0; i < streams.size(); i++)
        {
            // Assign a stream name for joins, if not supplied
            streamNames[i] = streams.get(i).getOptionalStreamName();
            if (streamNames[i] == null)
            {
                streamNames[i] = "stream_" + i;
            }
        }
        return streamNames;
    }

    /**
     * Validate filter and join expression nodes.
     * @param statementSpec the compiled statement
     * @param statementContext the statement services
     * @param typeService the event types for streams
     * @param viewResourceDelegate the delegate to verify expressions that use view resources
     */
    protected static void validateNodes(StatementSpecCompiled statementSpec,
                                        StatementContext statementContext,
                                        StreamTypeService typeService,
                                        ViewResourceDelegate viewResourceDelegate)
    {
        MethodResolutionService methodResolutionService = statementContext.getMethodResolutionService();

        if (statementSpec.getFilterRootNode() != null)
        {
            ExprNode optionalFilterNode = statementSpec.getFilterRootNode();

            // Validate where clause, initializing nodes to the stream ids used
            try
            {
                optionalFilterNode = optionalFilterNode.getValidatedSubtree(typeService, methodResolutionService, viewResourceDelegate, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                statementSpec.setFilterExprRootNode(optionalFilterNode);

                // Make sure there is no aggregation in the where clause
                List<ExprAggregateNode> aggregateNodes = new LinkedList<ExprAggregateNode>();
                ExprAggregateNode.getAggregatesBottomUp(optionalFilterNode, aggregateNodes);
                if (!aggregateNodes.isEmpty())
                {
                    throw new ExprValidationException("An aggregate function may not appear in a WHERE clause (use the HAVING clause)");
                }
            }
            catch (ExprValidationException ex)
            {
                log.debug(".validateNodes Validation exception for filter=" + optionalFilterNode.toExpressionString(), ex);
                throw new EPStatementException("Error validating expression: " + ex.getMessage(), statementContext.getExpression());
            }
        }

        if ((statementSpec.getOutputLimitSpec() != null) && (statementSpec.getOutputLimitSpec().getWhenExpressionNode() != null))
        {
            ExprNode outputLimitWhenNode = statementSpec.getOutputLimitSpec().getWhenExpressionNode();

            // Validate where clause, initializing nodes to the stream ids used
            try
            {
                EventType outputLimitType = OutputConditionExpression.getBuiltInEventType(statementContext.getEventAdapterService());
                StreamTypeService typeServiceOutputWhen = new StreamTypeServiceImpl(new EventType[] {outputLimitType}, new String[]{null}, new boolean[] {true}, statementContext.getEngineURI(), false);
                outputLimitWhenNode = outputLimitWhenNode.getValidatedSubtree(typeServiceOutputWhen, methodResolutionService, null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                statementSpec.getOutputLimitSpec().setWhenExpressionNode(outputLimitWhenNode);

                if (JavaClassHelper.getBoxedType(outputLimitWhenNode.getExprEvaluator().getType()) != Boolean.class)
                {
                    throw new ExprValidationException("The when-trigger expression in the OUTPUT WHEN clause must return a boolean-type value");
                }
                validateNoAggregations(outputLimitWhenNode, "An aggregate function may not appear in a OUTPUT LIMIT clause");

                if (statementSpec.getOutputLimitSpec().getThenExpressions() != null)
                {
                    for (OnTriggerSetAssignment assign : statementSpec.getOutputLimitSpec().getThenExpressions())
                    {
                        ExprNode node = assign.getExpression().getValidatedSubtree(typeServiceOutputWhen, methodResolutionService, null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                        assign.setExpression(node);
                        validateNoAggregations(node, "An aggregate function may not appear in a OUTPUT LIMIT clause");
                    }
                }
            }
            catch (ExprValidationException ex)
            {
                throw new EPStatementException("Error validating expression: " + ex.getMessage(), statementContext.getExpression());
            }
        }

        for (int outerJoinCount = 0; outerJoinCount < statementSpec.getOuterJoinDescList().size(); outerJoinCount++)
        {
            OuterJoinDesc outerJoinDesc = statementSpec.getOuterJoinDescList().get(outerJoinCount);

            UniformPair<Integer> streamIdPair = validateOuterJoinPropertyPair(statementContext, outerJoinDesc.getLeftNode(), outerJoinDesc.getRightNode(), outerJoinCount,
                    typeService, viewResourceDelegate);

            if (outerJoinDesc.getAdditionalLeftNodes() != null)
            {
                Set<Integer> streamSet = new HashSet<Integer>();
                streamSet.add(streamIdPair.getFirst());
                streamSet.add(streamIdPair.getSecond());
                for (int i = 0; i < outerJoinDesc.getAdditionalLeftNodes().length; i++)
                {
                    UniformPair<Integer> streamIdPairAdd = validateOuterJoinPropertyPair(statementContext, outerJoinDesc.getAdditionalLeftNodes()[i], outerJoinDesc.getAdditionalRightNodes()[i], outerJoinCount,
                            typeService, viewResourceDelegate);

                    // make sure all additional properties point to the same two streams
                    if ((!streamSet.contains(streamIdPairAdd.getFirst()) || (!streamSet.contains(streamIdPairAdd.getSecond()))))
                    {
                        String message = "Outer join ON-clause columns must refer to properties of the same joined streams" +
                                " when using multiple columns in the on-clause";
                        throw new EPStatementException("Error validating expression: " + message, statementContext.getExpression());
                    }

                }
            }
        }
    }

    private static void validateNoAggregations(ExprNode exprNode, String errorMsg)
            throws ExprValidationException
    {
        // Make sure there is no aggregation in the where clause
        List<ExprAggregateNode> aggregateNodes = new LinkedList<ExprAggregateNode>();
        ExprAggregateNode.getAggregatesBottomUp(exprNode, aggregateNodes);
        if (!aggregateNodes.isEmpty())
        {
            throw new ExprValidationException(errorMsg);
        }
    }

    private static UniformPair<Integer> validateOuterJoinPropertyPair(
            StatementContext statementContext,
            ExprIdentNode leftNode,
            ExprIdentNode rightNode,
            int outerJoinCount,
            StreamTypeService typeService,
            ViewResourceDelegate viewResourceDelegate)
    {
        // Validate the outer join clause using an artificial equals-node on top.
        // Thus types are checked via equals.
        // Sets stream ids used for validated nodes.
        ExprNode equalsNode = new ExprEqualsNode(false);
        equalsNode.addChildNode(leftNode);
        equalsNode.addChildNode(rightNode);
        try
        {
            equalsNode = equalsNode.getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), viewResourceDelegate, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
        }
        catch (ExprValidationException ex)
        {
            log.debug("Validation exception for outer join node=" + equalsNode.toExpressionString(), ex);
            throw new EPStatementException("Error validating expression: " + ex.getMessage(), statementContext.getExpression());
        }

        // Make sure we have left-hand-side and right-hand-side refering to different streams
        int streamIdLeft = leftNode.getStreamId();
        int streamIdRight = rightNode.getStreamId();
        if (streamIdLeft == streamIdRight)
        {
            String message = "Outer join ON-clause cannot refer to properties of the same stream";
            throw new EPStatementException("Error validating expression: " + message, statementContext.getExpression());
        }

        // Make sure one of the properties refers to the acutual stream currently being joined
        int expectedStreamJoined = outerJoinCount + 1;
        if ((streamIdLeft != expectedStreamJoined) && (streamIdRight != expectedStreamJoined))
        {
            String message = "Outer join ON-clause must refer to at least one property of the joined stream" +
                    " for stream " + expectedStreamJoined;
            throw new EPStatementException("Error validating expression: " + message, statementContext.getExpression());
        }

        // Make sure neither of the streams refer to a 'future' stream
        String badPropertyName = null;
        if (streamIdLeft > outerJoinCount + 1)
        {
            badPropertyName = leftNode.getResolvedPropertyName();
        }
        if (streamIdRight > outerJoinCount + 1)
        {
            badPropertyName = rightNode.getResolvedPropertyName();
        }
        if (badPropertyName != null)
        {
            String message = "Outer join ON-clause invalid scope for property" +
                    " '" + badPropertyName + "', expecting the current or a prior stream scope";
            throw new EPStatementException("Error validating expression: " + message, statementContext.getExpression());
        }

        return new UniformPair<Integer>(streamIdLeft, streamIdRight);
    }


    private Viewable handleSimpleSelect(Viewable view,
                                        ResultSetProcessor resultSetProcessor,
                                        StatementContext statementContext)
            throws ExprValidationException
    {
        Viewable finalView = view;

        // Add filter view that evaluates the filter expression
        if (statementSpec.getFilterRootNode() != null)
        {
            FilterExprView filterView = new FilterExprView(statementSpec.getFilterRootNode().getExprEvaluator(), statementContext);
            finalView.addView(filterView);
            finalView = filterView;
        }

        // for ordered deliver without output limit/buffer
        if (!statementSpec.getOrderByList().isEmpty() && (statementSpec.getOutputLimitSpec() == null)) {
            SingleStreamDispatchView bf = new SingleStreamDispatchView();
            statementContext.getEpStatementHandle().setOptionalDispatchable(bf);
            finalView.addView(bf);
            finalView = bf;
        }

        OutputProcessView selectView = OutputProcessViewFactory.makeView(resultSetProcessor, statementSpec,
                statementContext, services.getInternalEventRouter());

        finalView.addView(selectView);
        finalView = selectView;

        return finalView;
    }

    private SubSelectStreamCollection createSubSelectStreams(boolean isJoin, Annotation[] annotations)
            throws ExprValidationException, ViewProcessingException
    {
        SubSelectStreamCollection subSelectStreamDesc = new SubSelectStreamCollection();
        int subselectStreamNumber = 1024;

        // Process all subselect expression nodes
        for (ExprSubselectNode subselect : statementSpec.getSubSelectExpressions())
        {
            StatementSpecCompiled statementSpec = subselect.getStatementSpecCompiled();

            if (statementSpec.getStreamSpecs().get(0) instanceof FilterStreamSpecCompiled)
            {
                FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) statementSpec.getStreamSpecs().get(0);

                // A child view is required to limit the stream
                if (filterStreamSpec.getViewSpecs().size() == 0)
                {
                    throw new ExprValidationException("Subqueries require one or more views to limit the stream, consider declaring a length or time window");
                }

                subselectStreamNumber++;

                // Register filter, create view factories
                Pair<EventStream, StatementLock> streamLockPair = services.getStreamService().createStream(statementContext.getStatementId(), filterStreamSpec.getFilterSpec(),
                        statementContext.getFilterService(), statementContext.getEpStatementHandle(), isJoin, true, statementContext, false);
                Viewable viewable = streamLockPair.getFirst();
                ViewFactoryChain viewFactoryChain = services.getViewService().createFactories(subselectStreamNumber, viewable.getEventType(), filterStreamSpec.getViewSpecs(), filterStreamSpec.getOptions(), statementContext);
                subselect.setRawEventType(viewFactoryChain.getEventType());

                // Add lookup to list, for later starts
                subSelectStreamDesc.add(subselect, subselectStreamNumber, viewable, viewFactoryChain);
            }
            else
            {
                NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) statementSpec.getStreamSpecs().get(0);
                NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());

                // if named-window index sharing is disabled (the default) or filter expressions are provided then consume the insert-remove stream
                boolean disableIndexShare = HintEnum.DISABLE_WINDOW_SUBQUERY_INDEXSHARE.getHint(annotations) != null;
                if (!namedSpec.getFilterExpressions().isEmpty() || !processor.isEnableSubqueryIndexShare() || disableIndexShare) {
                    NamedWindowConsumerView consumerView = processor.addConsumer(namedSpec.getFilterExpressions(), namedSpec.getOptPropertyEvaluator(), statementContext.getEpStatementHandle(), statementContext.getStatementStopService());
                    ViewFactoryChain viewFactoryChain = services.getViewService().createFactories(0, consumerView.getEventType(), namedSpec.getViewSpecs(), namedSpec.getOptions(), statementContext);
                    subselect.setRawEventType(viewFactoryChain.getEventType());
                    subSelectStreamDesc.add(subselect, subselectStreamNumber, consumerView, viewFactoryChain);
                }
                // else if there are no named window stream filter expressions and index sharing is enabled
                else {
                    ViewFactoryChain viewFactoryChain = services.getViewService().createFactories(0, processor.getNamedWindowType(), namedSpec.getViewSpecs(), namedSpec.getOptions(), statementContext);
                    subselect.setRawEventType(processor.getNamedWindowType());
                    subSelectStreamDesc.add(subselect, subselectStreamNumber, null, viewFactoryChain);
                }
            }
        }

        return subSelectStreamDesc;
    }

    private void startSubSelect(SubSelectStreamCollection subSelectStreamDesc, String[] outerStreamNames, EventType[] outerEventTypesSelect, String[] outerEventTypeNamees, List<StopCallback> stopCallbacks, Annotation[] annotations)
            throws ExprValidationException
    {
        boolean fullTableScan = HintEnum.SET_NOINDEX.getHint(annotations) != null;
        int subqueryNum = 0;
        for (ExprSubselectNode subselect : statementSpec.getSubSelectExpressions())
        {
            if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
                queryPlanLog.info("For statement '" + statementContext.getStatementName() + "' subquery " + subqueryNum);
            }

            StatementSpecCompiled statementSpec = subselect.getStatementSpecCompiled();
            StreamSpecCompiled filterStreamSpec = statementSpec.getStreamSpecs().get(0);

            String subselecteventTypeName = null;
            if (filterStreamSpec instanceof FilterStreamSpecCompiled)
            {
                subselecteventTypeName = ((FilterStreamSpecCompiled) filterStreamSpec).getFilterSpec().getFilterForEventTypeName();
            }
            else if (filterStreamSpec instanceof NamedWindowConsumerStreamSpec)
            {
                subselecteventTypeName = ((NamedWindowConsumerStreamSpec) filterStreamSpec).getWindowName();
            }

            ViewFactoryChain viewFactoryChain = subSelectStreamDesc.getViewFactoryChain(subselect);
            EventType eventType = viewFactoryChain.getEventType();

            // determine a stream name unless one was supplied
            String subexpressionStreamName = filterStreamSpec.getOptionalStreamName();
            int subselectStreamNumber = subSelectStreamDesc.getStreamNumber(subselect);
            if (subexpressionStreamName == null)
            {
                subexpressionStreamName = "$subselect_" + subselectStreamNumber;
            }

            // Named windows don't allow data views
            if (filterStreamSpec instanceof NamedWindowConsumerStreamSpec)
            {
                ViewResourceDelegate viewResourceDelegate = new ViewResourceDelegateImpl(new ViewFactoryChain[] {viewFactoryChain}, statementContext);
                viewResourceDelegate.requestCapability(0, new NotADataWindowViewCapability(), null);
            }

            EventType[] outerEventTypes;
            StreamTypeService subselectTypeService;

            // Use the override provided by the subselect if applicable
            if (subselect.getFilterSubqueryStreamTypes() != null) {
                subselectTypeService = subselect.getFilterSubqueryStreamTypes();
                outerEventTypes = new EventType[subselectTypeService.getEventTypes().length - 1];
                System.arraycopy(subselectTypeService.getEventTypes(), 1, outerEventTypes, 0, subselectTypeService.getEventTypes().length - 1);
            }
            else {
                // Streams event types are the original stream types with the stream zero the subselect stream
                LinkedHashMap<String, Pair<EventType, String>> namesAndTypes = new LinkedHashMap<String, Pair<EventType, String>>();
                namesAndTypes.put(subexpressionStreamName, new Pair<EventType, String>(eventType, subselecteventTypeName));
                for (int i = 0; i < outerEventTypesSelect.length; i++)
                {
                    Pair<EventType, String> pair = new Pair<EventType, String>(outerEventTypesSelect[i], outerEventTypeNamees[i]);
                    namesAndTypes.put(outerStreamNames[i], pair);
                }
                subselectTypeService = new StreamTypeServiceImpl(namesAndTypes, services.getEngineURI(), true, true);
                outerEventTypes = outerEventTypesSelect;
            }
            ViewResourceDelegate viewResourceDelegateSubselect = new ViewResourceDelegateImpl(new ViewFactoryChain[] {viewFactoryChain}, statementContext);

            // Validate select expression
            SelectClauseSpecCompiled selectClauseSpec = subselect.getStatementSpecCompiled().getSelectClauseSpec();
            AggregationService aggregationService = null;
            List<ExprNode> selectExpressions = new ArrayList<ExprNode>();
            List<String> assignedNames = new ArrayList<String>();
            boolean isWildcard = false;
            boolean isStreamWildcard = false;
            if (selectClauseSpec.getSelectExprList().size() > 0)
            {
                List<ExprAggregateNode> aggExprNodes = new LinkedList<ExprAggregateNode>();

                for (int i = 0; i < selectClauseSpec.getSelectExprList().size(); i++) {
                    SelectClauseElementCompiled element = selectClauseSpec.getSelectExprList().get(i);

                    if (element instanceof SelectClauseExprCompiledSpec)
                    {
                        // validate
                        SelectClauseExprCompiledSpec compiled = (SelectClauseExprCompiledSpec) element;
                        ExprNode selectExpression = compiled.getSelectExpression();
                        selectExpression = selectExpression.getValidatedSubtree(subselectTypeService, statementContext.getMethodResolutionService(), viewResourceDelegateSubselect, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);

                        selectExpressions.add(selectExpression);
                        assignedNames.add(compiled.getAssignedName());

                        // handle aggregation
                        ExprAggregateNode.getAggregatesBottomUp(selectExpression, aggExprNodes);

                        if (aggExprNodes.size() > 0)
                        {
                            // This stream (stream 0) properties must either all be under aggregation, or all not be.
                            List<Pair<Integer, String>> propertiesNotAggregated = getExpressionProperties(selectExpression, false);
                            for (Pair<Integer, String> pair : propertiesNotAggregated)
                            {
                                if (pair.getFirst() == 0)
                                {
                                    throw new ExprValidationException("Subselect properties must all be within aggregation functions");
                                }
                            }
                        }
                    }
                    else if (element instanceof SelectClauseElementWildcard) {
                        isWildcard = true;
                    }
                    else if (element instanceof SelectClauseStreamCompiledSpec) {
                        isStreamWildcard = true;
                    }
                }   // end of for loop

                if (!selectExpressions.isEmpty()) {
                    subselect.setSelectClause(selectExpressions.toArray(new ExprNode[selectExpressions.size()]));
                    subselect.setSelectAsNames(assignedNames.toArray(new String[assignedNames.size()]));
                    if (isWildcard || isStreamWildcard) {
                        throw new ExprValidationException("Subquery multi-column select does not allow wildcard or stream wildcard when selecting multiple columns.");
                    }
                    if (selectExpressions.size() > 1 && !subselect.isAllowMultiColumnSelect()) {
                        throw new ExprValidationException("Subquery multi-column select is not allowed in this context.");
                    }
                    if ((selectExpressions.size() > 1 && aggExprNodes.size() > 0)) {
                        // all properties must be aggregated
                        if (!ExprNodeUtility.getNonAggregatedProps(selectExpressions).isEmpty()) {
                            throw new ExprValidationException("Subquery with multi-column select requires that either all or none of the selected columns are under aggregation.");
                        }
                    }
                }

                if (aggExprNodes.size() > 0)
                {
                    List<ExprAggregateNode> havingAgg = Collections.emptyList();
                    List<ExprAggregateNode> orderByAgg = Collections.emptyList();
                    aggregationService = AggregationServiceFactory.getService(aggExprNodes, havingAgg, orderByAgg, false, statementContext.getMethodResolutionService(), statementContext, annotations, statementContext.getVariableService(), statementContext.getStatementStopService(), false, statementSpec.getFilterRootNode(), statementSpec.getHavingExprRootNode());

                    // Other stream properties, if there is aggregation, cannot be under aggregation.
                    for (ExprAggregateNode aggNode : aggExprNodes)
                    {
                        List<Pair<Integer, String>> propertiesNodesAggregated = getExpressionProperties(aggNode, true);
                        for (Pair<Integer, String> pair : propertiesNodesAggregated)
                        {
                            if (pair.getFirst() != 0)
                            {
                                throw new ExprValidationException("Subselect aggregation functions cannot aggregate across correlated properties");
                            }
                        }
                    }
                }
            }

            // no aggregation functions allowed in filter
            if (statementSpec.getFilterRootNode() != null)
            {
                List<ExprAggregateNode> aggExprNodesFilter = new LinkedList<ExprAggregateNode>();
                ExprAggregateNode.getAggregatesBottomUp(statementSpec.getFilterRootNode(), aggExprNodesFilter);
                if (aggExprNodesFilter.size() > 0)
                {
                    throw new ExprValidationException("Aggregation functions are not supported within subquery filters, consider using insert-into instead");
                }
            }

            // Validate filter expression, if there is one
            ExprNode filterExpr = statementSpec.getFilterRootNode();
            boolean correlatedSubquery = false;
            if (filterExpr != null)
            {
                filterExpr = filterExpr.getValidatedSubtree(subselectTypeService, statementContext.getMethodResolutionService(), viewResourceDelegateSubselect, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                if (JavaClassHelper.getBoxedType(filterExpr.getExprEvaluator().getType()) != Boolean.class)
                {
                    throw new ExprValidationException("Subselect filter expression must return a boolean value");
                }

                // check the presence of a correlated filter, not allowed with aggregation
                ExprNodeIdentifierVisitor visitor = new ExprNodeIdentifierVisitor(true);
                filterExpr.accept(visitor);
                List<Pair<Integer, String>> propertiesNodes = visitor.getExprProperties();
                for (Pair<Integer, String> pair : propertiesNodes)
                {
                    if (pair.getFirst() != 0)
                    {
                        correlatedSubquery = true;
                        break;
                    }
                }
            }

            // Finally create views
            Viewable viewableRoot = subSelectStreamDesc.getRootViewable(subselect);
            Viewable subselectView = services.getViewService().createViews(viewableRoot, viewFactoryChain.getViewFactoryChain(), statementContext);

            // If we do aggregation, then the view results must be added and removed from aggregation
            final EventTable eventIndex;
            // Under aggregation conditions, there is no lookup/corelated subquery strategy, and
            // the view-supplied events are simply aggregated, a null-event supplied to the stream for the select-clause, and not kept in index.
            // Note that "var1 + max(var2)" is not allowed as some properties are not under aggregation (which event to use?).
            if (aggregationService != null)
            {
                subselect.setStrategy(new TableLookupStrategyNullRow());
                subselect.setFilterExpr(null);      // filter not evaluated by subselect expression as not correlated
                ExprEvaluator filterExprEval = (filterExpr == null) ? null : filterExpr.getExprEvaluator();

                if (!correlatedSubquery) {
                    SubselectAggregatorView aggregatorView = new SubselectAggregatorView(aggregationService, filterExprEval, statementContext);
                    subselectView.addView(aggregatorView);
                    subselectView = aggregatorView;
                    eventIndex = null;
                }
                else {
                    Pair<EventTable, TableLookupStrategy> indexPair = determineSubqueryIndex(filterExpr, eventType,
                            outerEventTypes, subselectTypeService, fullTableScan);
                    subselect.setStrategy(indexPair.getSecond());
                    subselect.setFilterExpr(null);  // this will be evaluated in the preprocessor
                    eventIndex = indexPair.getFirst();

                    SubselectAggregationPreprocessor preprocessor = new SubselectAggregationPreprocessor(aggregationService, filterExpr.getExprEvaluator());
                    subselect.setSubselectAggregationPreprocessor(preprocessor);
                }
            }
            else
            {
                // Determine indexing of the filter expression
                Pair<EventTable, TableLookupStrategy> indexPair = determineSubqueryIndex(filterExpr, eventType,
                        outerEventTypes, subselectTypeService, fullTableScan);
                ExprEvaluator filterExprEval = (filterExpr == null) ? null : filterExpr.getExprEvaluator();
                subselect.setStrategy(indexPair.getSecond());
                subselect.setFilterExpr(filterExprEval);
                eventIndex = indexPair.getFirst();
            }

            boolean disableIndexShare = HintEnum.DISABLE_WINDOW_SUBQUERY_INDEXSHARE.getHint(annotations) != null;
            TableLookupStrategy namedWindowSubqueryLookup = null;
            if ((filterStreamSpec instanceof NamedWindowConsumerStreamSpec) && (!disableIndexShare)) {
                NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) filterStreamSpec;
                if (namedSpec.getFilterExpressions().isEmpty()) {
                    NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());
                    if (processor.isEnableSubqueryIndexShare()) {
                        JoinedPropPlan joinedPropPlan = getJoinProps(filterExpr, outerEventTypes, subselectTypeService);
                        namedWindowSubqueryLookup = processor.getRootView().getAddSubqueryLookupStrategy(outerEventTypesSelect, joinedPropPlan, fullTableScan);
                        subselect.setStrategy(namedWindowSubqueryLookup);
                        stopCallbacks.add(new NamedWindowSubqueryStopCallback(processor, namedWindowSubqueryLookup));
                    }
                }
            }

            // Clear out index on statement stop
            if (namedWindowSubqueryLookup == null) {
                stopCallbacks.add(new SubqueryStopCallback(eventIndex));
            }

            // Preload
            if (namedWindowSubqueryLookup == null) {
                if (filterStreamSpec instanceof NamedWindowConsumerStreamSpec) 
                {
                    NamedWindowConsumerStreamSpec namedSpec = (NamedWindowConsumerStreamSpec) filterStreamSpec ;
                    NamedWindowProcessor processor = services.getNamedWindowService().getProcessor(namedSpec.getWindowName());
                    NamedWindowTailView consumerView = processor.getTailView();

                    // preload view for stream
                    ArrayList<EventBean> eventsInWindow = new ArrayList<EventBean>();
                    if (namedSpec.getFilterExpressions() != null) {
                        EventBean[] events = new EventBean[1];
                        for (EventBean event : consumerView) {
                            events[0] = event;
                            boolean add = true;
                            for (ExprNode filter : namedSpec.getFilterExpressions()) {
                                Object result = filter.getExprEvaluator().evaluate(events, true, statementContext);
                                if ((result == null) || (!((Boolean) result))) {
                                    add = false;
                                    break;
                                }
                            }
                            if (add) {
                                eventsInWindow.add(events[0]);
                            }
                        }
                    }
                    else {
                        for(Iterator<EventBean> it = consumerView.iterator(); it.hasNext();)
                        {
                            eventsInWindow.add(it.next());
                        }
                    }
                    EventBean[] newEvents = eventsInWindow.toArray(new EventBean[eventsInWindow.size()]);
                    ((View)viewableRoot).update(newEvents, null); // fill view
                    if (eventIndex != null)
                    {
                        eventIndex.add(newEvents);  // fill index
                    }
                }
                else        // preload from the data window that sit on top
                {
                    // Start up event table from the iterator
                    Iterator<EventBean> it = subselectView.iterator();
                    if ((it != null) && (it.hasNext()))
                    {
                        ArrayList<EventBean> preloadEvents = new ArrayList<EventBean>();
                        for (;it.hasNext();)
                        {
                            preloadEvents.add(it.next());
                        }
                        if (eventIndex != null)
                        {
                            eventIndex.add(preloadEvents.toArray(new EventBean[preloadEvents.size()]));
                        }
                    }
                }

                subqueryNum++;
            }

            // hook up subselect viewable and event table
            if (subselectView != null) {
                BufferView bufferView = new BufferView(subselectStreamNumber);
                bufferView.setObserver(new SubselectBufferObserver(eventIndex));
                subselectView.addView(bufferView);
            }
        }
    }

    private JoinedPropPlan getJoinProps(ExprNode filterExpr, EventType[] outerEventTypes, StreamTypeService subselectTypeService)
    {
        // No filter expression means full table scan
        if (filterExpr == null)
        {
            return new JoinedPropPlan(Collections.<String, JoinedPropDesc>emptyMap(), false);
        }

        // analyze query graph
        QueryGraph queryGraph = new QueryGraph(outerEventTypes.length + 1);
        FilterExprAnalyzer.analyze(filterExpr, queryGraph);

        // Build a list of streams and indexes
        Map<String, JoinedPropDesc> joinProps = new LinkedHashMap<String, JoinedPropDesc>();
        boolean mustCoerce = false;
        for (int stream = 0; stream <  outerEventTypes.length; stream++)
        {
            int lookupStream = stream + 1;
            String[] keyPropertiesJoin = queryGraph.getKeyProperties(lookupStream, 0);
            String[] indexPropertiesJoin = queryGraph.getIndexProperties(lookupStream, 0);
            if ((keyPropertiesJoin == null) || (keyPropertiesJoin.length == 0))
            {
                continue;
            }
            if (keyPropertiesJoin.length != indexPropertiesJoin.length)
            {
                throw new IllegalStateException("Invalid query key and index property collection for stream " + stream);
            }

            for (int i = 0; i < keyPropertiesJoin.length; i++)
            {
                Class keyPropType = JavaClassHelper.getBoxedType(subselectTypeService.getEventTypes()[lookupStream].getPropertyType(keyPropertiesJoin[i]));
                Class indexedPropType = JavaClassHelper.getBoxedType(subselectTypeService.getEventTypes()[0].getPropertyType(indexPropertiesJoin[i]));
                Class coercionType = indexedPropType;
                if (keyPropType != indexedPropType)
                {
                    coercionType = JavaClassHelper.getCompareToCoercionType(keyPropType, indexedPropType);
                    mustCoerce = true;
                }

                JoinedPropDesc desc = new JoinedPropDesc(indexPropertiesJoin[i],
                        coercionType, keyPropertiesJoin[i], stream);
                joinProps.put(indexPropertiesJoin[i], desc);
            }
        }
        return new JoinedPropPlan(joinProps, mustCoerce);
    }

    private Pair<EventTable, TableLookupStrategy> determineSubqueryIndex(ExprNode filterExpr,
                                                                                 EventType viewableEventType,
                                                                                 EventType[] outerEventTypes,
                                                                                 StreamTypeService subselectTypeService,
                                                                                 boolean fullTableScan)
            throws ExprValidationException
    {
        // No filter expression means full table scan
        if ((filterExpr == null) || fullTableScan)
        {
            UnindexedEventTable table = new UnindexedEventTable(0);
            FullTableScanLookupStrategy strategy = new FullTableScanLookupStrategy(table);
            if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
                queryPlanLog.info("local buf, full table scan");
            }
            return new Pair<EventTable, TableLookupStrategy>(table, strategy);
        }

        // Build a list of streams and indexes
        JoinedPropPlan joinPropDesc = getJoinProps(filterExpr, outerEventTypes, subselectTypeService);
        Map<String, JoinedPropDesc> joinProps = joinPropDesc.getJoinProps();

        if (joinProps.size() != 0)
        {
            String indexedProps[] = joinProps.keySet().toArray(new String[joinProps.keySet().size()]);
            int[] keyStreamNums = JoinedPropDesc.getKeyStreamNums(joinProps.values());
            String[] keyProps = JoinedPropDesc.getKeyProperties(joinProps.values());
            Class coercionTypes[] = JoinedPropDesc.getCoercionTypes(joinProps.values());

            if (!joinPropDesc.isMustCoerce())
            {
                PropertyIndexedEventTable table = new PropertyIndexedEventTable(0, viewableEventType, indexedProps, coercionTypes);
                TableLookupStrategy strategy = new IndexedTableLookupStrategy( outerEventTypes,
                        keyStreamNums, keyProps, table);
                if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
                    queryPlanLog.info("local index, index lookup on " + Arrays.toString(indexedProps) + " based on " + Arrays.toString(keyProps));
                }
                return new Pair<EventTable, TableLookupStrategy>(table, strategy);
            }
            else
            {                
                PropertyIndTableCoerceAdd table = new PropertyIndTableCoerceAdd(0, viewableEventType, indexedProps, coercionTypes);
                TableLookupStrategy strategy = new IndexedTableLookupStrategyCoercing( outerEventTypes, keyStreamNums, keyProps, table, coercionTypes);
                if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
                    queryPlanLog.info("local index, coerced index lookup on " + Arrays.toString(indexedProps) + " based on " + Arrays.toString(keyProps));
                }
                return new Pair<EventTable, TableLookupStrategy>(table, strategy);
            }
        }
        else
        {
            UnindexedEventTable table = new UnindexedEventTable(0);
            if (queryPlanLogging && queryPlanLog.isInfoEnabled()) {
                queryPlanLog.info("local buf, full table scan");
            }
            return new Pair<EventTable, TableLookupStrategy>(table, new FullTableScanLookupStrategy(table));
        }
    }

    // For delete actions from named windows
    private ExprNode validateJoinNamedWindow(ExprNode deleteJoinExpr,
                                         EventType namedWindowType,
                                         String namedWindowStreamName,
                                         String namedWindowName,
                                         EventType filteredType,
                                         String filterStreamName,
                                         String filteredTypeName) throws ExprValidationException
    {
        if (deleteJoinExpr == null)
        {
            return null;
        }

        LinkedHashMap<String, Pair<EventType, String>> namesAndTypes = new LinkedHashMap<String, Pair<EventType, String>>();
        namesAndTypes.put(namedWindowStreamName, new Pair<EventType, String>(namedWindowType, namedWindowName));
        namesAndTypes.put(filterStreamName, new Pair<EventType, String>(filteredType, filteredTypeName));
        StreamTypeService typeService = new StreamTypeServiceImpl(namesAndTypes, services.getEngineURI(), false, false);

        return deleteJoinExpr.getValidatedSubtree(typeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
    }

    /**
     * Walk expression returning properties used.
     * @param exprNode to walk
     * @param visitAggregateNodes true to visit aggregation nodes
     * @return list of props
     */
    public static List<Pair<Integer, String>> getExpressionProperties(ExprNode exprNode, boolean visitAggregateNodes)
    {
        ExprNodeIdentifierVisitor visitor = new ExprNodeIdentifierVisitor(visitAggregateNodes);
        exprNode.accept(visitor);
        return visitor.getExprProperties();
    }
}

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
import com.espertech.esper.client.annotation.Hint;
import com.espertech.esper.client.annotation.Name;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.annotation.AnnotationUtil;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.StreamTypeServiceImpl;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.named.NamedWindowService;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.event.NativeEventType;
import com.espertech.esper.event.map.MapEventType;
import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.filter.FilterSpecParam;
import com.espertech.esper.pattern.EvalFilterNode;
import com.espertech.esper.pattern.EvalNode;
import com.espertech.esper.pattern.EvalNodeAnalysisResult;
import com.espertech.esper.util.ManagedReadWriteLock;
import com.espertech.esper.util.UuidGenerator;
import com.espertech.esper.view.ViewProcessingException;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Provides statement lifecycle services.
 */
public class StatementLifecycleSvcImpl implements StatementLifecycleSvc
{
    private static Log log = LogFactory.getLog(StatementLifecycleSvcImpl.class);

    /**
     * Services context for statement lifecycle management.
     */
    protected final EPServicesContext services;

    /**
     * Maps of statement id to descriptor.
     */
    protected final Map<String, EPStatementDesc> stmtIdToDescMap;

    /**
     * Map of statement name to statement.
     */
    protected final Map<String, EPStatement> stmtNameToStmtMap;

    private final EPServiceProviderSPI epServiceProvider;
    private final ManagedReadWriteLock eventProcessingRWLock;

    private final Map<String, String> stmtNameToIdMap;

    // Observers to statement-related events
    private final Set<StatementLifecycleObserver> observers;

    /**
     * Ctor.
     * @param epServiceProvider is the engine instance to hand to statement-aware listeners
     * @param services is engine services
     */
    public StatementLifecycleSvcImpl(EPServiceProvider epServiceProvider, EPServicesContext services)
    {
        this.services = services;
        this.epServiceProvider = (EPServiceProviderSPI) epServiceProvider;

        // lock for starting and stopping statements
        this.eventProcessingRWLock = services.getEventProcessingRWLock();

        this.stmtIdToDescMap = new HashMap<String, EPStatementDesc>();
        this.stmtNameToStmtMap = new HashMap<String, EPStatement>();
        this.stmtNameToIdMap = new LinkedHashMap<String, String>();

        observers = new CopyOnWriteArraySet<StatementLifecycleObserver>();
    }

    public void addObserver(StatementLifecycleObserver observer)
    {
        observers.add(observer);
    }

    public void removeObserver(StatementLifecycleObserver observer)
    {
        observers.remove(observer);
    }

    public void destroy()
    {
        this.destroyAllStatements();
    }

    public void init()
    {
        // called after services are activated, to begin statement loading from store
    }

    public synchronized EPStatement createAndStart(StatementSpecRaw statementSpec, String expression, boolean isPattern, String optStatementName, Object userObject, EPIsolationUnitServices isolationUnitServices)
    {
        // Generate statement id
        String statementId = UuidGenerator.generate();
        return createAndStart(statementSpec, expression, isPattern, optStatementName, statementId, null, userObject, isolationUnitServices);
    }

    /**
     * Creates and starts statement.
     * @param statementSpec defines the statement
     * @param expression is the EPL
     * @param isPattern is true for patterns
     * @param optStatementName is the optional statement name
     * @param statementId is the statement id
     * @param optAdditionalContext additional context for use by the statement context
     * @param userObject the application define user object associated to each statement, if supplied
     * @param isolationUnitServices isolated service services
     * @return started statement
     */
    protected synchronized EPStatement createAndStart(StatementSpecRaw statementSpec, String expression, boolean isPattern, String optStatementName, String statementId, Map<String, Object> optAdditionalContext, Object userObject, EPIsolationUnitServices isolationUnitServices)
    {
        boolean nameProvided = false;
        String statementName = statementId;

        // find name annotation
        if (optStatementName == null) {
            if ((statementSpec.getAnnotations() != null) && (!statementSpec.getAnnotations().isEmpty()))
            {
                for (AnnotationDesc desc : statementSpec.getAnnotations())
                {
                    if ((desc.getName().equals(Name.class.getSimpleName())) || (desc.getName().equals(Name.class.getName())))
                    {
                        if (desc.getAttributes().get(0) != null)
                        {
                            optStatementName = desc.getAttributes().get(0).getSecond().toString();
                        }
                    }
                }
            }
        }

        // Determine a statement name, i.e. use the id or use/generate one for the name passed in
        if (optStatementName != null)
        {
            statementName = getUniqueStatementName(optStatementName, statementId);
            nameProvided = true;
        }

        EPStatementDesc desc = createStopped(statementSpec, expression, isPattern, statementName, nameProvided, statementId, optAdditionalContext, userObject, isolationUnitServices, false);
        start(statementId, desc, true, false, false);
        return desc.getEpStatement();
    }

    /**
     * Create stopped statement.
     * @param statementSpec - statement definition
     * @param expression is the expression text
     * @param isPattern is true for patterns, false for non-patterns
     * @param statementName is the statement name assigned or given
     * @param statementId is the statement id
     * @param optAdditionalContext additional context for use by the statement context
     * @param userObject the application define user object associated to each statement, if supplied
     * @param isolationUnitServices isolated service services
     * @param isFailed to start the statement in failed state
     * @param nameProvided true when an explicit statement name is provided
     * @return stopped statement
     */
    protected synchronized EPStatementDesc createStopped(StatementSpecRaw statementSpec,
                                                         String expression,
                                                         boolean isPattern,
                                                         String statementName,
                                                         boolean nameProvided,
                                                         String statementId,
                                                         Map<String, Object> optAdditionalContext,
                                                         Object userObject,
                                                         EPIsolationUnitServices isolationUnitServices,
                                                         boolean isFailed)
    {
        EPStatementDesc statementDesc;
        EPStatementStartMethod startMethod;

        Annotation[] annotations = AnnotationUtil.compileAnnotations(statementSpec.getAnnotations(), services.getEngineImportService(), expression);
        if (annotations != null)
        {
            for (Annotation annotation : annotations)
            {
                if (annotation instanceof Hint)
                {
                    statementSpec.setHasVariables(true);
                }
            }
        }
        
        StatementContext statementContext =  services.getStatementContextFactory().makeContext(statementId, statementName, expression, statementSpec.isHasVariables(), services, optAdditionalContext, statementSpec.getOnTriggerDesc(), statementSpec.getCreateWindowDesc(), false, annotations, isolationUnitServices);
        StatementSpecCompiled compiledSpec;
        try
        {
            compiledSpec = compile(statementSpec, expression, statementContext, false, annotations);
        }
        catch (EPStatementException ex)
        {
            stmtNameToIdMap.remove(statementName); // Clean out the statement name as it's already assigned
            throw ex;
        }

        // For insert-into streams, create a lock taken out as soon as an event is inserted
        // Makes the processing between chained statements more predictable.
        if (statementSpec.getInsertIntoDesc() != null)
        {
            String insertIntoStreamName = statementSpec.getInsertIntoDesc().getEventTypeName();
            String latchFactoryNameBack = "insert_stream_B_" + insertIntoStreamName + "_" + statementId;
            String latchFactoryNameFront = "insert_stream_F_" + insertIntoStreamName + "_" + statementId;
            long msecTimeout = services.getEngineSettingsService().getEngineSettings().getThreading().getInsertIntoDispatchTimeout();
            ConfigurationEngineDefaults.Threading.Locking locking = services.getEngineSettingsService().getEngineSettings().getThreading().getInsertIntoDispatchLocking();
            InsertIntoLatchFactory latchFactoryFront = new InsertIntoLatchFactory(latchFactoryNameFront, msecTimeout, locking, services.getTimeSource());
            InsertIntoLatchFactory latchFactoryBack = new InsertIntoLatchFactory(latchFactoryNameBack, msecTimeout, locking, services.getTimeSource());
            statementContext.getEpStatementHandle().setInsertIntoFrontLatchFactory(latchFactoryFront);
            statementContext.getEpStatementHandle().setInsertIntoBackLatchFactory(latchFactoryBack);
        }

        // In a join statements if the same event type or it's deep super types are used in the join more then once,
        // then this is a self-join and the statement handle must know to dispatch the results together
        boolean canSelfJoin = isPotentialSelfJoin(compiledSpec);
        statementContext.getEpStatementHandle().setCanSelfJoin(canSelfJoin);

        // add statically typed event type references: those in the from clause; Dynamic (created) types collected by statement context and added on start
        services.getStatementEventTypeRefService().addReferences(statementName, compiledSpec.getEventTypeReferences());

        // add variable references
        services.getStatementVariableRefService().addReferences(statementName, compiledSpec.getVariableReferences());

        // determine statement type
        StatementType statementType = null;
        if (statementSpec.getCreateVariableDesc() != null) {
            statementType = StatementType.CREATE_VARIABLE;
        }
        else if (statementSpec.getCreateWindowDesc() != null) {
            statementType = StatementType.CREATE_WINDOW;
        }
        else if (statementSpec.getOnTriggerDesc() != null) {
            if (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_DELETE) {
                statementType = StatementType.ON_DELETE;
            }
            else if (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_UPDATE) {
                statementType = StatementType.ON_UPDATE;
            }
            else if (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_SELECT) {
                if (statementSpec.getInsertIntoDesc() != null) {
                    statementType = StatementType.ON_INSERT;
                }
                else {
                    statementType = StatementType.ON_SELECT;
                }
            }
            else if (statementSpec.getOnTriggerDesc().getOnTriggerType() == OnTriggerType.ON_SET) {
                statementType = StatementType.ON_SET;
            }
        }
        else if (statementSpec.getInsertIntoDesc() != null) {
            statementType = StatementType.INSERT_INTO;
        }
        else if (isPattern) {
            statementType = StatementType.PATTERN;
        }
        else if (statementSpec.getUpdateDesc() != null) {
            statementType = StatementType.UPDATE;
        }
        else if (statementSpec.getCreateIndexDesc() != null) {
            statementType = StatementType.CREATE_INDEX;
        }
        else if (statementSpec.getCreateSchemaDesc() != null) {
            statementType = StatementType.CREATE_SCHEMA;
        }
        if (statementType == null) {
            statementType = StatementType.SELECT;
        }

        eventProcessingRWLock.acquireWriteLock();
        try
        {
            // create statement - may fail for parser and simple validation errors
            boolean preserveDispatchOrder = services.getEngineSettingsService().getEngineSettings().getThreading().isListenerDispatchPreserveOrder();
            boolean isSpinLocks = services.getEngineSettingsService().getEngineSettings().getThreading().getListenerDispatchLocking() == ConfigurationEngineDefaults.Threading.Locking.SPIN;
            long blockingTimeout = services.getEngineSettingsService().getEngineSettings().getThreading().getListenerDispatchTimeout();
            long timeLastStateChange = services.getSchedulingService().getTime();
            EPStatementSPI statement = new EPStatementImpl(statementId, statementName, expression, statementSpec.getExpressionNoAnnotations(), isPattern,
                    services.getDispatchService(), this, timeLastStateChange, preserveDispatchOrder, isSpinLocks, blockingTimeout,
                    services.getTimeSource(), new StatementMetadata(statementType), userObject, compiledSpec.getAnnotations(), statementContext, isFailed, nameProvided);

            boolean isInsertInto = statementSpec.getInsertIntoDesc() != null;
            boolean isDistinct = statementSpec.getSelectClauseSpec().isDistinct();
            boolean isForClause = statementSpec.getForClauseSpec() != null;
            statementContext.getStatementResultService().setContext(statement, epServiceProvider,
                    isInsertInto, isPattern, isDistinct, isForClause, statementContext.getEpStatementHandle().getMetricsHandle());

            // create start method
            startMethod = new EPStatementStartMethod(compiledSpec, services, statementContext);

            // keep track of the insert-into statements supplying streams.
            // these may need to lock to get more predictable behavior for multithreaded processing.
            String insertIntoStreamName = null;
            if (statementSpec.getInsertIntoDesc() != null)
            {
                insertIntoStreamName = statementSpec.getInsertIntoDesc().getEventTypeName();
            }

            statementDesc = new EPStatementDesc(statement, startMethod, null, null, insertIntoStreamName, statementContext.getEpStatementHandle(), statementContext);
            stmtIdToDescMap.put(statementId, statementDesc);
            stmtNameToStmtMap.put(statementName, statement);
            stmtNameToIdMap.put(statementName, statementId);

            dispatchStatementLifecycleEvent(new StatementLifecycleEvent(statement, StatementLifecycleEvent.LifecycleEventType.CREATE));
        }
        catch (RuntimeException ex)
        {
            stmtIdToDescMap.remove(statementId);
            stmtNameToIdMap.remove(statementName);
            stmtNameToStmtMap.remove(statementName);
            throw ex;
        }
        finally
        {
            eventProcessingRWLock.releaseWriteLock();
        }

        return statementDesc;
    }

    private boolean isPotentialSelfJoin(StatementSpecCompiled spec)
    {
        // if order-by is specified, ans since multiple output rows may produce, ensure dispatch
        if (!spec.getOrderByList().isEmpty())
        {
            return true;
        }

        for (StreamSpecCompiled streamSpec : spec.getStreamSpecs())
        {
            if (streamSpec instanceof PatternStreamSpecCompiled)
            {
                return true;
            }
        }

        // not a self join
        if ((spec.getStreamSpecs().size() <= 1) && (spec.getSubSelectExpressions().isEmpty()))
        {
            return false;
        }

        // join - determine types joined
        List<EventType> filteredTypes = new ArrayList<EventType>();

        // consider subqueryes
        Set<EventType> optSubselectTypes = populateSubqueryTypes(spec.getSubSelectExpressions());

        boolean hasFilterStream = false;
        for (StreamSpecCompiled streamSpec : spec.getStreamSpecs())
        {
            if (streamSpec instanceof FilterStreamSpecCompiled)
            {
                EventType type = ((FilterStreamSpecCompiled) streamSpec).getFilterSpec().getFilterForEventType();
                filteredTypes.add(type);
                hasFilterStream = true;
            }
        }

        if ((filteredTypes.size() == 1) && (optSubselectTypes.isEmpty()))
        {
            return false;
        }
        
        // pattern-only streams are not self-joins
        if (!hasFilterStream)
        {
            return false;
        }

        // is type overlap in filters
        for (int i = 0; i < filteredTypes.size(); i++)
        {
            for (int j = i + 1; j < filteredTypes.size(); j++)
            {
                EventType typeOne = filteredTypes.get(i);
                EventType typeTwo = filteredTypes.get(j);
                if (typeOne == typeTwo)
                {
                    return true;
                }

                if (typeOne.getSuperTypes() != null)
                {
                    for (EventType typeOneSuper : typeOne.getSuperTypes())
                    {
                        if (typeOneSuper == typeTwo)
                        {
                            return true;
                        }
                    }
                }
                if (typeTwo.getSuperTypes() != null)
                {
                    for (EventType typeTwoSuper : typeTwo.getSuperTypes())
                    {
                        if (typeOne == typeTwoSuper)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        // analyze subselect types
        if (!optSubselectTypes.isEmpty())
        {
            for (int i = 0; i < filteredTypes.size(); i++)
            {
                EventType typeOne = filteredTypes.get(i);
                if (optSubselectTypes.contains(typeOne))
                {
                    return true;
                }

                if (typeOne.getSuperTypes() != null)
                {
                    for (EventType typeOneSuper : typeOne.getSuperTypes())
                    {
                        if (optSubselectTypes.contains(typeOneSuper))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    private Set<EventType> populateSubqueryTypes(List<ExprSubselectNode> subSelectExpressions)
    {
        Set<EventType> set = null;
        for (ExprSubselectNode subselect : subSelectExpressions)
        {
            for (StreamSpecCompiled streamSpec : subselect.getStatementSpecCompiled().getStreamSpecs())
            {
                if (streamSpec instanceof FilterStreamSpecCompiled)
                {
                    EventType type = ((FilterStreamSpecCompiled) streamSpec).getFilterSpec().getFilterForEventType();
                    if (set == null)
                    {
                        set = new HashSet<EventType>();
                    }
                    set.add(type);
                }
                else if (streamSpec instanceof PatternStreamSpecCompiled)
                {
                    EvalNodeAnalysisResult evalNodeAnalysisResult = EvalNode.recursiveAnalyzeChildNodes(((PatternStreamSpecCompiled)streamSpec).getEvalNode());
                    List<EvalFilterNode> filterNodes = evalNodeAnalysisResult.getFilterNodes();
                    for (EvalFilterNode filterNode : filterNodes)
                    {
                        if (set == null)
                        {
                            set = new HashSet<EventType>();
                        }
                        set.add(filterNode.getFilterSpec().getFilterForEventType());
                    }
                }
            }            
        }
        if (set == null)
        {
            return Collections.EMPTY_SET;
        }
        return set;
    }

    public synchronized void start(String statementId)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".start Starting statement " + statementId);
        }

        // Acquire a lock for event processing as threads may be in the views used by the statement
        // and that could conflict with the destroy of views
        eventProcessingRWLock.acquireWriteLock();
        try
        {
            EPStatementDesc desc = stmtIdToDescMap.get(statementId);
            if (desc == null)
            {
                throw new IllegalStateException("Cannot start statement, statement is in destroyed state");
            }
            startInternal(statementId, desc, false, false, false);
        }
        finally
        {
            eventProcessingRWLock.releaseWriteLock();
        }
    }

    /**
     * Start the given statement.
     * @param statementId is the statement id
     * @param desc is the cached statement info
     * @param isNewStatement indicator whether the statement is new or a stop-restart statement
     * @param isRecoveringStatement if the statement is recovering or new
     * @param isResilient true if recovering a resilient stmt
     */
    public void start(String statementId, EPStatementDesc desc, boolean isNewStatement, boolean isRecoveringStatement, boolean isResilient)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".start Starting statement " + statementId + " from desc=" + desc);
        }

        // Acquire a lock for event processing as threads may be in the views used by the statement
        // and that could conflict with the destroy of views
        eventProcessingRWLock.acquireWriteLock();
        try
        {
            startInternal(statementId, desc, isNewStatement, isRecoveringStatement, isResilient);
        }
        finally
        {
            eventProcessingRWLock.releaseWriteLock();
        }
    }

    private void startInternal(String statementId, EPStatementDesc desc, boolean isNewStatement, boolean isRecoveringStatement, boolean isResilient)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".startInternal Starting statement " + statementId + " from desc=" + desc);
        }

        if (desc.getStartMethod() == null)
        {
            throw new IllegalStateException("Statement start method not found for id " + statementId);
        }

        EPStatementSPI statement = desc.getEpStatement();
        if (statement.getState() == EPStatementState.STARTED)
        {
            log.debug(".startInternal - Statement already started");
            return;
        }

        EPStatementStartResult startResult;
        try
        {
            startResult = desc.getStartMethod().start(isNewStatement, isRecoveringStatement, isResilient);
        }
        catch (EPStatementException ex)
        {
            stmtIdToDescMap.remove(statementId);
            stmtNameToIdMap.remove(statement.getName());
            stmtNameToStmtMap.remove(statement.getName());
            log.debug(".start Error starting statement", ex);
            throw ex;
        }
        catch (ExprValidationException ex)
        {
            stmtIdToDescMap.remove(statementId);
            stmtNameToIdMap.remove(statement.getName());
            stmtNameToStmtMap.remove(statement.getName());
            log.debug(".start Error starting statement", ex);
            throw new EPStatementException("Error starting statement: " + ex.getMessage(), statement.getText());
        }
        catch (ViewProcessingException ex)
        {
            stmtIdToDescMap.remove(statementId);
            stmtNameToIdMap.remove(statement.getName());
            stmtNameToStmtMap.remove(statement.getName());
            log.debug(".start Error starting statement", ex);
            throw new EPStatementException("Error starting statement: " + ex.getMessage(), statement.getText());
        }

        // add statically typed event type references: those in the from clause; Dynamic (created) types collected by statement context and added on start
        services.getStatementEventTypeRefService().addReferences(desc.getEpStatement().getName(), desc.getStatementContext().getDynamicReferenceEventTypes());

        // hook up
        Viewable parentView = startResult.getViewable();
        desc.setStopMethod(startResult.getStopMethod());
        desc.setDestroyMethod(startResult.getDestroyMethod());
        statement.setParentView(parentView);
        long timeLastStateChange = services.getSchedulingService().getTime();
        statement.setCurrentState(EPStatementState.STARTED, timeLastStateChange);

        dispatchStatementLifecycleEvent(new StatementLifecycleEvent(statement, StatementLifecycleEvent.LifecycleEventType.STATECHANGE));
    }

    public synchronized void stop(String statementId)
    {
        // Acquire a lock for event processing as threads may be in the views used by the statement
        // and that could conflict with the destroy of views
        eventProcessingRWLock.acquireWriteLock();
        try
        {
            EPStatementDesc desc = stmtIdToDescMap.get(statementId);
            if (desc == null)
            {
                throw new IllegalStateException("Cannot stop statement, statement is in destroyed state");
            }

            EPStatementSPI statement = desc.getEpStatement();
            EPStatementStopMethod stopMethod = desc.getStopMethod();
            if (stopMethod == null)
            {
                throw new IllegalStateException("Stop method not found for statement " + statementId);
            }

            if (statement.getState() == EPStatementState.STOPPED)
            {
                log.debug(".startInternal - Statement already stopped");
                return;
            }

            stopMethod.stop();
            statement.setParentView(null);
            desc.setStopMethod(null);

            long timeLastStateChange = services.getSchedulingService().getTime();
            statement.setCurrentState(EPStatementState.STOPPED, timeLastStateChange);

            dispatchStatementLifecycleEvent(new StatementLifecycleEvent(statement, StatementLifecycleEvent.LifecycleEventType.STATECHANGE));
        }
        finally
        {
            eventProcessingRWLock.releaseWriteLock();
        }
    }

    public synchronized void destroy(String statementId)
    {
        // Acquire a lock for event processing as threads may be in the views used by the statement
        // and that could conflict with the destroy of views
        eventProcessingRWLock.acquireWriteLock();
        try
        {
            EPStatementDesc desc = stmtIdToDescMap.get(statementId);
            if (desc == null)
            {
                log.debug(".startInternal - Statement already destroyed");
                return;
            }

            // remove referenced event types
            services.getStatementEventTypeRefService().removeReferencesStatement(desc.getEpStatement().getName());

            // remove referenced variabkes
            services.getStatementVariableRefService().removeReferencesStatement(desc.getEpStatement().getName());

            EPStatementSPI statement = desc.getEpStatement();
            if (statement.getState() == EPStatementState.STARTED)
            {
                EPStatementStopMethod stopMethod = desc.getStopMethod();
                statement.setParentView(null);
                stopMethod.stop();
            }

            if (desc.getDestroyMethod() != null) {
                desc.getDestroyMethod().destroy();
            }

            long timeLastStateChange = services.getSchedulingService().getTime();
            statement.setCurrentState(EPStatementState.DESTROYED, timeLastStateChange);

            stmtNameToStmtMap.remove(statement.getName());
            stmtNameToIdMap.remove(statement.getName());
            stmtIdToDescMap.remove(statementId);

            dispatchStatementLifecycleEvent(new StatementLifecycleEvent(statement, StatementLifecycleEvent.LifecycleEventType.STATECHANGE));
        }
        finally
        {
            eventProcessingRWLock.releaseWriteLock();
        }
    }

    public synchronized EPStatement getStatementByName(String name)
    {
        return stmtNameToStmtMap.get(name);
    }

    /**
     * Returns the statement given a statement id.
     * @param id is the statement id
     * @return statement
     */
    public EPStatementSPI getStatementById(String id)
    {
        EPStatementDesc statementDesc = this.stmtIdToDescMap.get(id);
        if (statementDesc == null)
        {
            log.warn("Could not locate statement descriptor for statement id '" + id + "'");
            return null;
        }
        return statementDesc.getEpStatement();
    }

    public synchronized String[] getStatementNames()
    {
        String[] statements = new String[stmtNameToStmtMap.size()];
        int count = 0;
        for (String key : stmtNameToStmtMap.keySet())
        {
            statements[count++] = key;
        }
        return statements;
    }

    public synchronized void startAllStatements() throws EPException
    {
        String[] statementIds = getStatementIds();
        for (int i = 0; i < statementIds.length; i++)
        {
            EPStatement statement = stmtIdToDescMap.get(statementIds[i]).getEpStatement();
            if (statement.getState() == EPStatementState.STOPPED)
            {
                start(statementIds[i]);
            }
        }
    }

    public synchronized void stopAllStatements() throws EPException
    {
        String[] statementIds = getStatementIds();
        for (int i = 0; i < statementIds.length; i++)
        {
            EPStatement statement = stmtIdToDescMap.get(statementIds[i]).getEpStatement();
            if (statement.getState() == EPStatementState.STARTED)
            {
                stop(statementIds[i]);
            }
        }
    }

    public synchronized void destroyAllStatements() throws EPException
    {
        String[] statementIds = getStatementIds();
        for (int i = 0; i < statementIds.length; i++)
        {
            try
            {
                destroy(statementIds[i]);
            }
            catch (Exception ex)
            {
                log.warn("Error destroying statement:" + ex.getMessage());
            }
        }
    }

    private String[] getStatementIds()
    {
        String[] statementIds = new String[stmtNameToIdMap.size()];
        int count = 0;
        for (String id : stmtNameToIdMap.values())
        {
            statementIds[count++] = id;
        }
        return statementIds;
    }

    private String getUniqueStatementName(String statementName, String statementId)
    {
        String finalStatementName;

        if (stmtNameToIdMap.containsKey(statementName))
        {
            int count = 0;
            while(true)
            {
                finalStatementName = statementName + "--" + count;
                if (!(stmtNameToIdMap.containsKey(finalStatementName)))
                {
                    break;
                }
                if (count > Integer.MAX_VALUE - 2)
                {
                    throw new EPException("Failed to establish a unique statement name");
                }
                count++;
            }
        }
        else
        {
            finalStatementName = statementName;
        }

        stmtNameToIdMap.put(finalStatementName, statementId);
        return finalStatementName;
    }

    @Override
    public String getStatementNameById(String id) {
        EPStatementDesc desc = stmtIdToDescMap.get(id);
        if (desc != null) {
            return desc.getEpStatement().getName();
        }
        return null;
    }

    public void updatedListeners(EPStatement statement, EPStatementListenerSet listeners)
    {
        log.debug(".updatedListeners No action for base implementation");
    }

    /**
     * Compiles a statement returning the compile (verified, non-serializable) form of a statement.
     * @param spec is the statement specification
     * @param eplStatement the statement to compile
     * @param statementContext the statement services
     * @param isSubquery is true for subquery compilation or false for statement compile
     * @param annotations statement annotations
     * @return compiled statement
     * @throws EPStatementException if the statement cannot be compiled
     */
    protected static StatementSpecCompiled compile(StatementSpecRaw spec, String eplStatement, StatementContext statementContext, boolean isSubquery, Annotation[] annotations) throws EPStatementException
    {
        List<StreamSpecCompiled> compiledStreams;
        Set<String> eventTypeReferences = new HashSet<String>();

        // If not using a join and not specifying a data window, make the where-clause, if present, the filter of the stream
        // if selecting using filter spec, and not subquery in where clause
        if ((spec.getStreamSpecs().size() == 1) &&
            (spec.getStreamSpecs().get(0) instanceof FilterStreamSpecRaw) &&
            (spec.getStreamSpecs().get(0).getViewSpecs().isEmpty()) &&
            (spec.getFilterRootNode() != null) &&
            (spec.getOnTriggerDesc() == null) &&
            (!isSubquery))
        {
            boolean disqualified;
            ExprNode whereClause = spec.getFilterRootNode();

            ExprNodeSubselectVisitor visitor = new ExprNodeSubselectVisitor();
            whereClause.accept(visitor);
            disqualified = visitor.getSubselects().size() > 0;

            if (!disqualified)
            {
                ExprNodeViewResourceVisitor viewResourceVisitor = new ExprNodeViewResourceVisitor();
                whereClause.accept(viewResourceVisitor);
                disqualified = viewResourceVisitor.getExprNodes().size() > 0;
            }


            if (!disqualified)
            {
                // If an alias is provided, find all properties to ensure the alias gets removed
                String alias = spec.getStreamSpecs().get(0).getOptionalStreamName();
                if (alias != null) {
                    ExprNodeIdentifierCollectVisitor v = new ExprNodeIdentifierCollectVisitor();
                    whereClause.accept(v);
                    for (ExprIdentNode node : v.getExprProperties()) {
                        if (node.getStreamOrPropertyName() != null && (node.getStreamOrPropertyName().equals(alias))) {
                            node.setStreamOrPropertyName(null);
                        }
                    }
                }

                spec.setFilterRootNode(null);
                FilterStreamSpecRaw streamSpec = (FilterStreamSpecRaw) spec.getStreamSpecs().get(0);
                streamSpec.getRawFilterSpec().getFilterExpressions().add(whereClause);
            }
        }

        // Look for expressions with sub-selects in select expression list and filter expression
        // Recursively compile the statement within the statement.
        ExprNodeSubselectVisitor visitor = new ExprNodeSubselectVisitor();
        List<SelectClauseElementCompiled> selectElements = new ArrayList<SelectClauseElementCompiled>();
        SelectClauseSpecCompiled selectClauseCompiled = new SelectClauseSpecCompiled(selectElements, spec.getSelectClauseSpec().isDistinct());
        for (SelectClauseElementRaw raw : spec.getSelectClauseSpec().getSelectExprList())
        {
            if (raw instanceof SelectClauseExprRawSpec)
            {
                SelectClauseExprRawSpec rawExpr = (SelectClauseExprRawSpec) raw;
                rawExpr.getSelectExpression().accept(visitor);
                selectElements.add(new SelectClauseExprCompiledSpec(rawExpr.getSelectExpression(), rawExpr.getOptionalAsName()));
            }
            else if (raw instanceof SelectClauseStreamRawSpec)
            {
                SelectClauseStreamRawSpec rawExpr = (SelectClauseStreamRawSpec) raw;
                selectElements.add(new SelectClauseStreamCompiledSpec(rawExpr.getStreamName(), rawExpr.getOptionalAsName()));
            }
            else if (raw instanceof SelectClauseElementWildcard)
            {
                SelectClauseElementWildcard wildcard = (SelectClauseElementWildcard) raw;
                selectElements.add(wildcard);
            }
            else
            {
                throw new IllegalStateException("Unexpected select clause element class : " + raw.getClass().getName());
            }
        }
        if (spec.getFilterRootNode() != null)
        {
            spec.getFilterRootNode().accept(visitor);
        }
        if (spec.getUpdateDesc() != null)
        {
            if (spec.getUpdateDesc().getOptionalWhereClause() != null)
            {
                spec.getUpdateDesc().getOptionalWhereClause().accept(visitor);
            }
            for (OnTriggerSetAssignment assignment : spec.getUpdateDesc().getAssignments())
            {
                assignment.getExpression().accept(visitor);
            }
        }
        if (spec.getOnTriggerDesc() != null) {
            visitSubselectOnTrigger(spec.getOnTriggerDesc(), visitor);
        }
        // Determine pattern-filter subqueries
        for (StreamSpecRaw streamSpecRaw : spec.getStreamSpecs()) {
            if (streamSpecRaw instanceof PatternStreamSpecRaw) {
                PatternStreamSpecRaw patternStreamSpecRaw = (PatternStreamSpecRaw) streamSpecRaw;
                EvalNodeAnalysisResult analysisResult = EvalNode.recursiveAnalyzeChildNodes(patternStreamSpecRaw.getEvalNode());
                for (EvalFilterNode filterNode : analysisResult.getFilterNodes()) {
                    for (ExprNode filterExpr : filterNode.getRawFilterSpec().getFilterExpressions()) {
                        filterExpr.accept(visitor);
                    }
                }
            }
        }
        // Determine filter streams
        for (StreamSpecRaw rawSpec : spec.getStreamSpecs())
        {
            if (rawSpec instanceof FilterStreamSpecRaw) {
                FilterStreamSpecRaw raw = (FilterStreamSpecRaw) rawSpec;
                for (ExprNode filterExpr : raw.getRawFilterSpec().getFilterExpressions()) {
                    filterExpr.accept(visitor);
                }
            }
        }
        for (ExprSubselectNode subselect : visitor.getSubselects())
        {
            StatementSpecRaw raw = subselect.getStatementSpecRaw();
            StatementSpecCompiled compiled = compile(raw, eplStatement, statementContext, true, new Annotation[0]);
            subselect.setStatementSpecCompiled(compiled);
        }

        // compile each stream used
        try
        {
            compiledStreams = new ArrayList<StreamSpecCompiled>();
            for (StreamSpecRaw rawSpec : spec.getStreamSpecs())
            {
                StreamSpecCompiled compiled = rawSpec.compile(statementContext, eventTypeReferences, spec.getInsertIntoDesc() != null);
                compiledStreams.add(compiled);
            }
        }
        catch (ExprValidationException ex)
        {
            log.info("Failed to compile statement: " + ex.getMessage(), ex);
            if (ex.getMessage() == null)
            {
                throw new EPStatementException("Unexpected exception compiling statement, please consult the log file and report the exception", eplStatement);
            }
            else
            {
                throw new EPStatementException(ex.getMessage(), eplStatement);
            }
        }
        catch (RuntimeException ex)
        {
            String text = "Unexpected error compiling statement";
            log.error(text, ex);
            throw new EPStatementException(text + ": " + ex.getClass().getName() + ":" + ex.getMessage(), eplStatement);
        }

        // for create window statements, we switch the filter to a new event type
        if (spec.getCreateWindowDesc() != null)
        {
            try
            {
                StreamSpecCompiled createWindowTypeSpec = compiledStreams.get(0);
                EventType selectFromType;
                String selectFromTypeName;
                if (createWindowTypeSpec instanceof FilterStreamSpecCompiled)
                {
                    FilterStreamSpecCompiled filterStreamSpec = (FilterStreamSpecCompiled) createWindowTypeSpec;
                    selectFromType = filterStreamSpec.getFilterSpec().getFilterForEventType();
                    selectFromTypeName = filterStreamSpec.getFilterSpec().getFilterForEventTypeName();

                    if (spec.getCreateWindowDesc().getInsertFilter() != null)
                    {
                        throw new EPStatementException("A named window by name '" + selectFromTypeName + "' could not be located, use the insert-keyword with an existing named window", eplStatement);
                    }
                }
                else
                {
                    NamedWindowConsumerStreamSpec consumerStreamSpec = (NamedWindowConsumerStreamSpec) createWindowTypeSpec;
                    selectFromType = statementContext.getEventAdapterService().getExistsTypeByName(consumerStreamSpec.getWindowName());
                    selectFromTypeName = consumerStreamSpec.getWindowName();

                    if (spec.getCreateWindowDesc().getInsertFilter() != null)
                    {
                        ExprNode insertIntoFilter = spec.getCreateWindowDesc().getInsertFilter();
                        String checkMinimal = ExprNodeUtility.isMinimalExpression(insertIntoFilter);
                        if (checkMinimal != null)
                        {
                            throw new ExprValidationException("Create window where-clause may not have " + checkMinimal);
                        }
                        StreamTypeService streamTypeService = new StreamTypeServiceImpl(selectFromType, selectFromTypeName, true, statementContext.getEngineURI());
                        ExprNode insertFilter = spec.getCreateWindowDesc().getInsertFilter().getValidatedSubtree(streamTypeService, statementContext.getMethodResolutionService(), null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
                        spec.getCreateWindowDesc().setInsertFilter(insertFilter);
                    }

                    // set the window to insert from
                    spec.getCreateWindowDesc().setInsertFromWindow(consumerStreamSpec.getWindowName());
                }
                Pair<FilterSpecCompiled, SelectClauseSpecRaw> newFilter = handleCreateWindow(selectFromType, selectFromTypeName, spec.getCreateWindowDesc().getColumns(), spec, eplStatement, statementContext);
                eventTypeReferences.add(((EventTypeSPI)newFilter.getFirst().getFilterForEventType()).getMetadata().getPrimaryName());

                // view must be non-empty list
                if (spec.getCreateWindowDesc().getViewSpecs().isEmpty())
                {
                    throw new ExprValidationException(NamedWindowService.ERROR_MSG_DATAWINDOWS);
                }

                // use the filter specification of the newly created event type and the views for the named window
                compiledStreams.clear();
                List<ViewSpec> views = new ArrayList<ViewSpec>(spec.getCreateWindowDesc().getViewSpecs());
                compiledStreams.add(new FilterStreamSpecCompiled(newFilter.getFirst(), views, null, spec.getCreateWindowDesc().getStreamSpecOptions()));
                spec.setSelectClauseSpec(newFilter.getSecond());
            }
            catch (ExprValidationException e)
            {
                throw new EPStatementException(e.getMessage(), eplStatement);
            }
        }

        return new StatementSpecCompiled(
                spec.getOnTriggerDesc(),
                spec.getCreateWindowDesc(),
                spec.getCreateIndexDesc(),
                spec.getCreateVariableDesc(),
                spec.getCreateSchemaDesc(),
                spec.getInsertIntoDesc(),
                spec.getSelectStreamSelectorEnum(),
                selectClauseCompiled,
                compiledStreams,
                spec.getOuterJoinDescList(),
                spec.getFilterRootNode(),
                spec.getGroupByExpressions(),
                spec.getHavingExprRootNode(),
                spec.getOutputLimitSpec(),
                spec.getOrderByList(),
                visitor.getSubselects(),
                spec.getReferencedVariables(),
                spec.getRowLimitSpec(),
                eventTypeReferences,
                annotations,
                spec.getUpdateDesc(),
                spec.getMatchRecognizeSpec(),
                spec.getForClauseSpec(),
                spec.getSqlParameters()
                );
    }

    private static void visitSubselectOnTrigger(OnTriggerDesc onTriggerDesc, ExprNodeSubselectVisitor visitor) {
        if (onTriggerDesc instanceof OnTriggerWindowUpdateDesc) {
            OnTriggerWindowUpdateDesc updates = (OnTriggerWindowUpdateDesc) onTriggerDesc;
            for (OnTriggerSetAssignment assignment : updates.getAssignments())
            {
                assignment.getExpression().accept(visitor);
            }
        }
        else if (onTriggerDesc instanceof OnTriggerSetDesc) {
            OnTriggerSetDesc sets = (OnTriggerSetDesc) onTriggerDesc;
            for (OnTriggerSetAssignment assignment : sets.getAssignments())
            {
                assignment.getExpression().accept(visitor);
            }
        }
        else if (onTriggerDesc instanceof OnTriggerSplitStreamDesc) {
            OnTriggerSplitStreamDesc splits = (OnTriggerSplitStreamDesc) onTriggerDesc;
            for (OnTriggerSplitStream split : splits.getSplitStreams())
            {
                if (split.getWhereClause() != null) {
                    split.getWhereClause().accept(visitor);
                }
                if (split.getSelectClause().getSelectExprList() != null) {
                    for (SelectClauseElementRaw element : split.getSelectClause().getSelectExprList()) {
                        if (element instanceof SelectClauseExprRawSpec) {
                            SelectClauseExprRawSpec selectExpr = (SelectClauseExprRawSpec) element;
                            selectExpr.getSelectExpression().accept(visitor);
                        }
                    }
                }
            }
        }
        else if (onTriggerDesc instanceof OnTriggerMergeDesc) {
            OnTriggerMergeDesc merge = (OnTriggerMergeDesc) onTriggerDesc;
            for (OnTriggerMergeItem item : merge.getItems())
            {
                if (item.getOptionalMatchCond() != null) {
                    item.getOptionalMatchCond().accept(visitor);
                }

                if (item instanceof OnTriggerMergeItemUpdate) {
                    OnTriggerMergeItemUpdate update = (OnTriggerMergeItemUpdate) item;
                    for (OnTriggerSetAssignment assignment : update.getAssignments())
                    {
                        assignment.getExpression().accept(visitor);
                    }
                }
                if (item instanceof OnTriggerMergeItemInsert) {
                    OnTriggerMergeItemInsert insert = (OnTriggerMergeItemInsert) item;
                    for (SelectClauseElementRaw element : insert.getSelectClause()) {
                        if (element instanceof SelectClauseExprRawSpec) {
                            SelectClauseExprRawSpec selectExpr = (SelectClauseExprRawSpec) element;
                            selectExpr.getSelectExpression().accept(visitor);
                        }
                    }
                }
            }
        }
    }

    /**
     * Compile a select clause allowing subselects.
     * @param spec to compile
     * @return select clause compiled
     * @throws ExprValidationException when validation fails
     */
    public static SelectClauseSpecCompiled compileSelectAllowSubselect(SelectClauseSpecRaw spec) throws ExprValidationException
    {
        // Look for expressions with sub-selects in select expression list and filter expression
        // Recursively compile the statement within the statement.
        ExprNodeSubselectVisitor visitor = new ExprNodeSubselectVisitor();
        List<SelectClauseElementCompiled> selectElements = new ArrayList<SelectClauseElementCompiled>();
        SelectClauseSpecCompiled selectClauseCompiled = new SelectClauseSpecCompiled(selectElements, spec.isDistinct());
        for (SelectClauseElementRaw raw : spec.getSelectExprList())
        {
            if (raw instanceof SelectClauseExprRawSpec)
            {
                SelectClauseExprRawSpec rawExpr = (SelectClauseExprRawSpec) raw;
                rawExpr.getSelectExpression().accept(visitor);
                selectElements.add(new SelectClauseExprCompiledSpec(rawExpr.getSelectExpression(), rawExpr.getOptionalAsName()));
            }
            else if (raw instanceof SelectClauseStreamRawSpec)
            {
                SelectClauseStreamRawSpec rawExpr = (SelectClauseStreamRawSpec) raw;
                selectElements.add(new SelectClauseStreamCompiledSpec(rawExpr.getStreamName(), rawExpr.getOptionalAsName()));
            }
            else if (raw instanceof SelectClauseElementWildcard)
            {
                SelectClauseElementWildcard wildcard = (SelectClauseElementWildcard) raw;
                selectElements.add(wildcard);
            }
            else
            {
                throw new IllegalStateException("Unexpected select clause element class : " + raw.getClass().getName());
            }
        }
        return selectClauseCompiled;
    }

    // The create window command:
    //      create window windowName[.window_view_list] as [select properties from] type
    //
    // This section expected s single FilterStreamSpecCompiled representing the selected type.
    // It creates a new event type representing the window type and a sets the type selected on the filter stream spec.
    private static Pair<FilterSpecCompiled, SelectClauseSpecRaw> handleCreateWindow(EventType selectFromType,
                                           String selectFromTypeName,
                                           List<ColumnDesc> columns,
                                           StatementSpecRaw spec,
                                           String eplStatement,
                                           StatementContext statementContext)
            throws ExprValidationException
    {
        String typeName = spec.getCreateWindowDesc().getWindowName();
        EventType targetType;

        // Validate the select expressions which consists of properties only
        List<NamedWindowSelectedProps> select = compileLimitedSelect(spec.getSelectClauseSpec(), eplStatement, selectFromType, selectFromTypeName, statementContext.getEngineURI(), statementContext, statementContext.getMethodResolutionService());

        // Create Map or Wrapper event type from the select clause of the window.
        // If no columns selected, simply create a wrapper type
        // Build a list of properties
        SelectClauseSpecRaw newSelectClauseSpecRaw = new SelectClauseSpecRaw();
        Map<String, Object> properties;
        boolean hasProperties = false;
        if ((columns != null) && (!columns.isEmpty())) {
            properties = TypeBuilderUtil.buildType(columns);
            hasProperties = true;
        }
        else {
            properties = new HashMap<String, Object>();
            for (NamedWindowSelectedProps selectElement : select)
            {
                if (selectElement.getFragmentType() != null)
                {
                    properties.put(selectElement.getAssignedName(), selectElement.getFragmentType());
                }
                else
                {
                    properties.put(selectElement.getAssignedName(), selectElement.getSelectExpressionType());
                }

                // Add any properties to the new select clause for use by consumers to the statement itself
                newSelectClauseSpecRaw.add(new SelectClauseExprRawSpec(new ExprIdentNode(selectElement.getAssignedName()), null));
                hasProperties = true;
            }
        }

        // Create Map or Wrapper event type from the select clause of the window.
        // If no columns selected, simply create a wrapper type
        boolean isOnlyWildcard = spec.getSelectClauseSpec().isOnlyWildcard();
        boolean isWildcard = spec.getSelectClauseSpec().isUsingWildcard();
        if (statementContext.getValueAddEventService().isRevisionTypeName(selectFromTypeName))
        {
            targetType = statementContext.getValueAddEventService().createRevisionType(typeName, selectFromTypeName, statementContext.getStatementStopService(), statementContext.getEventAdapterService());
        }
        else if (isWildcard && !isOnlyWildcard)
        {
            targetType = statementContext.getEventAdapterService().addWrapperType(typeName, selectFromType, properties, true, false);
        }
        else
        {
            // Some columns selected, use the types of the columns
            if (hasProperties && !isOnlyWildcard)
            {
                targetType = statementContext.getEventAdapterService().addNestableMapType(typeName, properties, null, false, false, false, true, false);
            }
            else
            {
                // No columns selected, no wildcard, use the type as is or as a wrapped type
                if (selectFromType instanceof MapEventType)
                {
                    MapEventType mapType = (MapEventType) selectFromType;
                    targetType = statementContext.getEventAdapterService().addNestableMapType(typeName, mapType.getTypes(), null, false, false, false, true, false);
                }
                else
                {
                    Map<String, Object> addOnTypes = new HashMap<String, Object>();
                    targetType = statementContext.getEventAdapterService().addWrapperType(typeName, selectFromType, addOnTypes, true, false);
                }
            }
        }

        FilterSpecCompiled filter = new FilterSpecCompiled(targetType, typeName, new ArrayList<FilterSpecParam>(), null);
        return new Pair<FilterSpecCompiled, SelectClauseSpecRaw>(filter, newSelectClauseSpecRaw);
    }

    private static List<NamedWindowSelectedProps> compileLimitedSelect(SelectClauseSpecRaw spec, String eplStatement, EventType singleType, String selectFromTypeName, String engineURI, ExprEvaluatorContext exprEvaluatorContext, MethodResolutionService methodResolutionService)
    {
        List<NamedWindowSelectedProps> selectProps = new LinkedList<NamedWindowSelectedProps>();
        StreamTypeService streams = new StreamTypeServiceImpl(new EventType[] {singleType}, new String[] {"stream_0"}, new boolean[] {false}, engineURI, false);

        for (SelectClauseElementRaw raw : spec.getSelectExprList())
        {
            if (!(raw instanceof SelectClauseExprRawSpec))
            {
                continue;
            }
            SelectClauseExprRawSpec exprSpec = (SelectClauseExprRawSpec) raw;
            ExprNode validatedExpression;
            try
            {
                validatedExpression = exprSpec.getSelectExpression().getValidatedSubtree(streams, methodResolutionService, null, null, null, exprEvaluatorContext);
            }
            catch (ExprValidationException e)
            {
                throw new EPStatementException(e.getMessage(), eplStatement);
            }

            // determine an element name if none assigned
            String asName = exprSpec.getOptionalAsName();
            if (asName == null)
            {
                asName = validatedExpression.toExpressionString();
            }

            // check for fragments
            EventType fragmentType = null;
            if ((validatedExpression instanceof ExprIdentNode) && (!(singleType instanceof NativeEventType)))
            {
                ExprIdentNode identNode = (ExprIdentNode) validatedExpression;
                FragmentEventType fragmentEventType = singleType.getFragmentType(identNode.getFullUnresolvedName());
                if ((fragmentEventType != null) && (!fragmentEventType.isNative()))
                {
                    fragmentType = fragmentEventType.getFragmentType();
                }
            }

            NamedWindowSelectedProps validatedElement = new NamedWindowSelectedProps(validatedExpression.getExprEvaluator().getType(), asName, fragmentType);
            selectProps.add(validatedElement);
        }

        return selectProps;
    }

    public void dispatchStatementLifecycleEvent(StatementLifecycleEvent event)
    {
        for (StatementLifecycleObserver observer : observers)
        {
            observer.observe(event);
        }
    }

    /**
     * Statement information.
     */
    public static class EPStatementDesc
    {
        private EPStatementSPI epStatement;
        private EPStatementStartMethod startMethod;
        private EPStatementStopMethod stopMethod;
        private EPStatementDestroyMethod destroyMethod;
        private String optInsertIntoStream;
        private EPStatementHandle statementHandle;
        private StatementContext statementContext;

        /**
         * Ctor.
         * @param epStatement the statement
         * @param startMethod the start method
         * @param stopMethod the stop method
         * @param optInsertIntoStream is the insert-into stream name, or null if not using insert-into
         * @param statementHandle is the locking handle for the statement
         * @param statementContext statement context
         * @param destroyMethod method to call when destroyed
         */
        public EPStatementDesc(EPStatementSPI epStatement, EPStatementStartMethod startMethod, EPStatementStopMethod stopMethod, EPStatementDestroyMethod destroyMethod, String optInsertIntoStream, EPStatementHandle statementHandle, StatementContext statementContext)
        {
            this.epStatement = epStatement;
            this.startMethod = startMethod;
            this.stopMethod = stopMethod;
            this.destroyMethod = destroyMethod;
            this.optInsertIntoStream = optInsertIntoStream;
            this.statementHandle = statementHandle;
            this.statementContext = statementContext;
        }

        /**
         * Returns the statement.
         * @return statement.
         */
        public EPStatementSPI getEpStatement()
        {
            return epStatement;
        }

        /**
         * Returns the start method.
         * @return start method
         */
        public EPStatementStartMethod getStartMethod()
        {
            return startMethod;
        }

        /**
         * Returns the stop method.
         * @return stop method
         */
        public EPStatementStopMethod getStopMethod()
        {
            return stopMethod;
        }

        /**
         * Return the insert-into stream name, or null if no insert-into
         * @return stream name
         */
        public String getOptInsertIntoStream()
        {
            return optInsertIntoStream;
        }

        /**
         * Sets the stop method.
         * @param stopMethod to set
         */
        public void setStopMethod(EPStatementStopMethod stopMethod)
        {
            this.stopMethod = stopMethod;
        }

        /**
         * Returns the statements handle.
         * @return statement handle
         */
        public EPStatementHandle getStatementHandle()
        {
            return statementHandle;
        }

        /**
         * Returns the statement context.
         * @return statement context
         */
        public StatementContext getStatementContext()
        {
            return statementContext;
        }

        /**
         * Set method to call when destroyed.
         * @param destroyMethod method
         */
        public void setDestroyMethod(EPStatementDestroyMethod destroyMethod) {
            this.destroyMethod = destroyMethod;
        }

        /**
         * Return destroy method.
         * @return method.
         */
        public EPStatementDestroyMethod getDestroyMethod() {
            return destroyMethod;
        }
    }
}

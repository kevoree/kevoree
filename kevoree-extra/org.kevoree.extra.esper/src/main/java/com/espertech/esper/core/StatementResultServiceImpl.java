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
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.thread.OutboundUnitRunnable;
import com.espertech.esper.core.thread.ThreadingOption;
import com.espertech.esper.core.thread.ThreadingService;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.metric.MetricReportingPath;
import com.espertech.esper.epl.metric.MetricReportingService;
import com.espertech.esper.epl.metric.MetricReportingServiceSPI;
import com.espertech.esper.epl.metric.StatementMetricHandle;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.event.NaturalEventBean;
import com.espertech.esper.util.AuditPath;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Implements tracking of statement listeners and subscribers for a given statement
 * such as to efficiently dispatch in situations where 0, 1 or more listeners
 * are attached and/or 0 or 1 subscriber (such as iteration-only statement).
 */
public class StatementResultServiceImpl implements StatementResultService
{
    private static Log log = LogFactory.getLog(StatementResultServiceImpl.class);

    private final StatementLifecycleSvc statementLifecycleSvc;
    private final MetricReportingService metricReportingService;
    private final ThreadingService threadingService;

    // Part of the statement context
    private EPStatementSPI epStatement;
    private EPServiceProviderSPI epServiceProvider;
    private boolean isInsertInto;
    private boolean isPattern;
    private boolean isDistinct;
    private boolean isForClause;
    private StatementMetricHandle statementMetricHandle;

    private boolean forClauseDelivery= false;
    private ExprEvaluator[] groupDeliveryExpressions;
    private ExprEvaluatorContext exprEvaluatorContext;

    // For natural delivery derived out of select-clause expressions
    private Class[] selectClauseTypes;
    private String[] selectClauseColumnNames;

    // Listeners and subscribers and derived information
    private EPStatementListenerSet statementListenerSet;
    private boolean isMakeNatural;
    private boolean isMakeSynthetic;
    private ResultDeliveryStrategy statementResultNaturalStrategy;

    // For iteration over patterns
    private EventBean lastIterableEvent;

    private Set<StatementResultListener> statementOutputHooks;

    /**
     * Buffer for holding dispatchable events.
     */
    protected ThreadLocal<ArrayDeque<UniformPair<EventBean[]>>> lastResults = new ThreadLocal<ArrayDeque<UniformPair<EventBean[]>>>() {
        protected synchronized ArrayDeque<UniformPair<EventBean[]>> initialValue() {
            return new ArrayDeque<UniformPair<EventBean[]>>();
        }
    };

    /**
     * Ctor.
     * @param statementLifecycleSvc handles persistence for statements
     * @param metricReportingService for metrics reporting
     * @param threadingService for outbound threading
     */
    public StatementResultServiceImpl(StatementLifecycleSvc statementLifecycleSvc,
                                      MetricReportingServiceSPI metricReportingService,
                                      ThreadingService threadingService)
    {
        log.debug(".ctor");
        this.statementLifecycleSvc = statementLifecycleSvc;
        this.metricReportingService = metricReportingService;
        if (metricReportingService != null) {
            this.statementOutputHooks = metricReportingService.getStatementOutputHooks();
        }
        else {
            this.statementOutputHooks = Collections.EMPTY_SET;
        }
        this.threadingService = threadingService;
    }

    public void setContext(EPStatementSPI epStatement, EPServiceProviderSPI epServiceProvider,
                           boolean isInsertInto,
                           boolean isPattern,
                           boolean isDistinct,
                           boolean isForClause,
                           StatementMetricHandle statementMetricHandle)
    {
        this.epStatement = epStatement;
        this.epServiceProvider = epServiceProvider;
        this.isInsertInto = isInsertInto;
        this.isPattern = isPattern;
        this.isDistinct = isDistinct;
        this.isForClause = isForClause;
        isMakeSynthetic = isInsertInto || isPattern || isDistinct || isForClause;
        this.statementMetricHandle = statementMetricHandle;
    }

    public void setSelectClause(Class[] selectClauseTypes, String[] selectClauseColumnNames,
                                boolean forClauseDelivery, ExprEvaluator[] groupDeliveryExpressions, ExprEvaluatorContext exprEvaluatorContext)
    {
        if ((selectClauseTypes == null) || (selectClauseTypes.length == 0))
        {
            throw new IllegalArgumentException("Invalid null or zero-element list of select clause expression types");
        }
        if ((selectClauseColumnNames == null) || (selectClauseColumnNames.length == 0))
        {
            throw new IllegalArgumentException("Invalid null or zero-element list of select clause column names");
        }
        this.selectClauseTypes = selectClauseTypes;
        this.selectClauseColumnNames = selectClauseColumnNames;
        this.forClauseDelivery = forClauseDelivery;
        this.exprEvaluatorContext = exprEvaluatorContext;
        this.groupDeliveryExpressions = groupDeliveryExpressions;
    }

    public boolean isMakeSynthetic()
    {
        return isMakeSynthetic;
    }

    public boolean isMakeNatural()
    {
        return isMakeNatural;
    }

    public EventBean getLastIterableEvent()
    {
        return lastIterableEvent;
    }

    public void setUpdateListeners(EPStatementListenerSet statementListenerSet)
    {
        // indicate that listeners were updated for potential persistence of listener set, once the statement context is known
        if (epStatement != null)
        {
            this.statementLifecycleSvc.updatedListeners(epStatement, statementListenerSet);
        }

        this.statementListenerSet = statementListenerSet;

        isMakeNatural = statementListenerSet.getSubscriber() != null;
        isMakeSynthetic = !(statementListenerSet.getListeners().isEmpty() && statementListenerSet.getStmtAwareListeners().isEmpty())
                || isPattern || isInsertInto || isDistinct | isForClause;

        if (statementListenerSet.getSubscriber() == null)
        {
            statementResultNaturalStrategy = null;
            isMakeNatural = false;
            return;
        }

        statementResultNaturalStrategy = ResultDeliveryStrategyFactory.create(statementListenerSet.getSubscriber(),
                selectClauseTypes, selectClauseColumnNames);
        isMakeNatural = true;
    }

    // Called by OutputProcessView
    public void indicate(UniformPair<EventBean[]> results)
    {
        if (results != null)
        {
            if ((MetricReportingPath.isMetricsEnabled) && (statementMetricHandle.isEnabled()))
            {
                int numIStream = (results.getFirst() != null) ? results.getFirst().length : 0;
                int numRStream = (results.getSecond() != null) ? results.getSecond().length : 0;
                this.metricReportingService.accountOutput(statementMetricHandle, numIStream, numRStream);
            }

            if ((results.getFirst() != null) && (results.getFirst().length != 0))
            {
                lastResults.get().add(results);
                lastIterableEvent = results.getFirst()[0];
            }
            else if ((results.getSecond() != null) && (results.getSecond().length != 0))
            {
                lastResults.get().add(results);
            }
        }
    }

    public void execute()
    {
        ArrayDeque<UniformPair<EventBean[]>> dispatches = lastResults.get();
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".execute dispatches: " + dispatches.size());
        }

        UniformPair<EventBean[]> events = EventBeanUtility.flattenList(dispatches);

        if (ExecutionPathDebugLog.isDebugEnabled && log.isDebugEnabled())
        {
            ViewSupport.dumpUpdateParams(".execute", events);
        }

        if ((ThreadingOption.isThreadingEnabled) && (threadingService.isOutboundThreading()))
        {
            threadingService.submitOutbound(new OutboundUnitRunnable(events, this));
        }
        else
        {
            processDispatch(events);
        }

        dispatches.clear();
    }

    /**
     * Indicate an outbound result.
     * @param events to indicate
     */
    public void processDispatch(UniformPair<EventBean[]> events)
    {
        // Plain all-events delivery
        if (!forClauseDelivery) {
            dispatchInternal(events);
            return;
        }

        // Discrete delivery
        if ((groupDeliveryExpressions == null) || (groupDeliveryExpressions.length == 0)){
            UniformPair<EventBean[]> todeliver = new UniformPair<EventBean[]>(null, null);
            if (events.getFirst() != null) {
                for (EventBean event : events.getFirst()) {
                    todeliver.setFirst(new EventBean[] {event});
                    dispatchInternal(todeliver);
                }
            }
            todeliver.setFirst(null);
            if (events.getSecond() != null) {
                for (EventBean event : events.getSecond()) {
                    todeliver.setSecond(new EventBean[] {event});
                    dispatchInternal(todeliver);
                }
            }
            return;
        }

        // Grouped delivery
        Map<MultiKeyUntyped, UniformPair<EventBean[]>> groups;
        try {
            groups = getGroupedResults(events);
        }
        catch (RuntimeException ex) {
            log.error("Unexpected exception evaluating grouped-delivery expressions: " + ex.getMessage() + ", delivering ungrouped", ex);
            dispatchInternal(events);
            return;
        }

        // Deliver each group separately
        for (Map.Entry<MultiKeyUntyped, UniformPair<EventBean[]>> group : groups.entrySet()) {
            dispatchInternal(group.getValue());
        }
    }

    private Map<MultiKeyUntyped, UniformPair<EventBean[]>> getGroupedResults(UniformPair<EventBean[]> events)
    {
        if (events == null) {
            return Collections.emptyMap();
        }
        Map<MultiKeyUntyped, UniformPair<EventBean[]>> groups = new LinkedHashMap<MultiKeyUntyped, UniformPair<EventBean[]>>();
        EventBean[] eventsPerStream = new EventBean[1];
        getGroupedResults(groups, events.getFirst(), true, eventsPerStream);
        getGroupedResults(groups, events.getSecond(), false, eventsPerStream);
        return groups;
    }

    private void getGroupedResults(Map<MultiKeyUntyped, UniformPair<EventBean[]>> groups, EventBean[] events, boolean insertStream, EventBean[] eventsPerStream)
    {
        if (events == null) {
            return;
        }
        
        for (EventBean event : events) {

            EventBean evalEvent = event;
            if (evalEvent instanceof NaturalEventBean) {
                evalEvent = ((NaturalEventBean) evalEvent).getOptionalSynthetic();
            }

            Object[] keys = new Object[groupDeliveryExpressions.length];
            for (int i = 0; i < groupDeliveryExpressions.length; i++) {
                eventsPerStream[0] = evalEvent;
                keys[i] = groupDeliveryExpressions[i].evaluate(eventsPerStream, true, exprEvaluatorContext);
            }
            MultiKeyUntyped key = new MultiKeyUntyped(keys);

            UniformPair<EventBean[]> groupEntry = groups.get(key);
            if (groupEntry == null) {
                if (insertStream) {
                    groupEntry = new UniformPair<EventBean[]>(new EventBean[] {event}, null);
                }
                else {
                    groupEntry = new UniformPair<EventBean[]>(null, new EventBean[] {event});
                }
                groups.put(key, groupEntry);
            }
            else {
                if (insertStream) {
                    if (groupEntry.getFirst() == null) {
                        groupEntry.setFirst(new EventBean[] {event});
                    }
                    else {
                        groupEntry.setFirst(EventBeanUtility.addToArray(groupEntry.getFirst(), event));
                    }
                }
                else {
                    if (groupEntry.getSecond() == null) {
                        groupEntry.setSecond(new EventBean[] {event});
                    }
                    else {
                        groupEntry.setSecond(EventBeanUtility.addToArray(groupEntry.getSecond(), event));
                    }
                }
            }
        }
    }

    private void dispatchInternal(UniformPair<EventBean[]> events) {
        if (statementResultNaturalStrategy != null)
        {
            statementResultNaturalStrategy.execute(events);
        }

        EventBean[] newEventArr = events != null ? events.getFirst() : null;
        EventBean[] oldEventArr = events != null ? events.getSecond() : null;

        for (UpdateListener listener : statementListenerSet.listeners)
        {
            try
            {
                listener.update(newEventArr, oldEventArr);
            }
            catch (Throwable t)
            {
                String message = "Unexpected exception invoking listener update method on listener class '" + listener.getClass().getSimpleName() +
                        "' : " + t.getClass().getSimpleName() + " : " + t.getMessage();
                log.error(message, t);
            }
        }
        if (!(statementListenerSet.stmtAwareListeners.isEmpty()))
        {
            for (StatementAwareUpdateListener listener : statementListenerSet.getStmtAwareListeners())
            {
                try
                {
                    listener.update(newEventArr, oldEventArr, epStatement, epServiceProvider);
                }
                catch (Throwable t)
                {
                    String message = "Unexpected exception invoking listener update method on listener class '" + listener.getClass().getSimpleName() +
                            "' : " + t.getClass().getSimpleName() + " : " + t.getMessage();
                    log.error(message, t);
                }
            }
        }
        if ((AuditPath.isAuditEnabled) && (!statementOutputHooks.isEmpty()))
        {
            for (StatementResultListener listener : statementOutputHooks)
            {
                listener.update(newEventArr, oldEventArr, epStatement.getName(), epStatement, epServiceProvider);
            }
        }
    }

    /**
     * Dispatches when the statement is stopped any remaining results.
     */
    public void dispatchOnStop()
    {
        lastIterableEvent = null;
        ArrayDeque<UniformPair<EventBean[]>> dispatches = lastResults.get();
        if (dispatches.isEmpty())
        {
            return;
        }
        execute();
    }
}

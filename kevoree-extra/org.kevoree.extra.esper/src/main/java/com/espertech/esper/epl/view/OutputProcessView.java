/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.EventDistinctIterator;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.core.StatementResultListener;
import com.espertech.esper.core.StatementResultService;
import com.espertech.esper.core.UpdateDispatchView;
import com.espertech.esper.epl.core.ResultSetProcessor;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.join.JoinExecutionStrategy;
import com.espertech.esper.epl.join.JoinSetIndicator;
import com.espertech.esper.event.EventBeanReader;
import com.espertech.esper.event.EventBeanReaderDefaultImpl;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.view.View;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Base output processing view that has the responsibility to serve up event type and
 * statement iterator.
 * <p>
 * Implementation classes may enforce an output rate stabilizing or limiting policy.
 */
public abstract class OutputProcessView implements View, JoinSetIndicator
{
    private static final Log log = LogFactory.getLog(OutputProcessView.class);

    private JoinExecutionStrategy joinExecutionStrategy;
    private Long afterConditionTime;
    private Integer afterConditionNumberOfEvents;
    private int afterConditionEventsFound;

    /**
     * Statement services.
     */
    protected  StatementContext statementContext;

    /**
     * If the after-condition is satisfied, if any.
     */
    protected boolean isAfterConditionSatisfied;

    /**
     * Processes the parent views result set generating events for pushing out to child view.
     */
    protected final ResultSetProcessor resultSetProcessor;

    /**
     * Strategy to performs the output once it's decided we need to output.
     */
    protected final OutputStrategy outputStrategy;

    /**
     * Manages listeners/subscribers to a statement, informing about
     * current result generation needs.
     */
    protected final StatementResultService statementResultService;

    /**
     * The view to ultimately dispatch to.
     */
    protected UpdateDispatchView childView;

    /**
     * The parent view for iteration.
     */
    protected Viewable parentView;

    /**
     * An indicator on whether we always need synthetic events such as for insert-into.
     */
    protected boolean isGenerateSynthetic;

    /**
     * Returns the select-distinct indicator.
     */
    protected final boolean isDistinct;

    /**
     * Returns a reader for reading all properties to an event for processing distinct events. 
     */
    protected EventBeanReader eventBeanReader;

    /**
     * Ctor.
     * @param resultSetProcessor processes the results posted by parent view or joins
     * @param outputStrategy the strategy to use for producing output
     * @param isInsertInto true if this is an insert-into
     * @param statementContext for statement-level services
     * @param isDistinct true for distinct
     * @param afterTimePeriod after-keyword time period
     * @param afterConditionNumberOfEvents after-keyword number of events
     */
    protected OutputProcessView(ResultSetProcessor resultSetProcessor, OutputStrategy outputStrategy, boolean isInsertInto, StatementContext statementContext, boolean isDistinct, ExprEvaluator afterTimePeriod, Integer afterConditionNumberOfEvents)
    {
        this.resultSetProcessor = resultSetProcessor;
        this.outputStrategy = outputStrategy;
        this.statementResultService = statementContext.getStatementResultService();
        this.statementContext = statementContext;

        // by default, generate synthetic events only if we insert-into
        this.isGenerateSynthetic = isInsertInto;
        this.isDistinct = isDistinct;
        if (isDistinct)
        {
            if (resultSetProcessor.getResultEventType() instanceof EventTypeSPI)
            {
                EventTypeSPI eventTypeSPI = (EventTypeSPI) resultSetProcessor.getResultEventType();
                eventBeanReader = eventTypeSPI.getReader();
            }
            if (eventBeanReader == null)
            {
                eventBeanReader = new EventBeanReaderDefaultImpl(resultSetProcessor.getResultEventType());
            }
        }

        isAfterConditionSatisfied = true;
        if (afterConditionNumberOfEvents != null)
        {
            this.afterConditionNumberOfEvents = afterConditionNumberOfEvents;
            isAfterConditionSatisfied = false;            
        }
        else if (afterTimePeriod != null)
        {
            isAfterConditionSatisfied = false;
            Object result = afterTimePeriod.evaluate(null, true, statementContext);
            if (result == null)
            {
                log.warn("The expression in the 'after' clause time period has returned a null value, ignoring after-clause");
                isAfterConditionSatisfied = true;
            }
            else
            {
                double sec = ((Number) result).doubleValue();
                long msec = (long) (sec * 1000.0);
                afterConditionTime = statementContext.getTimeProvider().getTime() + msec;
            }
        }
    }



    /**
     * Returns true if the after-condition is satisfied.
     * @param newEvents is the view new events
     * @return indicator for output condition
     */
    public boolean checkAfterCondition(EventBean[] newEvents)
    {
        return isAfterConditionSatisfied || checkAfterCondition(newEvents == null ? 0 : newEvents.length);
    }

    /**
     * Returns true if the after-condition is satisfied.
     * @param newEvents is the join new events
     * @return indicator for output condition
     */
    public boolean checkAfterCondition(Set<MultiKey<EventBean>> newEvents)
    {
        return isAfterConditionSatisfied || checkAfterCondition(newEvents == null ? 0 : newEvents.size());
    }

    /**
     * Returns true if the after-condition is satisfied.
     * @param newOldEvents is the new and old events pair
     * @return indicator for output condition
     */
    public boolean checkAfterCondition(UniformPair<EventBean[]> newOldEvents)
    {
        return isAfterConditionSatisfied || checkAfterCondition(newOldEvents == null ? 0 : (newOldEvents.getFirst() == null ? 0 : newOldEvents.getFirst().length));
    }

    private boolean checkAfterCondition(int numOutputEvents)
    {
        if (afterConditionTime != null)
        {
            long time = statementContext.getTimeProvider().getTime();
            if (time < afterConditionTime)
            {
                return false;
            }

            isAfterConditionSatisfied = true;
            return true;
        }
        else if (afterConditionNumberOfEvents != null)
        {
            afterConditionEventsFound += numOutputEvents;
            if (afterConditionEventsFound <= afterConditionNumberOfEvents)
            {
                return false;
            }

            isAfterConditionSatisfied = true;
            return true;
        }
        else
        {
            isAfterConditionSatisfied = true;
            return true;
        }
    }

    public Viewable getParent() {
        return parentView;
    }

    public void setParent(Viewable parent) {
        this.parentView = parent;
    }

    public View addView(View view) {
        if (childView != null)
        {
            throw new IllegalStateException("Child view has already been supplied");
        }
        childView = (UpdateDispatchView) view;
        return this;
    }

    public List<View> getViews() {
        ArrayList<View> views = new ArrayList<View>();
        if (childView != null)
        {
            views.add(childView);
        }
        return views;
    }

    public void removeAllViews()
    {
        childView = null;
    }

    public boolean removeView(View view) {
        if (view != childView)
        {
            throw new IllegalStateException("Cannot remove child view, view has not been supplied");
        }
        childView = null;
        return true;
    }

    public boolean hasViews() {
        return childView != null;
    }

    public EventType getEventType()
    {
        EventType eventType = resultSetProcessor.getResultEventType();
        if (eventType != null)
        {
            return eventType;
        }
        return parentView.getEventType();
    }

    /**
     * For joins, supplies the join execution strategy that provides iteration over statement results.
     * @param joinExecutionStrategy executes joins including static (non-continuous) joins
     */
    public void setJoinExecutionStrategy(JoinExecutionStrategy joinExecutionStrategy)
    {
        this.joinExecutionStrategy = joinExecutionStrategy;
    }

    public Iterator<EventBean> iterator()
    {
        Iterator<EventBean> iterator;
        EventType eventType;
        if (joinExecutionStrategy != null)
        {
            Set<MultiKey<EventBean>> joinSet = joinExecutionStrategy.staticJoin();
            iterator = resultSetProcessor.getIterator(joinSet);
            eventType = resultSetProcessor.getResultEventType();
        }
        else if(resultSetProcessor != null)
    	{
            iterator = resultSetProcessor.getIterator(parentView);
            eventType = resultSetProcessor.getResultEventType();
    	}
    	else
    	{
    		iterator = parentView.iterator();
            eventType = parentView.getEventType();
    	}

        if (!isDistinct)
        {
            return iterator;
        }
        return new EventDistinctIterator(iterator, eventType);
    }

    /**
     * Indicate statement result.
     * @param newOldEvents result
     */
    public void indicateEarlyReturn(UniformPair<EventBean[]> newOldEvents) {
        if (newOldEvents == null) {
            return;
        }
        if ((statementContext.getMetricReportingService() != null) &&
            (statementContext.getMetricReportingService().getStatementOutputHooks() != null) &&
            (!statementContext.getMetricReportingService().getStatementOutputHooks().isEmpty())) {
            for (StatementResultListener listener : statementContext.getMetricReportingService().getStatementOutputHooks()) {
                listener.update(newOldEvents.getFirst(), newOldEvents.getSecond(), statementContext.getStatementName(), null, null);
            }
        }
    }
}

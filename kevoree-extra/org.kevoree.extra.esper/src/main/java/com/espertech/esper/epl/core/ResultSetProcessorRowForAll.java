/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.*;
import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.spec.OutputLimitLimitType;
import com.espertech.esper.view.Viewable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Result set processor for the case: aggregation functions used in the select clause, and no group-by,
 * and all properties in the select clause are under an aggregation function.
 * <p>
 * This processor does not perform grouping, every event entering and leaving is in the same group.
 * Produces one old event and one new event row every time either at least one old or new event is received.
 * Aggregation state is simply one row holding all the state.
 */
public class ResultSetProcessorRowForAll implements ResultSetProcessor
{
    private final static EventBean[] EVENT_PER_STREAM_EMPTY = new EventBean[0];
    private final boolean isSelectRStream;
    private final boolean isUnidirectional;
    private final SelectExprProcessor selectExprProcessor;
    private final AggregationService aggregationService;
    private final OrderByProcessor orderByProcessor;
    private final ExprEvaluator optionalHavingNode;
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param selectExprProcessor - for processing the select expression and generting the final output rows
     * @param aggregationService - handles aggregation
     * @param optionalHavingNode - having clause expression node
     * @param isSelectRStream - true if remove stream events should be generated
     * @param orderByProcessor - for ordering output events
     * @param isUnidirectional - true if unidirectional join
     * @param exprEvaluatorContext context for expression evalauation
     */
    public ResultSetProcessorRowForAll(SelectExprProcessor selectExprProcessor,
                                       AggregationService aggregationService,
                                       OrderByProcessor orderByProcessor,
                                       ExprEvaluator optionalHavingNode,
                                       boolean isSelectRStream,
                                       boolean isUnidirectional,
                                       ExprEvaluatorContext exprEvaluatorContext)
    {
        this.selectExprProcessor = selectExprProcessor;
        this.aggregationService = aggregationService;
        this.optionalHavingNode = optionalHavingNode;
        this.orderByProcessor = orderByProcessor;
        this.isSelectRStream = isSelectRStream;
        this.isUnidirectional = isUnidirectional;
        this.exprEvaluatorContext = exprEvaluatorContext;
    }

    public EventType getResultEventType()
    {
        return selectExprProcessor.getResultEventType();
    }

    public UniformPair<EventBean[]> processJoinResult(Set<MultiKey<EventBean>> newEvents, Set<MultiKey<EventBean>> oldEvents, boolean isSynthesize)
    {
        EventBean[] selectOldEvents = null;
        EventBean[] selectNewEvents;

        if (isUnidirectional)
        {
            this.clear();
        }

        if (isSelectRStream)
        {
            selectOldEvents = getSelectListEvents(false, isSynthesize);
        }

        if (!newEvents.isEmpty())
        {
            // apply new data to aggregates
            for (MultiKey<EventBean> events : newEvents)
            {
                aggregationService.applyEnter(events.getArray(), null, exprEvaluatorContext);
            }
        }
        if (!oldEvents.isEmpty())
        {
            // apply old data to aggregates
            for (MultiKey<EventBean> events : oldEvents)
            {
                aggregationService.applyLeave(events.getArray(), null, exprEvaluatorContext);
            }
        }

        selectNewEvents = getSelectListEvents(true, isSynthesize);

        if ((selectNewEvents == null) && (selectOldEvents == null))
        {
            return null;
        }
        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    public UniformPair<EventBean[]> processViewResult(EventBean[] newData, EventBean[] oldData, boolean isSynthesize)
    {
        EventBean[] selectOldEvents = null;
        EventBean[] selectNewEvents;

        if (isSelectRStream)
        {
            selectOldEvents = getSelectListEvents(false, isSynthesize);
        }

        EventBean[] eventsPerStream = new EventBean[1];
        if (newData != null)
        {
            // apply new data to aggregates
            for (int i = 0; i < newData.length; i++)
            {
                eventsPerStream[0] = newData[i];
                aggregationService.applyEnter(eventsPerStream, null, exprEvaluatorContext);
            }
        }
        if (oldData != null)
        {
            // apply old data to aggregates
            for (int i = 0; i < oldData.length; i++)
            {
                eventsPerStream[0] = oldData[i];
                aggregationService.applyLeave(eventsPerStream, null, exprEvaluatorContext);
            }
        }

        // generate new events using select expressions
        selectNewEvents = getSelectListEvents(true, isSynthesize);

        if ((selectNewEvents == null) && (selectOldEvents == null))
        {
            return null;
        }

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    private EventBean[] getSelectListEvents(boolean isNewData, boolean isSynthesize)
    {
        // Since we are dealing with strictly aggregation nodes, there are no events required for evaluating
        EventBean event = selectExprProcessor.process(EVENT_PER_STREAM_EMPTY, isNewData, isSynthesize);

        if (optionalHavingNode != null)
        {
            Boolean result = (Boolean) optionalHavingNode.evaluate(null, isNewData, exprEvaluatorContext);
            if ((result == null) || (!result))
            {
                return null;
            }
        }

        // The result is always a single row
        return new EventBean[] {event};
    }

    private EventBean getSelectListEvent(boolean isNewData, boolean isSynthesize)
    {
        // Since we are dealing with strictly aggregation nodes, there are no events required for evaluating
        EventBean event = selectExprProcessor.process(EVENT_PER_STREAM_EMPTY, isNewData, isSynthesize);

        if (optionalHavingNode != null)
        {
            Boolean result = (Boolean) optionalHavingNode.evaluate(null, isNewData, exprEvaluatorContext);
            if ((result == null) || (!result))
            {
                return null;
            }
        }

        // The result is always a single row
        return event;
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        EventBean[] selectNewEvents = getSelectListEvents(true, true);
        if (selectNewEvents == null)
        {
            return new NullIterator();
        }
        return new SingleEventIterator(selectNewEvents[0]);
    }

    public Iterator<EventBean> getIterator(Set<MultiKey<EventBean>> joinSet)
    {
        EventBean[] result = getSelectListEvents(true, true);
        return new ArrayEventIterator(result);
    }

    public void clear()
    {
        aggregationService.clearResults();
    }

    public UniformPair<EventBean[]> processOutputLimitedJoin(List<UniformPair<Set<MultiKey<EventBean>>>> joinEventsSet, boolean generateSynthetic, OutputLimitLimitType outputLimitLimitType)
    {
        if (outputLimitLimitType == OutputLimitLimitType.LAST)
        {
            EventBean lastOldEvent = null;
            EventBean lastNewEvent = null;

            // if empty (nothing to post)
            if (joinEventsSet.isEmpty())
            {
                if (isSelectRStream)
                {
                    lastOldEvent = getSelectListEvent(false, generateSynthetic);
                    lastNewEvent = lastOldEvent;
                }
                else
                {
                    lastNewEvent = getSelectListEvent(false, generateSynthetic);
                }
            }

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                if (isUnidirectional)
                {
                    this.clear();
                }

                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if ((lastOldEvent == null) && (isSelectRStream))
                {
                    lastOldEvent = getSelectListEvent(false, generateSynthetic);
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (MultiKey<EventBean> eventsPerStream : newData)
                    {
                        aggregationService.applyEnter(eventsPerStream.getArray(), null, exprEvaluatorContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (MultiKey<EventBean> eventsPerStream : oldData)
                    {
                        aggregationService.applyLeave(eventsPerStream.getArray(), null, exprEvaluatorContext);
                    }
                }

                lastNewEvent = getSelectListEvent(true, generateSynthetic);
            }

            EventBean[] lastNew = (lastNewEvent != null) ? new EventBean[] {lastNewEvent} : null;
            EventBean[] lastOld = (lastOldEvent != null) ? new EventBean[] {lastOldEvent} : null;

            if ((lastNew == null) && (lastOld == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(lastNew, lastOld);
        }
        else
        {
            List<EventBean> newEvents = new LinkedList<EventBean>();
            List<EventBean> oldEvents = null;
            if (isSelectRStream)
            {
                oldEvents = new LinkedList<EventBean>();
            }

            List<MultiKeyUntyped> newEventsSortKey = null;
            List<MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedList<MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedList<MultiKeyUntyped>();
                }
            }

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                if (isUnidirectional)
                {
                    this.clear();
                }

                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if (isSelectRStream)
                {
                    getSelectListEvent(false, generateSynthetic, oldEvents);
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (MultiKey<EventBean> row : newData)
                    {
                        aggregationService.applyEnter(row.getArray(), null, exprEvaluatorContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (MultiKey<EventBean> row : oldData)
                    {
                        aggregationService.applyLeave(row.getArray(), null, exprEvaluatorContext);
                    }
                }

                getSelectListEvent(false, generateSynthetic, newEvents);
            }

            EventBean[] newEventsArr = (newEvents.isEmpty()) ? null : newEvents.toArray(new EventBean[newEvents.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (oldEvents.isEmpty()) ? null : oldEvents.toArray(new EventBean[oldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, exprEvaluatorContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld,  exprEvaluatorContext);
                }
            }

            if (joinEventsSet.isEmpty())
            {
                if (isSelectRStream)
                {
                    oldEventsArr = getSelectListEvents(false, generateSynthetic);
                }
                newEventsArr = getSelectListEvents(true, generateSynthetic);
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
    }

    public UniformPair<EventBean[]> processOutputLimitedView(List<UniformPair<EventBean[]>> viewEventsList, boolean generateSynthetic, OutputLimitLimitType outputLimitLimitType)
    {
        if (outputLimitLimitType == OutputLimitLimitType.LAST)
        {
            // For last, if there are no events:
            //   As insert stream, return the current value, if matching the having clause
            //   As remove stream, return the current value, if matching the having clause
            // For last, if there are events in the batch:
            //   As insert stream, return the newest value that is matching the having clause
            //   As remove stream, return the oldest value that is matching the having clause

            EventBean lastOldEvent = null;
            EventBean lastNewEvent = null;
            EventBean[] eventsPerStream = new EventBean[1];

            // if empty (nothing to post)
            if (viewEventsList.isEmpty())
            {
                if (isSelectRStream)
                {
                    lastOldEvent = getSelectListEvent(false, generateSynthetic);
                    lastNewEvent = lastOldEvent;
                }
                else
                {
                    lastNewEvent = getSelectListEvent(false, generateSynthetic);
                }
            }

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                if ((lastOldEvent == null) && (isSelectRStream))
                {
                    lastOldEvent = getSelectListEvent(false, generateSynthetic);
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (EventBean aNewData : newData)
                    {
                        eventsPerStream[0] = aNewData;
                        aggregationService.applyEnter(eventsPerStream, null, exprEvaluatorContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (EventBean anOldData : oldData)
                    {
                        eventsPerStream[0] = anOldData;
                        aggregationService.applyLeave(eventsPerStream, null, exprEvaluatorContext);
                    }
                }

                lastNewEvent = getSelectListEvent(false, generateSynthetic);
            }

            EventBean[] lastNew = (lastNewEvent != null) ? new EventBean[] {lastNewEvent} : null;
            EventBean[] lastOld = (lastOldEvent != null) ? new EventBean[] {lastOldEvent} : null;

            if ((lastNew == null) && (lastOld == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(lastNew, lastOld);
        }
        else
        {
            List<EventBean> newEvents = new LinkedList<EventBean>();
            List<EventBean> oldEvents = null;
            if (isSelectRStream)
            {
                oldEvents = new LinkedList<EventBean>();
            }

            List<MultiKeyUntyped> newEventsSortKey = null;
            List<MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedList<MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedList<MultiKeyUntyped>();
                }
            }

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                if (isSelectRStream)
                {
                    getSelectListEvent(false, generateSynthetic, oldEvents);
                }

                EventBean[] eventsPerStream = new EventBean[1];
                if (newData != null)
                {
                    // apply new data to aggregates
                    for (EventBean aNewData : newData)
                    {
                        eventsPerStream[0] = aNewData;
                        aggregationService.applyEnter(eventsPerStream, null, exprEvaluatorContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (EventBean anOldData : oldData)
                    {
                        eventsPerStream[0] = anOldData;
                        aggregationService.applyLeave(eventsPerStream, null, exprEvaluatorContext);
                    }
                }

                getSelectListEvent(true, generateSynthetic, newEvents);
            }

            EventBean[] newEventsArr = (newEvents.isEmpty()) ? null : newEvents.toArray(new EventBean[newEvents.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (oldEvents.isEmpty()) ? null : oldEvents.toArray(new EventBean[oldEvents.size()]);
            }
            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, exprEvaluatorContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, exprEvaluatorContext);
                }
            }

            if (viewEventsList.isEmpty())
            {
                if (isSelectRStream)
                {
                    oldEventsArr = getSelectListEvents(false, generateSynthetic);
                }
                newEventsArr = getSelectListEvents(true, generateSynthetic);
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
    }

    private void getSelectListEvent(boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents)
    {
        // Since we are dealing with strictly aggregation nodes, there are no events required for evaluating
        EventBean event = selectExprProcessor.process(EVENT_PER_STREAM_EMPTY, isNewData, isSynthesize);

        if (optionalHavingNode != null)
        {
            Boolean result = (Boolean) optionalHavingNode.evaluate(null, isNewData, exprEvaluatorContext);
            if ((result == null) || (!result))
            {
                return;
            }
        }

        resultEvents.add(event);
    }
}

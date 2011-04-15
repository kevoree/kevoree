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
import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.spec.OutputLimitLimitType;
import com.espertech.esper.view.Viewable;

import java.util.*;

/**
 * Result set processor for the case: aggregation functions used in the select clause, and no group-by,
 * and not all of the properties in the select clause are under an aggregation function.
 * <p>
 * This processor does not perform grouping, every event entering and leaving is in the same group.
 * The processor generates one row for each event entering (new event) and one row for each event leaving (old event).
 * Aggregation state is simply one row holding all the state.
 */
public class ResultSetProcessorAggregateAll implements ResultSetProcessor
{
    private final SelectExprProcessor selectExprProcessor;
    private final OrderByProcessor orderByProcessor;
    private final AggregationService aggregationService;
    private final ExprEvaluator optionalHavingNode;
    private final boolean isSelectRStream;
    private final boolean isUnidirectional;
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param selectExprProcessor - for processing the select expression and generting the final output rows
     * @param orderByProcessor - for sorting the outgoing events according to the order-by clause
     * @param aggregationService - handles aggregation
     * @param optionalHavingNode - having clause expression node
     * @param isSelectRStream - true if remove stream events should be generated
     * @param isUnidirectional - true if unidirectional join
     * @param exprEvaluatorContext context for expression evalauation
     */
    public ResultSetProcessorAggregateAll(SelectExprProcessor selectExprProcessor,
                                          OrderByProcessor orderByProcessor,
                                          AggregationService aggregationService,
                                          ExprEvaluator optionalHavingNode,
                                          boolean isSelectRStream,
                                          boolean isUnidirectional,
                                          ExprEvaluatorContext exprEvaluatorContext)
    {
        this.selectExprProcessor = selectExprProcessor;
        this.orderByProcessor = orderByProcessor;
        this.aggregationService = aggregationService;
        this.optionalHavingNode = optionalHavingNode;
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

        if (optionalHavingNode == null)
        {
            if (isSelectRStream)
            {
                selectOldEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, oldEvents, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, newEvents, true, isSynthesize, exprEvaluatorContext);
        }
        else
        {
            if (isSelectRStream)
            {
                selectOldEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, oldEvents, optionalHavingNode, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, newEvents, optionalHavingNode, true, isSynthesize, exprEvaluatorContext);
        }

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

        // generate new events using select expressions
        if (optionalHavingNode == null)
        {
            if (isSelectRStream)
            {
                selectOldEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, oldData, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, newData, true, isSynthesize, exprEvaluatorContext);
        }
        else
        {
            if (isSelectRStream)
            {
                selectOldEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, oldData, optionalHavingNode, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, newData, optionalHavingNode, true, isSynthesize, exprEvaluatorContext);
        }

        if ((selectNewEvents == null) && (selectOldEvents == null))
        {
            return null;
        }

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        if (orderByProcessor == null)
        {
            return new ResultSetAggregateAllIterator(parent.iterator(), this, exprEvaluatorContext);
        }

        // Pull all parent events, generate order keys
        EventBean[] eventsPerStream = new EventBean[1];
        List<EventBean> outgoingEvents = new ArrayList<EventBean>();
        List<MultiKeyUntyped> orderKeys = new ArrayList<MultiKeyUntyped>();

        for (EventBean candidate : parent)
        {
            eventsPerStream[0] = candidate;

            Boolean pass = true;
            if (optionalHavingNode != null)
            {
                pass = (Boolean) optionalHavingNode.evaluate(eventsPerStream, true, exprEvaluatorContext);
            }
            if ((pass == null) || (!pass))
            {
                continue;
            }

            outgoingEvents.add(selectExprProcessor.process(eventsPerStream, true, true));

            MultiKeyUntyped orderKey = orderByProcessor.getSortKey(eventsPerStream, true, exprEvaluatorContext);
            orderKeys.add(orderKey);
        }

        // sort
        EventBean[] outgoingEventsArr = outgoingEvents.toArray(new EventBean[outgoingEvents.size()]);
        MultiKeyUntyped[] orderKeysArr = orderKeys.toArray(new MultiKeyUntyped[orderKeys.size()]);
        EventBean[] orderedEvents = orderByProcessor.sort(outgoingEventsArr, orderKeysArr, exprEvaluatorContext);

        return new ArrayEventIterator(orderedEvents);
    }

    /**
     * Returns the select expression processor
     * @return select processor.
     */
    public SelectExprProcessor getSelectExprProcessor()
    {
        return selectExprProcessor;
    }

    /**
     * Returns the optional having expression.
     * @return having expression node
     */
    public ExprEvaluator getOptionalHavingNode()
    {
        return optionalHavingNode;
    }

    public Iterator<EventBean> getIterator(Set<MultiKey<EventBean>> joinSet)
    {
        EventBean[] result;
        if (optionalHavingNode == null)
        {
            result = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, joinSet, true, true, exprEvaluatorContext);
        }
        else
        {
            result = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, joinSet, optionalHavingNode, true, true, exprEvaluatorContext);
        }
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

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if (isUnidirectional)
                {
                    this.clear();
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

                EventBean[] selectOldEvents;
                if (isSelectRStream)
                {
                    if (optionalHavingNode == null)
                    {
                        selectOldEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, oldData, false, generateSynthetic);
                    }
                    else
                    {
                        selectOldEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, oldData, optionalHavingNode, false, generateSynthetic, exprEvaluatorContext);
                    }
                    if ((selectOldEvents != null) && (selectOldEvents.length > 0))
                    {
                        lastOldEvent = selectOldEvents[selectOldEvents.length - 1];
                    }
                }

                // generate new events using select expressions
                EventBean[] selectNewEvents;
                if (optionalHavingNode == null)
                {
                    selectNewEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, newData, true, generateSynthetic);
                }
                else
                {
                    selectNewEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, newData, optionalHavingNode, true, generateSynthetic, exprEvaluatorContext);
                }
                if ((selectNewEvents != null) && (selectNewEvents.length > 0))
                {
                    lastNewEvent = selectNewEvents[selectNewEvents.length - 1];
                }
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
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if (isUnidirectional)
                {
                    this.clear();
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

                // generate old events using select expressions
                if (isSelectRStream)
                {
                    if (optionalHavingNode == null)
                    {
                        ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, oldData, false, generateSynthetic, oldEvents, oldEventsSortKey, exprEvaluatorContext);
                    }
                    // generate old events using having then select
                    else
                    {
                        ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, oldData, optionalHavingNode, false, generateSynthetic, oldEvents, oldEventsSortKey, exprEvaluatorContext);
                    }
                }

                // generate new events using select expressions
                if (optionalHavingNode == null)
                {
                    ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, newData, true, generateSynthetic, newEvents, newEventsSortKey, exprEvaluatorContext);
                }
                else
                {
                    ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, newData, optionalHavingNode, true, generateSynthetic, newEvents, newEventsSortKey, exprEvaluatorContext);
                }
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
            EventBean lastOldEvent = null;
            EventBean lastNewEvent = null;
            EventBean[] eventsPerStream = new EventBean[1];

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

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
                        aggregationService.applyLeave(eventsPerStream, null,exprEvaluatorContext);
                    }
                }

                EventBean[] selectOldEvents;
                if (isSelectRStream)
                {
                    if (optionalHavingNode == null)
                    {
                        selectOldEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, oldData, false, generateSynthetic);
                    }
                    else
                    {
                        selectOldEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, oldData, optionalHavingNode, false, generateSynthetic, exprEvaluatorContext);
                    }
                    if ((selectOldEvents != null) && (selectOldEvents.length > 0))
                    {
                        lastOldEvent = selectOldEvents[selectOldEvents.length - 1];
                    }
                }

                // generate new events using select expressions
                EventBean[] selectNewEvents;
                if (optionalHavingNode == null)
                {
                    selectNewEvents = ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, newData, true, generateSynthetic);
                }
                else
                {
                    selectNewEvents = ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, newData, optionalHavingNode, true, generateSynthetic, exprEvaluatorContext);
                }
                if ((selectNewEvents != null) && (selectNewEvents.length > 0))
                {
                    lastNewEvent = selectNewEvents[selectNewEvents.length - 1];
                }
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

                // generate old events using select expressions
                if (isSelectRStream)
                {
                    if (optionalHavingNode == null)
                    {
                        ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, oldData, false, generateSynthetic, oldEvents, oldEventsSortKey, exprEvaluatorContext);
                    }
                    // generate old events using having then select
                    else
                    {
                        ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, oldData, optionalHavingNode, false, generateSynthetic, oldEvents, oldEventsSortKey, exprEvaluatorContext);
                    }
                }

                // generate new events using select expressions
                if (optionalHavingNode == null)
                {
                    ResultSetProcessorSimple.getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, newData, true, generateSynthetic, newEvents, newEventsSortKey, exprEvaluatorContext);
                }
                else
                {
                    ResultSetProcessorSimple.getSelectEventsHaving(selectExprProcessor, orderByProcessor, newData, optionalHavingNode, true, generateSynthetic, newEvents, newEventsSortKey, exprEvaluatorContext);
                }
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

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
    }
}

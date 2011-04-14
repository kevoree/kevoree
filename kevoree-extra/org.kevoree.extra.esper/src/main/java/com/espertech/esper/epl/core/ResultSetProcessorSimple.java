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
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Result set processor for the simplest case: no aggregation functions used in the select clause, and no group-by.
 * <p>
 * The processor generates one row for each event entering (new event) and one row for each event leaving (old event).
 */
public class ResultSetProcessorSimple extends ResultSetProcessorBaseSimple
{
    private static final Log log = LogFactory.getLog(ResultSetProcessorSimple.class);

    private final boolean isSelectRStream;
    private final SelectExprProcessor selectExprProcessor;
    private final OrderByProcessor orderByProcessor;
    private final ExprEvaluator optionalHavingExpr;
    private final Set<MultiKey<EventBean>> emptyRowSet = new HashSet<MultiKey<EventBean>>();
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param selectExprProcessor - for processing the select expression and generting the final output rows
     * @param orderByProcessor - for sorting the outgoing events according to the order-by clause
     * @param optionalHavingNode - having clause expression node
     * @param isSelectRStream - true if remove stream events should be generated
     * @param exprEvaluatorContext context for expression evalauation
     */
    public ResultSetProcessorSimple(SelectExprProcessor selectExprProcessor,
                                    OrderByProcessor orderByProcessor,
                                    ExprEvaluator optionalHavingNode,
                                    boolean isSelectRStream,
                                    ExprEvaluatorContext exprEvaluatorContext)
    {
        this.selectExprProcessor = selectExprProcessor;
        this.orderByProcessor = orderByProcessor;
        this.optionalHavingExpr = optionalHavingNode;
        this.isSelectRStream = isSelectRStream;
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

        if (optionalHavingExpr == null)
        {
            if (isSelectRStream)
            {
                selectOldEvents = getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, oldEvents, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, newEvents, true, isSynthesize, exprEvaluatorContext);
        }
        else
        {
            if (isSelectRStream)
            {
                selectOldEvents = getSelectEventsHaving(selectExprProcessor, orderByProcessor, oldEvents, optionalHavingExpr, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = getSelectEventsHaving(selectExprProcessor, orderByProcessor, newEvents, optionalHavingExpr, true, isSynthesize, exprEvaluatorContext);
        }

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    public UniformPair<EventBean[]> processViewResult(EventBean[] newData, EventBean[] oldData, boolean isSynthesize)
    {
        EventBean[] selectOldEvents = null;
        EventBean[] selectNewEvents;
        if (optionalHavingExpr == null)
        {
            if (isSelectRStream)
            {
                selectOldEvents = getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, oldData, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = getSelectEventsNoHaving(selectExprProcessor, orderByProcessor, newData, true, isSynthesize, exprEvaluatorContext);
        }
        else
        {
            if (isSelectRStream)
            {
                selectOldEvents = getSelectEventsHaving(selectExprProcessor, orderByProcessor, oldData, optionalHavingExpr, false, isSynthesize, exprEvaluatorContext);
            }
            selectNewEvents = getSelectEventsHaving(selectExprProcessor, orderByProcessor, newData, optionalHavingExpr, true, isSynthesize, exprEvaluatorContext);
        }

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    /**
     * Process view results for the iterator.
     * @param newData new events
     * @return pair of insert and remove stream
     */
    public UniformPair<EventBean[]> processViewResultIterator(EventBean[] newData)
    {
        EventBean[] selectOldEvents = null;
        EventBean[] selectNewEvents;
        if (optionalHavingExpr == null)
        {
            selectNewEvents = getSelectEventsNoHaving(selectExprProcessor, null, newData, true, true, exprEvaluatorContext);
        }
        else
        {
            selectNewEvents = getSelectEventsHaving(selectExprProcessor, null, newData, optionalHavingExpr, true, true, exprEvaluatorContext);
        }

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - orders the outgoing events according to the order-by clause
     * @param events - input events
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param exprEvaluatorContext context for expression evalauation
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsNoHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, EventBean[] events, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (events == null)
        {
            return null;
        }

        EventBean[] result = new EventBean[events.length];
        EventBean[][] eventGenerators = null;
        if(orderByProcessor != null)
        {
            eventGenerators = new EventBean[events.length][];
        }

        EventBean[] eventsPerStream = new EventBean[1];
        for (int i = 0; i < events.length; i++)
        {
            eventsPerStream[0] = events[i];

            // Wildcard select case
            if(exprProcessor == null)
            {
                result[i] = events[i];
            }
            else
            {
                result[i] = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            }

            if(orderByProcessor != null)
            {
                  eventGenerators[i] = new EventBean[] {events[i]};
            }
        }

        if(orderByProcessor != null)
        {
            return orderByProcessor.sort(result, eventGenerators, isNewData, exprEvaluatorContext);
        }
        else
        {
            return result;
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - for sorting output events according to the order-by clause
     * @param events - input events
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param exprEvaluatorContext context for expression evalauation
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsNoHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, Set<MultiKey<EventBean>> events, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext)
    {
        if ((events == null) || (events.isEmpty()))
        {
            return null;
        }
        int length = events.size();

        EventBean[] result = new EventBean[length];
        EventBean[][] eventGenerators = null;
        if(orderByProcessor != null)
        {
            eventGenerators = new EventBean[length][];
        }

        int count = 0;
        for (MultiKey<EventBean> key : events)
        {
            EventBean[] eventsPerStream = key.getArray();
            result[count] = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            if(orderByProcessor != null)
            {
                eventGenerators[count] = eventsPerStream;
            }
            count++;
        }

        if(orderByProcessor != null)
        {
            return orderByProcessor.sort(result, eventGenerators, isNewData, exprEvaluatorContext);
        }
        else
        {
            return result;
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * <p>
     * Also applies a having clause.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - for sorting output events according to the order-by clause
     * @param events - input events
     * @param optionalHavingNode - supplies the having-clause expression
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param exprEvaluatorContext context for expression evalauation
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, EventBean[] events, ExprEvaluator optionalHavingNode, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (events == null)
        {
            return null;
        }

        LinkedList<EventBean> result = new LinkedList<EventBean>();
        List<EventBean[]> eventGenerators = null;
        if(orderByProcessor != null)
        {
            eventGenerators = new ArrayList<EventBean[]>();
        }

        EventBean[] eventsPerStream = new EventBean[1];
        for (EventBean event : events)
        {
            eventsPerStream[0] = event;

            Boolean passesHaving = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if ((passesHaving == null) || (!passesHaving))
            {
                continue;
            }

            result.add(exprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if (orderByProcessor != null)
            {
                eventGenerators.add(new EventBean[]{event});
            }
        }

        if (!result.isEmpty())
        {
            if(orderByProcessor != null)
            {
                return orderByProcessor.sort(result.toArray(new EventBean[result.size()]), eventGenerators.toArray(new EventBean[eventGenerators.size()][]), isNewData, exprEvaluatorContext);
            }
            else
            {
                return result.toArray(new EventBean[result.size()]);
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * <p>
     * Also applies a having clause.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - for sorting output events according to the order-by clause
     * @param events - input events
     * @param optionalHavingNode - supplies the having-clause expression
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param exprEvaluatorContext context for expression evalauation
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, Set<MultiKey<EventBean>> events, ExprEvaluator optionalHavingNode, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext)
    {
        if ((events == null) || (events.isEmpty()))
        {
            return null;
        }

        LinkedList<EventBean> result = new LinkedList<EventBean>();
        List<EventBean[]> eventGenerators = null;
        if(orderByProcessor != null)
        {
            eventGenerators = new ArrayList<EventBean[]>();
        }

        for (MultiKey<EventBean> key : events)
        {
            EventBean[] eventsPerStream = key.getArray();

            Boolean passesHaving = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if ((passesHaving == null) || (!passesHaving))
            {
                continue;
            }

            EventBean resultEvent = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            result.add(resultEvent);
            if(orderByProcessor != null)
            {
                eventGenerators.add(eventsPerStream);
            }
        }

        if (!result.isEmpty())
        {
            if(orderByProcessor != null)
            {
                return orderByProcessor.sort(result.toArray(new EventBean[result.size()]), eventGenerators.toArray(new EventBean[eventGenerators.size()][]), isNewData, exprEvaluatorContext);
            }
            else
            {
                return result.toArray(new EventBean[result.size()]);
            }
        }
        else
        {
            return null;
        }
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        if (orderByProcessor != null)
        {
            // Pull all events, generate order keys
            EventBean[] eventsPerStream = new EventBean[1];
            List<EventBean> events = new ArrayList<EventBean>();
            List<MultiKeyUntyped> orderKeys = new ArrayList<MultiKeyUntyped>();
            for (EventBean aParent : parent)
            {
                eventsPerStream[0] = aParent;
                MultiKeyUntyped orderKey = orderByProcessor.getSortKey(eventsPerStream, true, exprEvaluatorContext);
                UniformPair<EventBean[]> pair = processViewResultIterator(eventsPerStream);
                EventBean[] result = pair.getFirst();
                if (result.length != 0)
                {
                    events.add(result[0]);
                }
                orderKeys.add(orderKey);
            }

            // sort
            EventBean[] outgoingEvents = events.toArray(new EventBean[events.size()]);
            MultiKeyUntyped[] orderKeysArr = orderKeys.toArray(new MultiKeyUntyped[orderKeys.size()]);
            EventBean[] orderedEvents = orderByProcessor.sort(outgoingEvents, orderKeysArr, exprEvaluatorContext);

            return new ArrayEventIterator(orderedEvents);
        }
        // Return an iterator that gives row-by-row a result
        return new TransformEventIterator(parent.iterator(), new ResultSetProcessorSimpleTransform(this));
    }

    public Iterator<EventBean> getIterator(Set<MultiKey<EventBean>> joinSet)
    {
        // Process join results set as a regular join, includes sorting and having-clause filter
        UniformPair<EventBean[]> result = processJoinResult(joinSet, emptyRowSet, true);
        return new ArrayEventIterator(result.getFirst());
    }

    public void clear()
    {
        // No need to clear state, there is no state held
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * @param exprProcessor - processes each input event and returns output event
     * @param events - input events
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsNoHaving(SelectExprProcessor exprProcessor, EventBean[] events, boolean isNewData, boolean isSynthesize)
    {
        if (events == null)
        {
            return null;
        }

        EventBean[] result = new EventBean[events.length];
        EventBean[] eventsPerStream = new EventBean[1];
        for (int i = 0; i < events.length; i++)
        {
            eventsPerStream[0] = events[i];
            result[i] = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
        }

        return result;
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * @param exprProcessor - processes each input event and returns output event
     * @param events - input events
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsNoHaving(SelectExprProcessor exprProcessor, Set<MultiKey<EventBean>> events, boolean isNewData, boolean isSynthesize)
    {
        if ((events == null) || (events.isEmpty()))
        {
            return null;
        }
        int length = events.size();

        EventBean[] result = new EventBean[length];

        int count = 0;
        for (MultiKey<EventBean> key : events)
        {
            EventBean[] eventsPerStream = key.getArray();
            result[count] = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            count++;
        }

        return result;
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * <p>
     * Also applies a having clause.
     * @param exprProcessor - processes each input event and returns output event
     * @param events - input events
     * @param optionalHavingNode - supplies the having-clause expression
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param exprEvaluatorContext context for expression evalauation
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsHaving(SelectExprProcessor exprProcessor, EventBean[] events, ExprEvaluator optionalHavingNode, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (events == null)
        {
            return null;
        }

        LinkedList<EventBean> result = new LinkedList<EventBean>();

        EventBean[] eventsPerStream = new EventBean[1];
        for (EventBean event : events)
        {
            eventsPerStream[0] = event;

            Boolean passesHaving = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if ((passesHaving == null) || (!passesHaving))
            {
                continue;
            }

            result.add(exprProcessor.process(eventsPerStream, isNewData, isSynthesize));
        }

        if (!result.isEmpty())
        {
            return result.toArray(new EventBean[result.size()]);
        }
        else
        {
            return null;
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * <p>
     * Also applies a having clause.
     * @param exprProcessor - processes each input event and returns output event
     * @param events - input events
     * @param optionalHavingNode - supplies the having-clause expression
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param exprEvaluatorContext context for expression evalauation
     * @return output events, one for each input event
     */
    protected static EventBean[] getSelectEventsHaving(SelectExprProcessor exprProcessor, Set<MultiKey<EventBean>> events, ExprEvaluator optionalHavingNode, boolean isNewData, boolean isSynthesize, ExprEvaluatorContext exprEvaluatorContext)
    {
        LinkedList<EventBean> result = new LinkedList<EventBean>();

        for (MultiKey<EventBean> key : events)
        {
            EventBean[] eventsPerStream = key.getArray();

            Boolean passesHaving = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if ((passesHaving == null) || (!passesHaving))
            {
                continue;
            }

            EventBean resultEvent = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            result.add(resultEvent);
        }

        if (!result.isEmpty())
        {
            return result.toArray(new EventBean[result.size()]);
        }
        else
        {
            return null;
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - orders the outgoing events according to the order-by clause
     * @param events - input events
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param result is the result event list to populate
     * @param exprEvaluatorContext context for expression evalauation
     * @param optSortKeys is the result sort key list to populate, for sorting
     */
    protected static void getSelectEventsNoHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, EventBean[] events, boolean isNewData, boolean isSynthesize, List<EventBean> result, List<MultiKeyUntyped> optSortKeys, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (events == null)
        {
            return;
        }

        EventBean[] eventsPerStream = new EventBean[1];
        for (EventBean event : events)
        {
            eventsPerStream[0] = event;

            result.add(exprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if (orderByProcessor != null)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, exprEvaluatorContext));
            }
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - for sorting output events according to the order-by clause
     * @param events - input events
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param result is the result event list to populate
     * @param optSortKeys is the result sort key list to populate, for sorting
     * @param exprEvaluatorContext context for expression evalauation
     */
    protected static void getSelectEventsNoHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, Set<MultiKey<EventBean>> events, boolean isNewData, boolean isSynthesize, List<EventBean> result, List<MultiKeyUntyped> optSortKeys, ExprEvaluatorContext exprEvaluatorContext)
    {
        int length = (events != null) ? events.size() : 0;
        if (length == 0)
        {
            return;
        }

        for (MultiKey<EventBean> key : events)
        {
            EventBean[] eventsPerStream = key.getArray();
            result.add(exprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if(orderByProcessor != null)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, exprEvaluatorContext));
            }
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * <p>
     * Also applies a having clause.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - for sorting output events according to the order-by clause
     * @param events - input events
     * @param optionalHavingNode - supplies the having-clause expression
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param result is the result event list to populate
     * @param exprEvaluatorContext context for expression evalauation
     * @param optSortKeys is the result sort key list to populate, for sorting
     */
    protected static void getSelectEventsHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, EventBean[] events, ExprEvaluator optionalHavingNode, boolean isNewData, boolean isSynthesize, List<EventBean> result, List<MultiKeyUntyped> optSortKeys, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (events == null)
        {
            return;
        }

        EventBean[] eventsPerStream = new EventBean[1];
        for (EventBean event : events)
        {
            eventsPerStream[0] = event;

            Boolean passesHaving = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if ((passesHaving == null) || (!passesHaving))
            {
                continue;
            }

            result.add(exprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if (orderByProcessor != null)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, exprEvaluatorContext));
            }
        }
    }

    /**
     * Applies the select-clause to the given events returning the selected events. The number of events stays the
     * same, i.e. this method does not filter it just transforms the result set.
     * <p>
     * Also applies a having clause.
     * @param exprProcessor - processes each input event and returns output event
     * @param orderByProcessor - for sorting output events according to the order-by clause
     * @param events - input events
     * @param optionalHavingNode - supplies the having-clause expression
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param isSynthesize - set to true to indicate that synthetic events are required for an iterator result set
     * @param result is the result event list to populate
     * @param optSortKeys is the result sort key list to populate, for sorting
     * @param exprEvaluatorContext context for expression evalauation
     */
    protected static void getSelectEventsHaving(SelectExprProcessor exprProcessor, OrderByProcessor orderByProcessor, Set<MultiKey<EventBean>> events, ExprEvaluator optionalHavingNode, boolean isNewData, boolean isSynthesize, List<EventBean> result, List<MultiKeyUntyped> optSortKeys, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (events == null)
        {
            return;
        }
        for (MultiKey<EventBean> key : events)
        {
            EventBean[] eventsPerStream = key.getArray();

            Boolean passesHaving = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if ((passesHaving == null) || (!passesHaving))
            {
                continue;
            }

            EventBean resultEvent = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            result.add(resultEvent);
            if(orderByProcessor != null)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, exprEvaluatorContext));
            }
        }
    }
}

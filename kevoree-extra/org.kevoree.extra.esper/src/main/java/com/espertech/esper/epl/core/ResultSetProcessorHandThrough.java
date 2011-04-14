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
import com.espertech.esper.collection.TransformEventIterator;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Result set processor for the hand-through case:
 * no aggregation functions used in the select clause, and no group-by, no having and ordering.
 */
public class ResultSetProcessorHandThrough extends ResultSetProcessorBaseSimple
{
    private static final Log log = LogFactory.getLog(ResultSetProcessorHandThrough.class);

    private final boolean isSelectRStream;
    private final SelectExprProcessor selectExprProcessor;
    private final Set<MultiKey<EventBean>> emptyRowSet = new HashSet<MultiKey<EventBean>>();

    /**
     * Ctor.
     * @param selectExprProcessor - for processing the select expression and generting the final output rows
     * a row per group even if groups didn't change
     * @param isSelectRStream - true if remove stream events should be generated
     */
    public ResultSetProcessorHandThrough(SelectExprProcessor selectExprProcessor, boolean isSelectRStream)
    {
        this.selectExprProcessor = selectExprProcessor;
        this.isSelectRStream = isSelectRStream;
    }

    public EventType getResultEventType()
    {
        return selectExprProcessor.getResultEventType();
    }

    public UniformPair<EventBean[]> processJoinResult(Set<MultiKey<EventBean>> newEvents, Set<MultiKey<EventBean>> oldEvents, boolean isSynthesize)
    {
        EventBean[] selectOldEvents = null;
        EventBean[] selectNewEvents;

        if (isSelectRStream)
        {
            selectOldEvents = getSelectEventsNoHaving(selectExprProcessor, oldEvents, false, isSynthesize);
        }
        selectNewEvents = getSelectEventsNoHaving(selectExprProcessor, newEvents, true, isSynthesize);

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
    }

    public UniformPair<EventBean[]> processViewResult(EventBean[] newData, EventBean[] oldData, boolean isSynthesize)
    {
        EventBean[] selectOldEvents = null;

        if (isSelectRStream)
        {
            selectOldEvents = getSelectEventsNoHaving(selectExprProcessor, oldData, false, isSynthesize);
        }
        EventBean[] selectNewEvents = getSelectEventsNoHaving(selectExprProcessor, newData, true, isSynthesize);

        return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
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

            // Wildcard select case
            if(exprProcessor == null)
            {
                result[i] = events[i];
            }
            else
            {
                result[i] = exprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            }
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
        int length = events.size();
        if (length == 0)
        {
            return null;
        }

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


    public void clear()
    {
        // No need to clear state, there is no state held
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        // Return an iterator that gives row-by-row a result
        return new TransformEventIterator(parent.iterator(), new ResultSetProcessorSimpleTransform(this));
    }

    public Iterator<EventBean> getIterator(Set<MultiKey<EventBean>> joinSet)
    {
        // Process join results set as a regular join, includes sorting and having-clause filter
        UniformPair<EventBean[]> result = processJoinResult(joinSet, emptyRowSet, true);
        return new ArrayEventIterator(result.getFirst());
    }
}

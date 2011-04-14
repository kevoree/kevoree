/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.ArrayEventIterator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Implementation of access function for single-stream (not joins).
 */
public class AggregationAccessJoinImpl implements AggregationAccess
{
    private int streamId;
    private LinkedHashMap<EventBean, Integer> refSet = new LinkedHashMap<EventBean, Integer>();
    private EventBean[] array;

    /**
     * Ctor.
     * @param streamId stream id
     */
    public AggregationAccessJoinImpl(int streamId)
    {
        this.streamId = streamId;
    }

    public void clear() {
        refSet.clear();
        array = null;
    }

    public void applyEnter(EventBean[] eventsPerStream)
    {
        EventBean event = eventsPerStream[streamId];
        if (event == null) {
            return;
        }
        array = null;
        Integer value = refSet.get(event);
        if (value == null)
        {
            refSet.put(event, 1);
            return;
        }

        value++;
        refSet.put(event, value);
    }

    public void applyLeave(EventBean[] eventsPerStream)
    {
        EventBean event = eventsPerStream[streamId];
        if (event == null) {
            return;
        }
        array = null;

        Integer value = refSet.get(event);
        if (value == null)
        {
            return;
        }

        if (value == 1)
        {
            refSet.remove(event);
            return;
        }

        value--;
        refSet.put(event, value);
    }

    public EventBean getFirstNthValue(int index) {
        if (index < 0) {
            return null;
        }
        if (refSet.isEmpty()) {
            return null;
        }
        if (index >= refSet.size()) {
            return null;
        }
        if (array == null) {
            initArray();
        }
        return array[index];
    }

    public EventBean getLastNthValue(int index) {
        if (index < 0) {
            return null;
        }
        if (refSet.isEmpty()) {
            return null;
        }
        if (index >= refSet.size()) {
            return null;
        }
        if (array == null) {
            initArray();
        }
        return array[array.length - index - 1];
    }

    public EventBean getFirstValue() {
        if (refSet.isEmpty()) {
            return null;
        }
        return refSet.entrySet().iterator().next().getKey();
    }

    public EventBean getLastValue()
    {
        if (refSet.isEmpty()) {
            return null;
        }
        if (array == null) {
            initArray();
        }
        return array[array.length - 1];
    }

    public Iterator<EventBean> iterator()
    {
        if (array == null) {
            initArray();
        }
        return new ArrayEventIterator(array);
    }

    public int size()
    {
        return refSet.size();
    }

    private void initArray() {
        Set<EventBean> events = refSet.keySet();
        array = events.toArray(new EventBean[events.size()]);
    }
}
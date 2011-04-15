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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Implementation of access function for single-stream (not joins).
 */
public class AggregationAccessImpl implements AggregationAccess
{
    private int streamId;
    private ArrayList<EventBean> events = new ArrayList<EventBean>();

    /**
     * Ctor.
     * @param streamId stream id
     */
    public AggregationAccessImpl(int streamId)
    {
        this.streamId = streamId;
    }

    public void clear() {
        events.clear();
    }

    public void applyLeave(EventBean[] eventsPerStream)
    {
        EventBean event = eventsPerStream[streamId];
        if (event == null) {
            return;
        }
        events.remove(event);
    }

    public void applyEnter(EventBean[] eventsPerStream)
    {
        EventBean event = eventsPerStream[streamId];
        if (event == null) {
            return;
        }
        events.add(event);
    }

    public EventBean getFirstNthValue(int index)
    {
        if (index < 0) {
            return null;
        }
        if (index >= events.size()) {
            return null;
        }
        return events.get(index);
    }

    public EventBean getLastNthValue(int index) {
        if (index < 0) {
            return null;
        }
        if (index >= events.size()) {
            return null;
        }
        return events.get(events.size() - index - 1);
    }

    public EventBean getFirstValue() {
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0);
    }

    public EventBean getLastValue()
    {
        if (events.isEmpty()) {
            return null;
        }
        return events.get(events.size() - 1);
    }

    public Iterator<EventBean> iterator()
    {
        return events.iterator();
    }

    public int size()
    {
        return events.size();
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.table;

import com.espertech.esper.client.EventBean;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;

/**
 * Simple table of events without an index.
 */
public class UnindexedEventTable implements EventTable
{
    private final int streamNum;
    private Set<EventBean> eventSet = new HashSet<EventBean>();

    /**
     * Ctor.
     * @param streamNum is the indexed stream's number
     */
    public UnindexedEventTable(int streamNum)
    {
        this.streamNum = streamNum;
    }

    public void clear()
    {
        eventSet.clear();
    }

    public void add(EventBean[] addEvents)
    {
        if (addEvents == null)
        {
            return;
        }

        eventSet.addAll(Arrays.asList(addEvents));
    }

    public void remove(EventBean[] removeEvents)
    {
        if (removeEvents == null)
        {
            return;
        }

        for (int i = 0; i < removeEvents.length; i++)
        {
            eventSet.remove(removeEvents[i]);
        }
    }

    public boolean isEmpty()
    {
        return eventSet.isEmpty();
    }

    /**
     * Returns events in table.
     * @return all events
     */
    public Set<EventBean> getEventSet()
    {
        return eventSet;
    }

    public Iterator<EventBean> iterator()
    {
        return eventSet.iterator();
    }

    public String toString()
    {
        return "UnindexedEventTable streamNum=" + streamNum;
    }
}

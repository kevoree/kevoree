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
import com.espertech.esper.collection.NullIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/**
 * Simple table of events without an index, based on a List implementation rather then a set
 * since we know there cannot be duplicates (such as a poll returning individual rows).
 */
public class UnindexedEventTableList implements EventTable
{
    private static final NullIterator<EventBean> emptyIterator = new NullIterator<EventBean>();
    private List<EventBean> eventSet;

    /**
     * Ctor.
     * @param eventSet is a list initializing the table
     */
    public UnindexedEventTableList(List<EventBean> eventSet)
    {
        this.eventSet = eventSet;
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

        for (EventBean removeEvent : removeEvents)
        {
            eventSet.remove(removeEvent);
        }
    }

    public Iterator<EventBean> iterator()
    {
        if (eventSet == null)
        {
            return emptyIterator;
        }
        return eventSet.iterator();
    }

    public boolean isEmpty()
    {
        return eventSet.isEmpty();
    }

    public String toString()
    {
        return "UnindexedEventTableList";
    }

    public void clear()
    {
        eventSet.clear();
    }
}

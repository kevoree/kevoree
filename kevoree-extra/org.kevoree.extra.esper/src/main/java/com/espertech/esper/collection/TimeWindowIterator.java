/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.collection;

import java.util.*;
import com.espertech.esper.client.EventBean;

/**
 * Iterator for {@link TimeWindow} to iterate over a timestamp slots that hold events.
 */
public final class TimeWindowIterator implements Iterator<EventBean>
{
    private final Iterator<Pair<Long, ArrayDeque<EventBean>>> keyIterator;
    private Iterator<EventBean> currentListIterator;

    /**
     * Ctor.
     * @param window is the time-slotted collection
     */
    public TimeWindowIterator(ArrayDeque<Pair<Long, ArrayDeque<EventBean>>> window)
    {
        keyIterator = window.iterator();
        if (keyIterator.hasNext())
        {
            // Position to the next filled list
            Pair<Long, ArrayDeque<EventBean>> pair = keyIterator.next();
            while((pair.getSecond().isEmpty()) && (keyIterator.hasNext()))
            {
                pair = keyIterator.next();
            }
            currentListIterator = pair.getSecond().iterator();
        }
    }

    public final EventBean next()
    {
        if (currentListIterator == null)
        {
            throw new NoSuchElementException();
        }

        EventBean eventBean = currentListIterator.next();

        if (!currentListIterator.hasNext())
        {
            currentListIterator = null;
            if (keyIterator.hasNext())
            {
                // Position to the next filled list
                Pair<Long, ArrayDeque<EventBean>> pair = keyIterator.next();
                while((pair.getSecond().isEmpty()) && (keyIterator.hasNext()))
                {
                    pair = keyIterator.next();
                }
                currentListIterator = pair.getSecond().iterator();
            }
        }

        return eventBean;
    }

    public final void remove()
    {
        throw new UnsupportedOperationException();
    }

    public final boolean hasNext()
    {
        if (currentListIterator == null)
        {
            return false;
        }

        if (currentListIterator.hasNext())
        {
            return true;
        }

        currentListIterator = null;

        if (!keyIterator.hasNext())
        {
            return false;
        }

        return true;
    }
}

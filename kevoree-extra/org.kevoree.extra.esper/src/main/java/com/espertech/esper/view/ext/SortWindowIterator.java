/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.ext;

import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.client.EventBean;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.NoSuchElementException;

/**
 * Iterator for use by {@link SortWindowView}.
 */
public final class SortWindowIterator implements Iterator<EventBean>
{
    private final SortedMap<MultiKeyUntyped, LinkedList<EventBean>> window;

    private final Iterator<MultiKeyUntyped> keyIterator;
    private Iterator<EventBean> currentListIterator;

    /**
     * Ctor.
     * @param window - sorted map with events
     */
    public SortWindowIterator(SortedMap<MultiKeyUntyped, LinkedList<EventBean>> window)
    {
        this.window = window;
        keyIterator = window.keySet().iterator();
        if (keyIterator.hasNext())
        {
            MultiKeyUntyped initialKey = keyIterator.next();
            currentListIterator = window.get(initialKey).iterator();
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
                MultiKeyUntyped nextKey = keyIterator.next();
                currentListIterator = window.get(nextKey).iterator();
            }
        }

        return eventBean;
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
        return keyIterator.hasNext();

    }

    public final void remove()
    {
        throw new UnsupportedOperationException();
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.ext;

import com.espertech.esper.client.EventBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;

/**
 * Iterator for use by {@link com.espertech.esper.view.ext.TimeOrderView}.
 */
public final class TimeOrderViewIterator implements Iterator<EventBean>
{
    private final SortedMap<Long, ArrayList<EventBean>> window;

    private final Iterator<Long> keyIterator;
    private Iterator<EventBean> currentListIterator;

    /**
     * Ctor.
     * @param window - sorted map with events
     */
    public TimeOrderViewIterator(SortedMap<Long, ArrayList<EventBean>> window)
    {
        this.window = window;
        keyIterator = window.keySet().iterator();
        if (keyIterator.hasNext())
        {
            Long key = keyIterator.next();

            ArrayList<EventBean> list = window.get(key);
            while((list.isEmpty()) && (keyIterator.hasNext()))
            {
                key = keyIterator.next();
                list = window.get(key);
            }
            currentListIterator = list.iterator();
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
                Long key = keyIterator.next();

                ArrayList<EventBean> list = window.get(key);
                while((list.isEmpty()) && (keyIterator.hasNext()))
                {
                    key = keyIterator.next();
                    list = window.get(key);
                }
                currentListIterator = list.iterator();
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

        if (!keyIterator.hasNext())
        {
            return false;
        }

        return true;
    }

    public final void remove()
    {
        throw new UnsupportedOperationException();
    }
}

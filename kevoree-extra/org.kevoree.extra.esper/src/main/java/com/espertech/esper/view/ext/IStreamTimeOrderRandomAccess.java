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
import com.espertech.esper.view.window.RandomAccessByIndex;
import com.espertech.esper.view.window.RandomAccessByIndexObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Provides random access into a time-ordered-window's data.
 */
public class IStreamTimeOrderRandomAccess implements RandomAccessByIndex
{
    private final RandomAccessByIndexObserver updateObserver;

    private TreeMap<Long, ArrayList<EventBean>> sortedEvents;
    private int currentSize;

    private Iterator<ArrayList<EventBean>> iterator;
    private ArrayList<EventBean> cache;
    private int cacheFilledTo;

    /**
     * Ctor.
     * @param updateObserver for indicating updates to
     */
    public IStreamTimeOrderRandomAccess(RandomAccessByIndexObserver updateObserver)
    {
        this.updateObserver = updateObserver;
    }

    /**
     * Refreshes the random access data with the updated information.
     * @param sortedEvents is the sorted window contents
     * @param currentSize is the current size of the window
     */
    public void refresh(TreeMap<Long, ArrayList<EventBean>> sortedEvents, int currentSize)
    {
        updateObserver.updated(this);
        this.sortedEvents = sortedEvents;
        this.currentSize = currentSize;

        this.iterator = null;
        this.cacheFilledTo = 0;
        if (cache == null)
        {
            cache = new ArrayList<EventBean>();
        }
    }

    public EventBean getNewData(int index)
    {
        if (iterator == null)
        {
            iterator = sortedEvents.values().iterator();
        }

        // if asking for more then the sorted window currently holds, return no data
        if (index >= currentSize)
        {
            return null;
        }

        // If we have it in cache, serve from cache
        if (index < cacheFilledTo)
        {
            return cache.get(index);
        }

        // Load more into cache
        while(true)
        {
            if (cacheFilledTo == currentSize)
            {
                break;
            }
            if (!iterator.hasNext())
            {
                break;
            }
            ArrayList<EventBean> events = iterator.next();
            for (EventBean event : events)
            {
                cache.add(cacheFilledTo, event);
                cacheFilledTo++;
            }

            if (cacheFilledTo > index)
            {
                break;
            }
        }

        // If we have it in cache, serve from cache
        if (index <= cacheFilledTo)
        {
            return cache.get(index);
        }

        return null;
    }

    public EventBean getOldData(int index)
    {
        return null;
    }

    public EventBean getNewDataTail(int index)
    {
        initCache();

        if ((index < cacheFilledTo) && (index >= 0))
        {
            return cache.get(cacheFilledTo - index - 1);
        }

        return null;
    }

    public Iterator<EventBean> getWindowIterator()
    {
        initCache();
        return cache.iterator();
    }

    public int getWindowCount()
    {
        return currentSize;
    }

    private void initCache() {

        // Load more into cache
        while(true)
        {
            if (cacheFilledTo == currentSize)
            {
                break;
            }
            if (!iterator.hasNext())
            {
                break;
            }
            ArrayList<EventBean> events = iterator.next();
            for (EventBean event : events)
            {
                cache.add(cacheFilledTo, event);
                cacheFilledTo++;
            }
        }
    }
}

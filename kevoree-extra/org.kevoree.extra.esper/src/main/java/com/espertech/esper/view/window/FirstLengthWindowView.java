/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.DataWindowView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A length-first view takes the first N arriving events. Further arriving insert stream events are disregarded until
 * events are deleted.
 * <p>
 * Remove stream events delete from the data window.
 */
public final class FirstLengthWindowView extends ViewSupport implements DataWindowView, CloneableView
{
    private final FirstLengthWindowViewFactory lengthFirstFactory;
    private final int size;
    private LinkedHashSet<EventBean> indexedEvents;

    /**
     * Ctor.
     * @param size the first N events to consider
     * @param lengthFirstWindowViewFactory for copying this view in a group-by
     */
    public FirstLengthWindowView(FirstLengthWindowViewFactory lengthFirstWindowViewFactory, int size)
    {
        if (size < 1)
        {
            throw new IllegalArgumentException("Illegal argument for size of length window");
        }

        this.lengthFirstFactory = lengthFirstWindowViewFactory;
        this.size = size;
        indexedEvents = new LinkedHashSet<EventBean>();
    }

    public View cloneView(StatementContext statementContext)
    {
        return lengthFirstFactory.makeView(statementContext);
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return indexedEvents.isEmpty();
    }

    /**
     * Returns the size of the length window.
     * @return size of length window
     */
    public final int getSize()
    {
        return size;
    }

    public final EventType getEventType()
    {
        // The event type is the parent view's event type
        return parent.getEventType();
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        OneEventCollection newDataToPost = null;
        OneEventCollection oldDataToPost = null;

        // add data points to the window as long as its not full, ignoring later events
        if (newData != null)
        {
            for (EventBean aNewData : newData)
            {
                if (indexedEvents.size() < size)
                {
                    if (newDataToPost == null)
                    {
                        newDataToPost = new OneEventCollection();
                    }
                    newDataToPost.add(aNewData);
                    indexedEvents.add(aNewData);
                }
            }
        }

        if (oldData != null)
        {
            for (EventBean anOldData : oldData)
            {
                boolean removed = indexedEvents.remove(anOldData);
                if (removed)
                {
                    if (oldDataToPost == null)
                    {
                        oldDataToPost = new OneEventCollection();
                    }
                    oldDataToPost.add(anOldData);
                }
            }
        }

        // If there are child views, call update method
        if ((this.hasViews()) && ((newDataToPost != null) || (oldDataToPost != null)))
        {
            updateChildren((newDataToPost != null) ? newDataToPost.toArray() : null,
                           (oldDataToPost != null) ? oldDataToPost.toArray() : null);
        }
    }

    public final Iterator<EventBean> iterator()
    {
        return indexedEvents.iterator();
    }

    public final String toString()
    {
        return this.getClass().getName() + " size=" + size;
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Event stream implementation that does not keep any window by itself of the events coming into the stream.
 */
public final class ZeroDepthStream implements EventStream
{
    private final LinkedList<View> children = new LinkedList<View>();
    private final EventType eventType;
    private EventBean lastInsertedEvent;
    private EventBean[] lastInsertedEvents;

    /**
     * Ctor.
     * @param eventType - type of event
     */
    public ZeroDepthStream(EventType eventType)
    {
        this.eventType = eventType;
    }

    public void insert(EventBean[] events)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".insert Received event, updating child views, events=" + Arrays.toString(events));
        }

        for (View childView : children)
        {
            childView.update(events, null);
        }

        lastInsertedEvents = events;
    }

    public final void insert(EventBean event)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".insert Received event, updating child views, event=" + event);
        }

        // Get a new array created rather then re-use the old one since some client listeners
        // to this view may keep reference to the new data
        EventBean[] row = new EventBean[]{event};
        for (View childView : children)
        {
            childView.update(row, null);
        }

        lastInsertedEvent = event;
    }

    public final EventType getEventType()
    {
        return eventType;
    }

    public final Iterator<EventBean> iterator()
    {
        if (lastInsertedEvents != null)
        {
            return new ArrayEventIterator(lastInsertedEvents);
        }
        return new SingleEventIterator(lastInsertedEvent);
    }

    public final View addView(View view)
    {
        children.add(view);
        view.setParent(this);
        return view;
    }

    public final List<View> getViews()
    {
        return children;
    }

    public final boolean removeView(View view)
    {
        boolean isRemoved = children.remove(view);
        view.setParent(null);
        return isRemoved;
    }

    public final boolean hasViews()
    {
        return (!children.isEmpty());
    }

    public void removeAllViews()
    {
        children.clear();
    }

    private static final Log log = LogFactory.getLog(ZeroDepthStream.class);
}



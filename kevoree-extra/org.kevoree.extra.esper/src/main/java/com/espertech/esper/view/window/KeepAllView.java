/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.core.StatementContext;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;
import com.espertech.esper.view.DataWindowView;
import com.espertech.esper.collection.ViewUpdatedCollection;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Arrays;

/**
 * This view is a keep-all data window that simply keeps all events added.
 * It in addition allows to remove events efficiently for the remove-stream events received by the view.
 */
public final class KeepAllView extends ViewSupport implements DataWindowView, CloneableView
{
    private final KeepAllViewFactory keepAllViewFactory;
    private LinkedHashSet<EventBean> indexedEvents;
    private final ViewUpdatedCollection viewUpdatedCollection;

    /**
     * Ctor.
     * @param keepAllViewFactory for copying this view in a group-by
     * @param viewUpdatedCollection for satisfying queries that select previous events in window order
     */
    public KeepAllView(KeepAllViewFactory keepAllViewFactory, ViewUpdatedCollection viewUpdatedCollection)
    {
        this.keepAllViewFactory = keepAllViewFactory;
        indexedEvents = new LinkedHashSet<EventBean>();
        this.viewUpdatedCollection = viewUpdatedCollection;
    }

    public View cloneView(StatementContext statementContext)
    {
        return keepAllViewFactory.makeView(statementContext);
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
     * Returns the (optional) collection handling random access to window contents for prior or previous events.
     * @return buffer for events
     */
    public ViewUpdatedCollection getViewUpdatedCollection()
    {
        return viewUpdatedCollection;
    }

    public final EventType getEventType()
    {
        // The event type is the parent view's event type
        return parent.getEventType();
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        if (newData != null)
        {
            indexedEvents.addAll(Arrays.asList(newData));
        }

        if (oldData != null)
        {
            for (EventBean anOldData : oldData)
            {
                indexedEvents.remove(anOldData);
            }
        }

        // update event buffer for access by expressions, if any
        if (viewUpdatedCollection != null)
        {
            viewUpdatedCollection.update(newData, oldData);
        }

        updateChildren(newData, oldData);
    }

    public final Iterator<EventBean> iterator()
    {
        return indexedEvents.iterator();
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.std;

import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;

import java.util.Iterator;

/**
 * This view is a very simple view presenting the last event posted by the parent view to any subviews.
 * Only the very last event object is kept by this view. The update method invoked by the parent view supplies
 * new data in an object array, of which the view keeps the very last instance as the 'last' or newest event.
 * The view always has the same schema as the parent view and attaches to anything, and accepts no parameters.
 *
 * Thus if 5 pieces of new data arrive, the child view receives 5 elements of new data
 * and also 4 pieces of old data which is the first 4 elements of new data.
 * I.e. New data elements immediatly gets to be old data elements.
 *
 *  Old data received from parent is not handled, it is ignored.
  * We thus post old data as follows:
 *      last event is not null +
 *      new data from index zero to N-1, where N is the index of the last element in new data
 */
public class LastElementView extends ViewSupport implements CloneableView
{
    /**
     * The last new element posted from a parent view.
     */
    protected EventBean lastEvent;

    public View cloneView(StatementContext context)
    {
        return new LastElementView();
    }

    public final EventType getEventType()
    {
        // The schema is the parent view's schema
        return parent.getEventType();
    }

    public void update(EventBean[] newData, EventBean[] oldData)
    {
        OneEventCollection oldDataToPost = null;

        if ((newData != null) && (newData.length != 0))
        {
            if (lastEvent != null)
            {
                oldDataToPost = new OneEventCollection();
                oldDataToPost.add(lastEvent);
            }
            if (newData.length > 1)
            {
                for (int i = 0; i < newData.length - 1; i++)
                {
                    if (oldDataToPost == null)
                    {
                        oldDataToPost = new OneEventCollection();
                    }
                    oldDataToPost.add(newData[i]);
                }
            }
            lastEvent = newData[newData.length - 1];
        }

        if (oldData != null)
        {
            for (int i = 0; i < oldData.length; i++)
            {
                if (oldData[i] == lastEvent)
                {
                    if (oldDataToPost == null)
                    {
                        oldDataToPost = new OneEventCollection();
                    }
                    oldDataToPost.add(oldData[i]);
                    lastEvent = null;
                }
            }
        }

        // If there are child views, fireStatementStopped update method
        if (this.hasViews())
        {
            if ((oldDataToPost != null) && (!oldDataToPost.isEmpty()))
            {
                updateChildren(newData, oldDataToPost.toArray());
            }
            else
            {
                updateChildren(newData, null);
            }
        }
    }

    public final Iterator<EventBean> iterator()
    {
        return new SingleEventIterator(lastEvent);
    }

    public final String toString()
    {
        return this.getClass().getName();
    }
}

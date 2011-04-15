/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.view.*;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.collection.ViewUpdatedCollection;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.client.EPException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * A data window view that holds events in a stream and only removes events from a stream (rstream) if
 * no more events arrive for a given time interval.
 * <p>
 * No batch version of the view exists as the batch version is simply the remove stream of this view, which removes
 * in batches.
 * <p>
 * The view is continuous, the insert stream consists of arriving events. The remove stream
 * only posts current window contents when no more events arrive for a given timer interval.
 */
public final class TimeAccumView extends ViewSupport implements CloneableView, DataWindowView, StoppableView
{
    // View parameters
    private final TimeAccumViewFactory factory;
    private final StatementContext statementContext;
    private final long msecIntervalSize;
    private final ViewUpdatedCollection viewUpdatedCollection;
    private final ScheduleSlot scheduleSlot;

    // Current running parameters
    private ArrayList<EventBean> currentBatch = new ArrayList<EventBean>();
    private long callbackScheduledTime;
    private EPStatementHandleCallback handle;

    /**
     * Constructor.
     * @param msecIntervalSize is the number of milliseconds to batch events for
     * @param viewUpdatedCollection is a collection that the view must update when receiving events
     * @param timeBatchViewFactory fr copying this view in a group-by
     * @param statementContext is required view services
     */
    public TimeAccumView(TimeAccumViewFactory timeBatchViewFactory,
                         StatementContext statementContext,
                         long msecIntervalSize,
                         ViewUpdatedCollection viewUpdatedCollection)
    {
        this.statementContext = statementContext;
        this.factory = timeBatchViewFactory;
        this.msecIntervalSize = msecIntervalSize;
        this.viewUpdatedCollection = viewUpdatedCollection;

        this.scheduleSlot = statementContext.getScheduleBucket().allocateSlot();

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                TimeAccumView.this.sendRemoveStream();
            }
        };
        handle = new EPStatementHandleCallback(statementContext.getEpStatementHandle(), callback);
    }

    public View cloneView(StatementContext statementContext)
    {
        return factory.makeView(statementContext);
    }

    /**
     * Returns the interval size in milliseconds.
     * @return batch size
     */
    public final long getMsecIntervalSize()
    {
        return msecIntervalSize;
    }

    public final EventType getEventType()
    {
        return parent.getEventType();
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".update Received update, " +
                    "  newData.length==" + ((newData == null) ? 0 : newData.length) +
                    "  oldData.length==" + ((oldData == null) ? 0 : oldData.length));
        }

        if (statementContext == null)
        {
            String message = "View context has not been supplied, cannot addSchedule callback";
            log.fatal(".update " + message);
            throw new EPException(message);
        }

        // we don't care about removed data from a prior view
        if ((newData == null) || (newData.length == 0))
        {
            return;
        }

        // If we have an empty window about to be filled for the first time, addSchedule a callback
        boolean removeSchedule = false;
        boolean addSchedule = false;
        long timestamp = statementContext.getSchedulingService().getTime();

        if (!currentBatch.isEmpty())
        {
            // check if we need to reschedule
            long callbackTime = timestamp + msecIntervalSize;
            if (callbackTime != callbackScheduledTime)
            {
                removeSchedule = true;
                addSchedule = true;
            }
        }
        else
        {
            addSchedule = true;
        }

        if (removeSchedule)
        {
            statementContext.getSchedulingService().remove(handle, scheduleSlot);
        }
        if (addSchedule)
        {
            statementContext.getSchedulingService().add(msecIntervalSize, handle, scheduleSlot);
            callbackScheduledTime = msecIntervalSize + timestamp;
        }

        // add data points to the window
        currentBatch.addAll(Arrays.asList(newData));

        // forward insert stream to child views
        if (viewUpdatedCollection != null)
        {
            viewUpdatedCollection.update(newData, null);
        }

        // update child views
        if (this.hasViews())
        {
            updateChildren(newData, null);
        }
    }

    /**
     * This method sends the remove stream for all accumulated events.
     */
    protected final void sendRemoveStream()
    {
        callbackScheduledTime = -1;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".sendRemoveStream Update child views, " +
                    "  time=" + statementContext.getSchedulingService().getTime());
        }

        // If there are child views and the batch was filled, fireStatementStopped update method
        if (this.hasViews())
        {
            // Convert to object arrays
            EventBean[] oldData = null;
            if (!currentBatch.isEmpty())
            {
                oldData = currentBatch.toArray(new EventBean[currentBatch.size()]);
            }

            // Post old data
            if (viewUpdatedCollection != null)
            {
                viewUpdatedCollection.update(null, oldData);
            }

            if (oldData != null)
            {
                updateChildren(null, oldData);
            }
        }

        currentBatch.clear();
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return currentBatch.isEmpty();
    }

    public final Iterator<EventBean> iterator()
    {
        return currentBatch.iterator();
    }

    public final String toString()
    {
        return this.getClass().getName() + " msecIntervalSize=" + msecIntervalSize;
    }

    public void stop() {
    	if (handle != null) {
        	statementContext.getSchedulingService().remove(handle, scheduleSlot);
        }
    }

    private static final Log log = LogFactory.getLog(TimeAccumView.class);
}

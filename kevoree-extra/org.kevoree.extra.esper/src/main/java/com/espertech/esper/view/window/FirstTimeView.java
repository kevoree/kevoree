/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.*;
import com.espertech.esper.collection.OneEventCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 *
 */
public final class FirstTimeView extends ViewSupport implements CloneableView, BatchingDataWindowView, StoppableView
{
    private final FirstTimeViewFactory timeFirstViewFactory;
    private final StatementContext statementContext;
    private final long msecIntervalSize;
    private final ScheduleSlot scheduleSlot;
    private EPStatementHandleCallback handle;

    // Current running parameters
    private LinkedHashSet<EventBean> events = new LinkedHashSet<EventBean>();
    private boolean isClosed;

    /**
     * Constructor.
     * @param msecIntervalSize is the number of milliseconds to batch events for
     * @param timeFirstViewFactory fr copying this view in a group-by
     * @param statementContext is required view services
     */
    public FirstTimeView(FirstTimeViewFactory timeFirstViewFactory,
                         StatementContext statementContext,
                         long msecIntervalSize)
    {
        this.statementContext = statementContext;
        this.timeFirstViewFactory = timeFirstViewFactory;
        this.msecIntervalSize = msecIntervalSize;

        this.scheduleSlot = statementContext.getScheduleBucket().allocateSlot();

        scheduleCallback();
    }

    public View cloneView(StatementContext statementContext)
    {
        return timeFirstViewFactory.makeView(statementContext);
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
            String message = "View context has not been supplied, cannot schedule callback";
            log.fatal(".update " + message);
            throw new EPException(message);
        }

        OneEventCollection oldDataToPost = null;
        if (oldData != null)
        {
            for (EventBean anOldData : oldData)
            {
                boolean removed = events.remove(anOldData);
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

        // add data points to the timeWindow
        OneEventCollection newDataToPost = null;
        if ((!isClosed) && (newData != null))
        {
            for (EventBean aNewData : newData)
            {
                events.add(aNewData);
                if (newDataToPost == null)
                {
                    newDataToPost = new OneEventCollection();
                }
                newDataToPost.add(aNewData);
            }
        }

        // If there are child views, call update method
        if ((this.hasViews()) && ((newDataToPost != null) || (oldDataToPost != null)))
        {
            updateChildren((newDataToPost != null) ? newDataToPost.toArray() : null,
                           (oldDataToPost != null) ? oldDataToPost.toArray() : null);
        }
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return events.isEmpty();
    }

    public final Iterator<EventBean> iterator()
    {
        return events.iterator();
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " msecIntervalSize=" + msecIntervalSize;
    }

    private void scheduleCallback()
    {
        long afterMSec = this.msecIntervalSize;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".scheduleCallback Scheduled new callback for " +
                    " afterMsec=" + afterMSec +
                    " msecIntervalSize=" + msecIntervalSize);
        }

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                FirstTimeView.this.isClosed = true;
            }
        };
        handle = new EPStatementHandleCallback(statementContext.getEpStatementHandle(), callback);
        statementContext.getSchedulingService().add(afterMSec, handle, scheduleSlot);
    }

    public void stop() {
    	if (handle != null) {
        	statementContext.getSchedulingService().remove(handle, scheduleSlot);
        }
    }

    private static final Log log = LogFactory.getLog(TimeBatchViewRStream.class);
}

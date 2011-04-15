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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Arrays;

/**
 * Same as the {@link TimeBatchView}, this view also supports fast-remove from the batch for remove stream events.
 */
public final class TimeBatchViewRStream extends ViewSupport implements CloneableView, BatchingDataWindowView, StoppableView
{
    // View parameters
    private final TimeBatchViewFactory timeBatchViewFactory;
    private final StatementContext statementContext;
    private final long msecIntervalSize;
    private final Long initialReferencePoint;
    private final ScheduleSlot scheduleSlot;
    private final boolean isForceOutput;
    private final boolean isStartEager;
    private EPStatementHandleCallback handle;

    // Current running parameters
    private Long currentReferencePoint;
    private LinkedHashSet<EventBean> lastBatch = null;
    private LinkedHashSet<EventBean> currentBatch = new LinkedHashSet<EventBean>();
    private boolean isCallbackScheduled;

    /**
     * Constructor.
     * @param msecIntervalSize is the number of milliseconds to batch events for
     * @param referencePoint is the reference point onto which to base intervals, or null if
     * there is no such reference point supplied
     * @param timeBatchViewFactory fr copying this view in a group-by
     * @param statementContext is required view services
     * @param forceOutput is true if the batch should produce empty output if there is no value to output following time intervals
     * @param isStartEager is true for start-eager
     */
    public TimeBatchViewRStream(TimeBatchViewFactory timeBatchViewFactory,
                         StatementContext statementContext,
                         long msecIntervalSize,
                         Long referencePoint,
                         boolean forceOutput,
                         boolean isStartEager)
    {
        this.statementContext = statementContext;
        this.timeBatchViewFactory = timeBatchViewFactory;
        this.msecIntervalSize = msecIntervalSize;
        this.initialReferencePoint = referencePoint;
        this.isStartEager = isStartEager;
        this.isForceOutput = forceOutput;

        this.scheduleSlot = statementContext.getScheduleBucket().allocateSlot();

        // schedule the first callback
        if (this.isStartEager)
        {
            currentReferencePoint = statementContext.getSchedulingService().getTime();
            scheduleCallback();
            isCallbackScheduled = true;
        }
    }

    public View cloneView(StatementContext statementContext)
    {
        return timeBatchViewFactory.makeView(statementContext);
    }

    /**
     * Returns the interval size in milliseconds.
     * @return batch size
     */
    public final long getMsecIntervalSize()
    {
        return msecIntervalSize;
    }

    /**
     * Gets the reference point to use to anchor interval start and end dates to.
     * @return is the millisecond reference point.
     */
    public final Long getInitialReferencePoint()
    {
        return initialReferencePoint;
    }

    /**
     * True for force-output.
     * @return indicates force-output
     */
    public boolean isForceOutput()
    {
        return isForceOutput;
    }

    /**
     * True for start-eager.
     * @return indicates start-eager
     */
    public boolean isStartEager()
    {
        return isStartEager;
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

        if (oldData != null)
        {
            for (int i = 0; i < oldData.length; i++)
            {
                currentBatch.remove(oldData[i]);
            }
        }

        // we don't care about removed data from a prior view
        if ((newData == null) || (newData.length == 0))
        {
            return;
        }

        // If we have an empty window about to be filled for the first time, schedule a callback
        if (currentBatch.isEmpty())
        {
            if (currentReferencePoint == null)
            {
                currentReferencePoint = initialReferencePoint;
                if (currentReferencePoint == null)
                {
                    currentReferencePoint = statementContext.getSchedulingService().getTime();
                }
            }

            // Schedule the next callback if there is none currently scheduled
            if (!isCallbackScheduled)
            {
                scheduleCallback();
                isCallbackScheduled = true;
            }
        }

        // add data points to the timeWindow
        currentBatch.addAll(Arrays.asList(newData));

        // We do not update child views, since we batch the events.
    }

    /**
     * This method updates child views and clears the batch of events.
     * We schedule a new callback at this time if there were events in the batch.
     */
    protected final void sendBatch()
    {
        isCallbackScheduled = false;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".sendBatch Update child views, " +
                    "  time=" + statementContext.getSchedulingService().getTime());
        }

        // If there are child views and the batch was filled, fireStatementStopped update method
        if (this.hasViews())
        {
            // Convert to object arrays
            EventBean[] newData = null;
            EventBean[] oldData = null;
            if (!currentBatch.isEmpty())
            {
                newData = currentBatch.toArray(new EventBean[currentBatch.size()]);
            }
            if ((lastBatch != null) && (!lastBatch.isEmpty()))
            {
                oldData = lastBatch.toArray(new EventBean[lastBatch.size()]);
            }

            if ((newData != null) || (oldData != null) || (isForceOutput))
            {
                updateChildren(newData, oldData);
            }
        }

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".sendBatch Published updated data, ....newData size=" + currentBatch.size());
            for (Object object : currentBatch)
            {
                log.debug(".sendBatch object=" + object);
            }
        }

        // Only if forceOutput is enabled or
        // there have been any events in this or the last interval do we schedule a callback,
        // such as to not waste resources when no events arrive.
        if ((!currentBatch.isEmpty()) || ((lastBatch != null) && (!lastBatch.isEmpty()))
            ||
            (isForceOutput))
        {
            scheduleCallback();
            isCallbackScheduled = true;
        }

        lastBatch = currentBatch;
        currentBatch = new LinkedHashSet<EventBean>();
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        if (lastBatch != null)
        {
            if (!lastBatch.isEmpty())
            {
                return false;
            }
        }
        return currentBatch.isEmpty();
    }

    public final Iterator<EventBean> iterator()
    {
        return currentBatch.iterator();
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " msecIntervalSize=" + msecIntervalSize +
                " initialReferencePoint=" + initialReferencePoint;
    }

    private void scheduleCallback()
    {
        long current = statementContext.getSchedulingService().getTime();
        long afterMSec = TimeBatchView.computeWaitMSec(current, this.currentReferencePoint, this.msecIntervalSize);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".scheduleCallback Scheduled new callback for " +
                    " afterMsec=" + afterMSec +
                    " now=" + current +
                    " currentReferencePoint=" + currentReferencePoint +
                    " initialReferencePoint=" + initialReferencePoint +
                    " msecIntervalSize=" + msecIntervalSize);
        }

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                TimeBatchViewRStream.this.sendBatch();
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

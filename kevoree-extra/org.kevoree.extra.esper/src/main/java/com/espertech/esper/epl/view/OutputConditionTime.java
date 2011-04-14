/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprTimePeriod;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Output condition that is satisfied at the end
 * of every time interval of a given length.
 */
public final class OutputConditionTime implements OutputCondition
{
    private static final boolean DO_OUTPUT = true;
	private static final boolean FORCE_UPDATE = true;

    private ExprTimePeriod timePeriod;
    private long msecIntervalSize;
    private final OutputCallback outputCallback;
    private final ScheduleSlot scheduleSlot;

    private Long currentReferencePoint;
    private StatementContext context;
    private boolean isCallbackScheduled;
    private EPStatementHandleCallback handle;

    /**
     * Constructor.
     * @param timePeriod is the number of minutes or seconds to batch events for, may include variables
     * @param context is the view context for time scheduling
     * @param outputCallback is the callback to make once the condition is satisfied
     */
    public OutputConditionTime(ExprTimePeriod timePeriod,
                               StatementContext context,
    						   OutputCallback outputCallback)
    {
		if(outputCallback ==  null)
		{
			throw new NullPointerException("Output condition by count requires a non-null callback");
		}
        if (context == null)
        {
            String message = "OutputConditionTime requires a non-null view context";
            throw new NullPointerException(message);
        }

        this.context = context;
        this.outputCallback = outputCallback;
        this.scheduleSlot = context.getScheduleBucket().allocateSlot();
        this.timePeriod = timePeriod;

        Double numSeconds = (Double) timePeriod.evaluate(null, true, context);
        if (numSeconds == null)
        {
            throw new IllegalArgumentException("Output condition by time returned a null value for the interval size");
        }
        if ((numSeconds < 0.001) && (!timePeriod.hasVariable()))
        {
            throw new IllegalArgumentException("Output condition by time requires a interval size of at least 1 msec or a variable");
        }
        this.msecIntervalSize = Math.round(1000 * numSeconds);
    }

    /**
     * Returns the interval size in milliseconds.
     * @return batch size
     */
    public final long getMsecIntervalSize()
    {
        return msecIntervalSize;
    }

    public final void updateOutputCondition(int newEventsCount, int oldEventsCount)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
        	log.debug(".updateOutputCondition, " +
        			"  newEventsCount==" + newEventsCount +
        			"  oldEventsCount==" + oldEventsCount);
        }

        if (currentReferencePoint == null)
        {
        	currentReferencePoint = context.getSchedulingService().getTime();
        }

        // If we pull the interval from a variable, then we may need to reschedule
        if (timePeriod.hasVariable())
        {
            Double numSeconds = (Double) timePeriod.evaluate(null, true, context);
            if (numSeconds != null)
            {
                long newMsecIntervalSize = Math.round(1000 * numSeconds);

                // reschedule if the interval changed
                if (newMsecIntervalSize != msecIntervalSize)
                {
                    if (isCallbackScheduled)
                    {
                        // reschedule
                        context.getSchedulingService().remove(handle, scheduleSlot);
                        scheduleCallback();
                    }
                }
            }
        }

        // Schedule the next callback if there is none currently scheduled
        if (!isCallbackScheduled)
        {
        	scheduleCallback();
        }
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " msecIntervalSize=" + msecIntervalSize;
    }

    private void scheduleCallback()
    {
        // If we pull the interval from a variable, get the current interval length
        if (timePeriod.hasVariable())
        {
            Double param = (Double) timePeriod.evaluate(null, true, context);
            if (param != null)
            {
                msecIntervalSize = Math.round(1000 * param);
            }
        }

    	isCallbackScheduled = true;
        long current = context.getSchedulingService().getTime();
        long afterMSec = computeWaitMSec(current, this.currentReferencePoint, this.msecIntervalSize);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".scheduleCallback Scheduled new callback for " +
                    " afterMsec=" + afterMSec +
                    " now=" + current +
                    " currentReferencePoint=" + currentReferencePoint +
                    " msecIntervalSize=" + msecIntervalSize);
        }

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                OutputConditionTime.this.isCallbackScheduled = false;
                OutputConditionTime.this.outputCallback.continueOutputProcessing(DO_OUTPUT, FORCE_UPDATE);
                scheduleCallback();
            }
        };
        handle = new EPStatementHandleCallback(context.getEpStatementHandle(), callback);
        context.getSchedulingService().add(afterMSec, handle, scheduleSlot);
    }

    /**
     * Given a current time and a reference time and an interval size, compute the amount of
     * milliseconds till the next interval.
     * @param current is the current time
     * @param reference is the reference point
     * @param interval is the interval size
     * @return milliseconds after current time that marks the end of the current interval
     */
    protected static long computeWaitMSec(long current, long reference, long interval)
    {
        // Example:  current c=2300, reference r=1000, interval i=500, solution s=200
        //
        // int n = ((2300 - 1000) / 500) = 2
        // r + (n + 1) * i - c = 200
        //
        // Negative example:  current c=2300, reference r=4200, interval i=500, solution s=400
        // int n = ((2300 - 4200) / 500) = -3
        // r + (n + 1) * i - c = 4200 - 3*500 - 2300 = 400
        //
        long n = (long) ( (current - reference) / (interval * 1f));
        if (reference > current)        // References in the future need to deduct one window
        {
            n--;
        }
        long solution = reference + (n + 1) * interval - current;

        if (solution == 0)
        {
            return interval;
        }
        return solution;
    }

    private static final Log log = LogFactory.getLog(OutputConditionTime.class);
}

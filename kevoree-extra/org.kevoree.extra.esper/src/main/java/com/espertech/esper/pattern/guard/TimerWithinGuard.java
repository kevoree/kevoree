/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern.guard;

import com.espertech.esper.pattern.PatternContext;
import com.espertech.esper.pattern.MatchedEventMap;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;

/**
 * Guard implementation that keeps a timer instance and quits when the timer expired,
 * letting all {@link MatchedEventMap} instances pass until then.
 */
public class TimerWithinGuard implements Guard, ScheduleHandleCallback
{
    private final long msec;
    private final Quitable quitable;
    private final ScheduleSlot scheduleSlot;

    private boolean isTimerActive;
    private EPStatementHandleCallback scheduleHandle;

    /**
     * Ctor.
     * @param msec - number of millisecond to guard expiration
     * @param quitable - to use to indicate that the gaurd quitted
     */
    public TimerWithinGuard(long msec, Quitable quitable)
    {
        this.msec = msec;
        this.quitable = quitable;
        this.scheduleSlot = quitable.getContext().getScheduleBucket().allocateSlot();
    }

    public void startGuard()
    {
        if (isTimerActive)
        {
            throw new IllegalStateException("Timer already active");
        }

        // Start the stopwatch timer
        scheduleHandle = new EPStatementHandleCallback(quitable.getContext().getEpStatementHandle(), this);
        quitable.getContext().getSchedulingService().add(msec, scheduleHandle, scheduleSlot);
        isTimerActive = true;
    }

    public void stopGuard()
    {
        if (isTimerActive)
        {
            quitable.getContext().getSchedulingService().remove(scheduleHandle, scheduleSlot);
            scheduleHandle = null;
            isTimerActive = false;
        }
    }

    public boolean inspect(MatchedEventMap matchEvent)
    {
        // no need to test: for timing only, if the timer expired the guardQuit stops any events from coming here
        return true;
    }

    public final void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
    {
        // Timer callback is automatically removed when triggering
        isTimerActive = false;
        quitable.guardQuit();
    }
}

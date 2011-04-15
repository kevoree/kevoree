/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern.observer;

import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.pattern.MatchedEventMap;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;

/**
 * Observer that will wait a certain interval before indicating true (raising an event).
 */
public class TimerIntervalObserver implements EventObserver, ScheduleHandleCallback
{
    private final long msec;
    private final MatchedEventMap beginState;
    private final ObserverEventEvaluator observerEventEvaluator;
    private final ScheduleSlot scheduleSlot;

    private boolean isTimerActive = false;
    private EPStatementHandleCallback scheduleHandle;

    /**
     * Ctor.
     * @param msec - number of milliseconds
     * @param beginState - start state
     * @param observerEventEvaluator - receiver for events
     */
    public TimerIntervalObserver(long msec, MatchedEventMap beginState, ObserverEventEvaluator observerEventEvaluator)
    {
        this.msec = msec;
        this.beginState = beginState;
        this.observerEventEvaluator = observerEventEvaluator;
        this.scheduleSlot = observerEventEvaluator.getContext().getScheduleBucket().allocateSlot();
    }

    public final void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
    {
        observerEventEvaluator.observerEvaluateTrue(beginState);
        isTimerActive = false;
    }

    public void startObserve()
    {
        if (isTimerActive)
        {
            throw new IllegalStateException("Timer already active");
        }

        if (msec <= 0)
        {
            observerEventEvaluator.observerEvaluateTrue(beginState);
        }
        else
        {
            scheduleHandle = new EPStatementHandleCallback(observerEventEvaluator.getContext().getEpStatementHandle(), this);
            observerEventEvaluator.getContext().getSchedulingService().add(msec, scheduleHandle, scheduleSlot);
            isTimerActive = true;
        }
    }

    public void stopObserve()
    {
        if (isTimerActive)
        {
            observerEventEvaluator.getContext().getSchedulingService().remove(scheduleHandle, scheduleSlot);
            isTimerActive = false;
            scheduleHandle = null;
        }
    }
}

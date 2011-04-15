package com.espertech.esper.schedule;

import com.espertech.esper.view.window.TimeWindowView;

import java.util.Set;
import java.util.HashSet;

/**
 * Service for holding expiration dates to adjust.
 */
public class ScheduleAdjustmentService
{
    private Set<ScheduleAdjustmentCallback> callbacks = new HashSet<ScheduleAdjustmentCallback>();

    /**
     * Add a callback
     * @param callback to add
     */
    public void addCallback(ScheduleAdjustmentCallback callback)
    {
        callbacks.add(callback);
    }

    /**
     * Make callbacks to adjust expiration dates.
     * @param delta to adjust for
     */
    public void adjust(long delta)
    {
        for (ScheduleAdjustmentCallback callback : callbacks)
        {
            callback.adjust(delta);
        }
    }

    public void removeCallback(TimeWindowView callback) {
        callbacks.remove(callback);
    }
}

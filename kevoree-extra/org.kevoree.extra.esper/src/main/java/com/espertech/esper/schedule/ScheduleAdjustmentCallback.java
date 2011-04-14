package com.espertech.esper.schedule;

/**
 * Callback for views that adjust an expiration date on event objects.
 */
public interface ScheduleAdjustmentCallback
{
    /**
     * Adjust expiration date.
     * @param delta to adjust
     */
    public void adjust(long delta);
}
package com.espertech.esper.schedule;

import java.util.List;

/**
 * Set of schedules.
 */
public class ScheduleSet
{
    private List<ScheduleSetEntry> list;

    /**
     * Ctor.
     * @param list schedules
     */
    public ScheduleSet(List<ScheduleSetEntry> list)
    {
        this.list = list;
    }

    /**
     * Return schedules.
     * @return schedules
     */
    public List<ScheduleSetEntry> getList()
    {
        return list;
    }
}

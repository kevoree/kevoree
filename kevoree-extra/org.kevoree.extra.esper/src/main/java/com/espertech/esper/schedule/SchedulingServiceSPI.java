package com.espertech.esper.schedule;

import java.util.Map;
import java.util.Set;

/**
 * Service provider interface for scheduling service.
 */
public interface SchedulingServiceSPI extends SchedulingService
{
    /**
     * Take a statement's schedules out of the currently active set of schedules.
     * @param statementId statements to take out
     * @return schedules
     */
    public ScheduleSet take(Set<String> statementId);

    /**
     * Apply the set of schedules.
     * @param scheduleSet to apply
     */
    public void apply(ScheduleSet scheduleSet);

    public Long getNearestTimeHandle();

    public Map<String, Long> getStatementSchedules();    
}

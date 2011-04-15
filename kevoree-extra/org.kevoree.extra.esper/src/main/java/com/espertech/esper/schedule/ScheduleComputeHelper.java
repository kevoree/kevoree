/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.schedule;

import java.util.*;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.type.ScheduleUnit;

/**
 * For a crontab-like schedule, this class computes the next occurance given a start time and a specification of
 * what the schedule looks like.
 * The resolution at which this works is at the second level. The next occurance
 * is always at least 1 second ahead.
 * The class implements an algorithm that starts at the highest precision (seconds) and
 * continues to the lowest precicion (month). For each precision level the
 * algorithm looks at the list of valid values and finds a value for each that is equal to or greater then
 * the valid values supplied. If no equal or
 * greater value was supplied, it will reset all higher precision elements to its minimum value.
 */
public final class ScheduleComputeHelper
{
    /**
     * Minimum time to next occurance.
     */
    private static final int MIN_OFFSET_MSEC = 1000;

    /**
     * Computes the next lowest date in milliseconds based on a specification and the
     * from-time passed in.
     * @param spec defines the schedule
     * @param afterTimeInMillis defines the start time
     * @return a long date millisecond value for the next schedule occurance matching the spec
     */
    public static long computeNextOccurance(ScheduleSpec spec, long afterTimeInMillis)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".computeNextOccurance Computing next occurance, afterTimeInMillis=" + (new Date(afterTimeInMillis)) +
                      "  as long=" + afterTimeInMillis +
                      "  spec=" + spec);
        }


        // Add the minimum resolution to the start time to ensure we don't get the same exact time
        if (spec.getUnitValues().containsKey(ScheduleUnit.SECONDS))
        {
            afterTimeInMillis += MIN_OFFSET_MSEC;
        }
        else
        {
            afterTimeInMillis += 60 * MIN_OFFSET_MSEC;
        }

        Date result = compute(spec, afterTimeInMillis);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".computeNextOccurance Completed, result=" + result + "  long=" + result.getTime());
        }

        return result.getTime();
    }

    private static Date compute(ScheduleSpec spec, long afterTimeInMillis)
    {
        while (true)
        {
            Calendar after = Calendar.getInstance();
            after.setTimeInMillis(afterTimeInMillis);

            ScheduleCalendar result = new ScheduleCalendar();
            result.setMilliseconds(after.get(Calendar.MILLISECOND));

            SortedSet<Integer> minutesSet = spec.getUnitValues().get(ScheduleUnit.MINUTES);
            SortedSet<Integer> hoursSet = spec.getUnitValues().get(ScheduleUnit.HOURS);
            SortedSet<Integer> monthsSet = spec.getUnitValues().get(ScheduleUnit.MONTHS);
            SortedSet<Integer> secondsSet = null;
            boolean isSecondsSpecified = false;

            if (spec.getUnitValues().containsKey(ScheduleUnit.SECONDS))
            {
                isSecondsSpecified = true;
                secondsSet = spec.getUnitValues().get(ScheduleUnit.SECONDS);
            }

            if (isSecondsSpecified)
            {
                result.setSecond(nextValue(secondsSet, after.get(Calendar.SECOND)));
                if (result.getSecond() == -1)
                {
                    result.setSecond(nextValue(secondsSet, 0));
                    after.add(Calendar.MINUTE, 1);
                }
            }

            result.setMinute(nextValue(minutesSet, after.get(Calendar.MINUTE)));
            if (result.getMinute() != after.get(Calendar.MINUTE))
            {
                result.setSecond(nextValue(secondsSet, 0));
            }
            if (result.getMinute() == -1)
            {
                result.setMinute(nextValue(minutesSet, 0));
                after.add(Calendar.HOUR_OF_DAY, 1);
            }

            result.setHour(nextValue(hoursSet, after.get(Calendar.HOUR_OF_DAY)));
            if (result.getHour() != after.get(Calendar.HOUR_OF_DAY))
            {
                result.setSecond(nextValue(secondsSet, 0));
                result.setMinute(nextValue(minutesSet, 0));
            }
            if (result.getHour() == -1)
            {
                result.setHour(nextValue(hoursSet, 0));
                after.add(Calendar.DAY_OF_MONTH, 1);
            }

            // This call may change second, minute and/or hour parameters
            // They may be reset to minimum values if the day rolled
            result.setDayOfMonth(determineDayOfMonth(spec, after, result));

            boolean dayMatchRealDate = false;
            while (!dayMatchRealDate)
            {
                if (checkDayValidInMonth(result.getDayOfMonth(), after.get(Calendar.MONTH), after.get(Calendar.YEAR)))
                {
                    dayMatchRealDate = true;
                }
                else
                {
                    after.add(Calendar.MONTH, 1);
                }
            }

            int currentMonth = after.get(Calendar.MONTH) + 1;
            result.setMonth(nextValue(monthsSet, currentMonth));
            if (result.getMonth() != currentMonth)
            {
                result.setSecond(nextValue(secondsSet, 0));
                result.setMinute(nextValue(minutesSet, 0));
                result.setHour(nextValue(hoursSet, 0));
                result.setDayOfMonth(determineDayOfMonth(spec, after, result));
            }
            if (result.getMonth() == -1)
            {
                result.setMonth(nextValue(monthsSet, 0));
                after.add(Calendar.YEAR, 1);
            }

            // Perform a last valid date check, if failing, try to compute a new date based on this altered after date
            int year = after.get(Calendar.YEAR);
            if (!(checkDayValidInMonth(result.getDayOfMonth(), result.getMonth() - 1, year)))
            {
                afterTimeInMillis = after.getTimeInMillis();
                continue;
            }

            return getTime(result, after.get(Calendar.YEAR));
        }
    }

    /*
     * Determine the next valid day of month based on the given specification of valid days in month and
     * valid days in week. If both days in week and days in month are supplied, the days are OR-ed.
     */
    private static int determineDayOfMonth(ScheduleSpec spec,
                                    Calendar after,
                                    ScheduleCalendar result)
    {
        SortedSet<Integer> daysOfMonthSet = spec.getUnitValues().get(ScheduleUnit.DAYS_OF_MONTH);
        SortedSet<Integer> daysOfWeekSet = spec.getUnitValues().get(ScheduleUnit.DAYS_OF_WEEK);
        SortedSet<Integer> secondsSet = spec.getUnitValues().get(ScheduleUnit.SECONDS);
        SortedSet<Integer> minutesSet = spec.getUnitValues().get(ScheduleUnit.MINUTES);
        SortedSet<Integer> hoursSet = spec.getUnitValues().get(ScheduleUnit.HOURS);

        int dayOfMonth;

        // If days of week is a wildcard, just go by days of month
        if (daysOfWeekSet == null)
        {
            dayOfMonth = nextValue(daysOfMonthSet, after.get(Calendar.DAY_OF_MONTH));
            if (dayOfMonth != after.get(Calendar.DAY_OF_MONTH))
            {
                result.setSecond(nextValue(secondsSet, 0));
                result.setMinute(nextValue(minutesSet, 0));
                result.setHour(nextValue(hoursSet, 0));
            }
            if (dayOfMonth == -1)
            {
                dayOfMonth = nextValue(daysOfMonthSet, 0);
                after.add(Calendar.MONTH, 1);
            }
        }
        // If days of weeks is not a wildcard and days of month is a wildcard, go by days of week only
        else if (daysOfMonthSet == null)
        {
            // Loop to find the next day of month that works for the specified day of week values
            while(true)
            {
                dayOfMonth = after.get(Calendar.DAY_OF_MONTH);
                int dayOfWeek = after.get(Calendar.DAY_OF_WEEK) - 1;

                // If the day matches neither the day of month nor the day of week
                if (!daysOfWeekSet.contains(dayOfWeek))
                {
                    result.setSecond(nextValue(secondsSet, 0));
                    result.setMinute(nextValue(minutesSet, 0));
                    result.setHour(nextValue(hoursSet, 0));
                    after.add(Calendar.DAY_OF_MONTH, 1);
                }
                else
                {
                    break;
                }
            }
        }
        // Both days of weeks and days of month are not a wildcard
        else
        {
            // Loop to find the next day of month that works for either day of month  OR   day of week
            while(true)
            {
                dayOfMonth = after.get(Calendar.DAY_OF_MONTH);
                int dayOfWeek = after.get(Calendar.DAY_OF_WEEK) - 1;

                // If the day matches neither the day of month nor the day of week
                if ((!daysOfWeekSet.contains(dayOfWeek)) &&
                    (!daysOfMonthSet.contains(dayOfMonth)))
                {
                    result.setSecond(nextValue(secondsSet, 0));
                    result.setMinute(nextValue(minutesSet, 0));
                    result.setHour(nextValue(hoursSet, 0));
                    after.add(Calendar.DAY_OF_MONTH, 1);
                }
                else
                {
                    break;
                }
            }
        }

        return dayOfMonth;
    }

    private static Date getTime(ScheduleCalendar result, int year)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, result.getMonth() - 1, result.getDayOfMonth(), result.getHour(), result.getMinute(), result.getSecond());
        calendar.set(Calendar.MILLISECOND, result.getMilliseconds());
        return calendar.getTime();
    }

    /*
     * Check if this is a valid date.
     */
    private static boolean checkDayValidInMonth(int day, int month, int year)
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.getTime();
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        return true;
    }

    /*
     * Determine if in the supplied valueSet there is a value after the given start value.
     * Return -1 to indicate that there is no value after the given startValue.
     * If the valueSet passed is null it is treated as a wildcard and the same startValue is returned
     */
    private static int nextValue(SortedSet<Integer> valueSet, int startValue)
    {
        if (valueSet == null)
        {
            return startValue;
        }

        if (valueSet.contains(startValue))
        {
            return startValue;
        }

        SortedSet<Integer> tailSet = valueSet.tailSet(startValue + 1);

        if (tailSet.isEmpty())
        {
            return -1;
        }
        else
        {
            return tailSet.first();
        }
    }

    private static final Log log = LogFactory.getLog(ScheduleComputeHelper.class);
}

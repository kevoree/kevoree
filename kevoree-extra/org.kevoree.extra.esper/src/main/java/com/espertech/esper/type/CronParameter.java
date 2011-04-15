/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.type;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Hold parameters for timer:at.
 */
public class CronParameter implements NumberSetParameter {
    private CronOperatorEnum operator;
    private Calendar calendar;
    private Integer day, month;

    private static int FIRST_DAY_OF_WEEK = Calendar.SUNDAY;
    private static final long serialVersionUID = -4006350378033980878L;

    /**
     * Ctor.
     * @param operator is the operator as text
     * @param day is the day text
     * @param engineTime is the current engine time
     */
    public CronParameter(CronOperatorEnum operator, Integer day, long engineTime) {
        this.operator = operator;
        this.day = day;
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(engineTime);
        calendar.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
    }

    /**
     * Sets the month value.
     * @param month to set
     */
    public void setMonth(int month) {
        this.month = month - 1;
    }

    public boolean isWildcard(int min, int max) {
        return false;
    }

    public Set<Integer> getValuesInRange(int min, int max) {
        Set<Integer> values = new HashSet<Integer>();
        if (((min != 0) && (min != 1)) || ((max != 6) && (max != 31))) {
            throw new IllegalArgumentException("Invalid usage for timer:at");
        }
        if (operator == CronOperatorEnum.LASTDAY)
        {
            // If max=6, determine last day of Week (In US Saturday=7)
            if ((min==0) && (max == 6)) {
                if (day == null) {
                    values.add(determineLastDayOfWeek());
                } else {
                    values.add(determineLastDayOfWeekInMonth());
                }
            }  else if ((min==1) && (max == 31)) {
                // "Last day of month"
                // or "the last xxx day of the month"
                if (day == null) {
                    values.add(determineLastDayOfMonth());
                } else {
                    values.add(determineLastDayOfWeekInMonth());
                }
            } else {
                throw new IllegalArgumentException("Invalid value for last operator");
            }
        }
        else if (operator == CronOperatorEnum.LASTWEEKDAY)
        {
            values.add(determineLastWeekDayOfMonth());
        }
        else if (operator == CronOperatorEnum.WEEKDAY)
        {
            values.add(determineLastWeekDayOfMonth());
        }
        else
        {
            throw new IllegalArgumentException("Invalid special operator for observer");
        }
        return values;
    }

    private int determineLastDayOfMonth() {
        setTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    private int determineLastDayOfWeekInMonth() {
        if (day == null) {
            return determineLastDayOfMonth();
        }
        if (day<0 || day>7) {
            throw new IllegalArgumentException("Last xx day of the month has to be a day of week (0-7)");
        }
        int dayOfWeek =  getDayOfWeek();
        setTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        int dayDiff = calendar.get(Calendar.DAY_OF_WEEK) - dayOfWeek;
        if (dayDiff > 0) {
            calendar.add(Calendar.DAY_OF_WEEK, -dayDiff);
        } else if (dayDiff < 0) {
            calendar.add(Calendar.DAY_OF_WEEK, -7 - dayDiff);
        }
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    private int determineLastDayOfWeek() {
        setTime();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private int getDayOfWeek() {
        setTime();
        if (day == null) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, day + 1);
        }
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    private int determineLastWeekDayOfMonth() {
      int computeDay = (day == null)? determineLastDayOfMonth(): day;
      setTime();
      if (!checkDayValidInMonth(computeDay, calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))) {
          throw new IllegalArgumentException("Invalid day for " +  calendar.get(Calendar.MONTH));
      }
      calendar.set(Calendar.DAY_OF_MONTH, computeDay);
      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      if ((dayOfWeek >=Calendar.MONDAY) && (dayOfWeek<=Calendar.FRIDAY)) {
        return computeDay;
      }
      if (dayOfWeek == Calendar.SATURDAY) {
          if (computeDay == 1) {
            calendar.add(Calendar.DAY_OF_MONTH, +2);
          } else {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
          }
      }
      if (dayOfWeek == Calendar.SUNDAY) {
          if ((computeDay == 28) || (computeDay==29) || (computeDay==30) || (computeDay==31)) {
           calendar.add(Calendar.DAY_OF_MONTH, -2);
          } else {
           calendar.add(Calendar.DAY_OF_MONTH, +2);
          }
      }
      return calendar.get(Calendar.DAY_OF_MONTH);
    }

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

    private void setTime() {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        if (month != null) {
          calendar.set(Calendar.MONTH, month);
        }
    }

}

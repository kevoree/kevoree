/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

/**
 * Enum for the type of rate for output-rate limiting.
 */
public enum OutputLimitRateType
{
    /**
     * Output by number of events.
     */
    EVENTS,

    /**
     * Output following a crontab-like schedule.
     */
    CRONTAB,

    /**
     * Output when an expression turns true.
     */
    WHEN_EXPRESSION,

    /**
     * Output based on a time period passing.
     */
    TIME_PERIOD,

    /**
     * Output after a given time period
     */
    AFTER
}

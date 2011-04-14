/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprTimePeriod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class OutputConditionPolledTime implements OutputConditionPolled
{
    private ExprTimePeriod timePeriod;
    private long msecIntervalSize;
    private StatementContext context;
    private long lastUpdate;

    /**
     * Constructor.
     * @param timePeriod is the number of minutes or seconds to batch events for, may include variables
     * @param context is the view context for time scheduling
     */
    public OutputConditionPolledTime(ExprTimePeriod timePeriod,
                               StatementContext context)
    {
        if (context == null)
        {
            String message = "OutputConditionTime requires a non-null view context";
            throw new NullPointerException(message);
        }

        this.context = context;
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
        this.lastUpdate = -msecIntervalSize - 1;
    }

    public boolean updateOutputCondition(int newEventsCount, int oldEventsCount)
    {
        // If we pull the interval from a variable, then we may need to reschedule
        if (timePeriod.hasVariable())
        {
            Double numSeconds = (Double) timePeriod.evaluate(null, true, context);
            if (numSeconds != null)
            {
                long newMsecIntervalSize = Math.round(1000 * numSeconds);
                this.msecIntervalSize = newMsecIntervalSize;
            }
        }

        long current = context.getTimeProvider().getTime();
        if (current - lastUpdate >= msecIntervalSize) {
            this.lastUpdate = current;
            return true;
        }
        return false;
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " msecIntervalSize=" + msecIntervalSize;
    }

    private static final Log log = LogFactory.getLog(OutputConditionPolledTime.class);
}
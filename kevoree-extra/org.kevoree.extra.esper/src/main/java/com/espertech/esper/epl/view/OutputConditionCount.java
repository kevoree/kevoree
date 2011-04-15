/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.epl.variable.VariableReader;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Output limit condition that is satisfied when either
 * the total number of new events arrived or the total number
 * of old events arrived is greater than a preset value.
 */
public final class OutputConditionCount implements OutputCondition
{
    private static final boolean DO_OUTPUT = true;
	private static final boolean FORCE_UPDATE = false;

    private long eventRate;
    private int newEventsCount;
    private int oldEventsCount;
    private final OutputCallback outputCallback;
    private final VariableReader variableReader;


    /**
     * Constructor.
     * @param eventRate is the number of old or new events that
     * must arrive in order for the condition to be satisfied
     * @param outputCallback is the callback that is made when the conditoin is satisfied
     * @param variableReader is for reading the variable value, if a variable was supplied, else null
     */
    public OutputConditionCount(int eventRate, VariableReader variableReader, OutputCallback outputCallback)
    {
        if ((eventRate < 1) && (variableReader == null))
        {
            throw new IllegalArgumentException("Limiting output by event count requires an event count of at least 1 or a variable name");
        }
		if(outputCallback ==  null)
		{
			throw new NullPointerException("Output condition by count requires a non-null callback");
		}
        this.eventRate = eventRate;
        this.outputCallback = outputCallback;
        this.variableReader = variableReader;
    }

    /**
     * Returns the number of new events.
     * @return number of new events
     */
    public int getNewEventsCount() {
		return newEventsCount;
	}

    /**
     * Returns the number of old events.
     * @return number of old events
     */
	public int getOldEventsCount() {
		return oldEventsCount;
	}

    /**
     * Returns the event rate.
     * @return event rate
     */
    public final long getEventRate()
    {
        return eventRate;
    }

    public final void updateOutputCondition(int newDataCount, int oldDataCount)
    {
        if (variableReader != null)
        {
            Object value = variableReader.getValue();
            if (value != null)
            {
                eventRate = ((Number) value).longValue();
            }
        }

        this.newEventsCount += newDataCount;
        this.oldEventsCount += oldDataCount;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".updateBatchCondition, " +
                    "  newEventsCount==" + newEventsCount +
                    "  oldEventsCount==" + oldEventsCount);
        }

        if (isSatisfied())
        {
        	if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug(".updateOutputCondition() condition satisfied");
            }
            this.newEventsCount = 0;
            this.oldEventsCount = 0;
            outputCallback.continueOutputProcessing(DO_OUTPUT, FORCE_UPDATE);
        }
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " eventRate=" + eventRate;
    }

    private boolean isSatisfied()
    {
    	return (newEventsCount >= eventRate) || (oldEventsCount >= eventRate);
    }

    private static final Log log = LogFactory.getLog(OutputConditionCount.class);




}

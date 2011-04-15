/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.metric;

/**
 * Reports engine-level instrumentation values.
 */
public class EngineMetric extends MetricEvent
{
    private final long timestamp;
    private final long inputCount;
    private final long scheduleDepth;

    /**
     * Ctor.
     * @param engineURI engine URI
     * @param timestamp engine timestamp
     * @param inputCount number of input events
     * @param scheduleDepth schedule depth
     */
    public EngineMetric(String engineURI, long timestamp, long inputCount, long scheduleDepth)
    {
        super(engineURI);
        this.timestamp = timestamp;
        this.inputCount = inputCount;
        this.scheduleDepth = scheduleDepth;
    }

    /**
     * Returns input count.
     * @return input count
     */
    public long getInputCount()
    {
        return inputCount;
    }

    /**
     * Returns schedule depth.
     * @return schedule depth
     */
    public long getScheduleDepth()
    {
        return scheduleDepth;
    }

    /**
     * Returns engine timestamp.
     * @return timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }
}

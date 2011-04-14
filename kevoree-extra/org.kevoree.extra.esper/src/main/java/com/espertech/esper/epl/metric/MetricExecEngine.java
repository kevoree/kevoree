/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.metric;

import com.espertech.esper.client.metric.EngineMetric;

/**
 * Metrics execution producing engine metric events.
 */
public class MetricExecEngine implements MetricExec
{
    private final MetricEventRouter metricEventRouter;
    private final String engineURI;
    private final MetricScheduleService metricScheduleService;
    private final long interval;

    /**
     * Ctor.
     * @param metricEventRouter for routing metric events
     * @param engineURI engine uri
     * @param metricScheduleService for scheduling a new execution
     * @param interval for rescheduling the execution
     */
    public MetricExecEngine(MetricEventRouter metricEventRouter, String engineURI, MetricScheduleService metricScheduleService, long interval)
    {
        this.metricEventRouter = metricEventRouter;
        this.engineURI = engineURI;
        this.metricScheduleService = metricScheduleService;
        this.interval = interval;
    }

    public void execute(MetricExecutionContext context)
    {
        long inputCount = context.getServices().getFilterService().getNumEventsEvaluated();
        long schedDepth = context.getServices().getSchedulingService().getScheduleHandleCount();
        EngineMetric metric = new EngineMetric(engineURI, metricScheduleService.getCurrentTime(), inputCount, schedDepth);
        metricEventRouter.route(metric);
        metricScheduleService.add(interval, this);        
    }

    /**
     * Returns reporting interval.
     * @return reporting interval
     */
    public long getInterval()
    {
        return interval;
    }
}

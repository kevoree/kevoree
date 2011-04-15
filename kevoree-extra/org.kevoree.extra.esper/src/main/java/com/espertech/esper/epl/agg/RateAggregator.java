/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.epl.core.MethodResolutionService;

/**
 * Aggregation computing an event arrival rate for data windowed-events.
 */
public class RateAggregator implements AggregationMethod {

    private double accumulator;
    private long latest;
    private long oldest;
    private boolean isSet = false;

    public void enter(Object value) {
        if (value.getClass().isArray()) {
            final Object[] params = (Object[]) value;
            Number val = (Number) params[1];
            accumulator += val.doubleValue();
            latest = (Long) params[0];
        } else {
            accumulator += 1;
            latest = (Long) value;
        }
    }

    public void leave(Object value) {
        if (value.getClass().isArray()) {
            final Object[] params = (Object[]) value;
            Number val = (Number) params[1];
            accumulator -= val.doubleValue();
            oldest = (Long) params[0];
        } else {
            accumulator -= 1;
            oldest = (Long) value;
        }
        if (!isSet) isSet = true;
    }

    public Object getValue() {
        if (!isSet) return null;
        return (accumulator * 1000) / (latest - oldest);
    }

    public void clear() {
        accumulator = 0;
        latest = 0;
        oldest = 0;
    }

    public Class getValueType() {
        return Double.class;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService) {
        return methodResolutionService.makeRateAggregator();
    }
}
/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.epl.agg.AggregationMethod;
import com.espertech.esper.epl.core.MethodResolutionService;

/**
 * Counts all datapoints including null values.
 */
public class CountAggregator implements AggregationMethod
{
    private long numDataPoints;

    public void clear()
    {
        numDataPoints = 0;
    }

    public void enter(Object object)
    {
        numDataPoints++;
    }

    public void leave(Object object)
    {
        numDataPoints--;
    }

    public Object getValue()
    {
        return numDataPoints;
    }

    public Class getValueType()
    {
        return Long.class;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makeCountAggregator(false);
    }
}

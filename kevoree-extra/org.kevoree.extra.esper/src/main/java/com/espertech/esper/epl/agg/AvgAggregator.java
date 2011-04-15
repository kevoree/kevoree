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
 * Average that generates double-typed numbers.
 */
public class AvgAggregator implements AggregationMethod
{
    private double sum;
    private long numDataPoints;

    public void clear()
    {
        sum = 0;
        numDataPoints = 0;
    }

    public void enter(Object object)
    {
        if (object == null)
        {
            return;
        }
        numDataPoints++;
        sum += ((Number) object).doubleValue();
    }

    public void leave(Object object)
    {
        if (object == null)
        {
            return;
        }
        numDataPoints--;
        sum -= ((Number) object).doubleValue();
    }

    public Object getValue()
    {
        if (numDataPoints == 0)
        {
            return null;
        }
        return sum / numDataPoints;
    }

    public Class getValueType()
    {
        return Double.class;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makeAvgAggregator(Double.class);
    }
}

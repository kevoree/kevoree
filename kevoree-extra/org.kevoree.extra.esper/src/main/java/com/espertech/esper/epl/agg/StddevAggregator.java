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
 * Standard deviation always generates double-typed numbers.
 */
public class StddevAggregator implements AggregationMethod
{
    private double sum;
    private double sumSq;
    private long numDataPoints;

    public void clear()
    {
        sum = 0;
        sumSq = 0;
        numDataPoints = 0;
    }

    public void enter(Object object)
    {
        if (object == null)
        {
            return;
        }

        double value = ((Number) object).doubleValue();

        numDataPoints++;
        sum += value;
        sumSq += value * value;
    }

    public void leave(Object object)
    {
        if (object == null)
        {
            return;
        }

        double value = ((Number) object).doubleValue();

        numDataPoints--;
        sum -= value;
        sumSq -= value * value;
    }

    public Object getValue()
    {
        if (numDataPoints < 2)
        {
            return null;
        }

        double variance = (sumSq - sum * sum / numDataPoints) / (numDataPoints - 1);
        return Math.sqrt(variance);
    }

    public Class getValueType()
    {
        return Double.class;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makeStddevAggregator();
    }
}

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

import java.math.BigInteger;

/**
 * Sum for BigInteger values.
 */
public class BigIntegerSumAggregator implements AggregationMethod
{
    private BigInteger sum;
    private long numDataPoints;

    /**
     * Ctor.
     */
    public BigIntegerSumAggregator()
    {
        sum = BigInteger.valueOf(0);
    }

    public void clear()
    {
        sum = BigInteger.valueOf(0);
        numDataPoints = 0;
    }

    public void enter(Object object)
    {
        if (object == null)
        {
            return;
        }
        numDataPoints++;
        sum = sum.add((BigInteger)object);
    }

    public void leave(Object object)
    {
        if (object == null)
        {
            return;
        }
        numDataPoints--;
        sum = sum.subtract((BigInteger)object);
    }

    public Object getValue()
    {
        if (numDataPoints == 0)
        {
            return null;
        }
        return sum;
    }

    public Class getValueType()
    {
        return BigInteger.class;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makeSumAggregator(BigInteger.class);
    }
}

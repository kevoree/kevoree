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
import com.espertech.esper.collection.SortedDoubleVector;

/**
 * Median aggregation.
 */
public class MedianAggregator implements AggregationMethod
{
    private SortedDoubleVector vector;

    public void clear()
    {
        vector.clear();
    }

    /**
     * Ctor.
     */
    public MedianAggregator()
    {
        this.vector = new SortedDoubleVector();
    }

    public void enter(Object object)
    {
        if (object == null)
        {
            return;
        }
        double value = ((Number) object).doubleValue();
        vector.add(value);
    }

    public void leave(Object object)
    {
        if (object == null)
        {
            return;
        }
        double value = ((Number) object).doubleValue();
        vector.remove(value);
    }

    public Object getValue()
    {
        if (vector.size() == 0)
        {
            return null;
        }
        if (vector.size() == 1)
        {
            return vector.getValue(0);
        }

        int middle = vector.size() >> 1;
        if (vector.size() % 2 == 0)
        {
            return (vector.getValue(middle - 1) + vector.getValue(middle)) / 2;
        }
        else
        {
            return vector.getValue(middle);
        }
    }

    public Class getValueType()
    {
        return Double.class;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makeMedianAggregator();
    }
}

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
import com.espertech.esper.type.MinMaxTypeEnum;
import com.espertech.esper.collection.SortedRefCountedSet;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Min/max aggregator for all values, not considering events leaving the aggregation (i.e. ever).
 */
public class MinMaxEverAggregator implements AggregationMethod
{
    private static final Log log = LogFactory.getLog(MinMaxEverAggregator.class);

    private final MinMaxTypeEnum minMaxTypeEnum;
    private final Class returnType;

    private Comparable currentMinMax;

    /**
     * Ctor.
     *
     * @param minMaxTypeEnum - enum indicating to return minimum or maximum values
     * @param returnType     - is the value type returned by aggregator
     */
    public MinMaxEverAggregator(MinMaxTypeEnum minMaxTypeEnum, Class returnType)
    {
        this.minMaxTypeEnum = minMaxTypeEnum;
        this.returnType = returnType;
    }

    public void clear()
    {
        currentMinMax = null;
    }

    public void enter(Object object)
    {
        if (object == null)
        {
            return;
        }
        if (currentMinMax == null) {
            currentMinMax = (Comparable) object;
            return;
        }
        if (minMaxTypeEnum == MinMaxTypeEnum.MAX) {
            if (currentMinMax.compareTo(object) < 0) {
                currentMinMax = (Comparable) object;
            }
        }
        else {
            if (currentMinMax.compareTo(object) > 0) {
                currentMinMax = (Comparable) object;
            }
        }
    }

    public void leave(Object object)
    {
        // no-op, this is designed to handle min-max ever
        log.warn(".leave Received remove stream, none was expected");
    }

    public Object getValue()
    {
        return currentMinMax;
    }

    public Class getValueType()
    {
        return returnType;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makeMinMaxAggregator(minMaxTypeEnum, returnType, false);
    }
}
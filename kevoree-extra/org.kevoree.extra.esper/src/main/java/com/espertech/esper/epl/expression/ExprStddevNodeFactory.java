/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.agg.*;
import com.espertech.esper.epl.core.MethodResolutionService;

public class ExprStddevNodeFactory implements AggregationMethodFactory
{
    private final boolean isDistinct;
    private final Class aggregatedValueType;

    public ExprStddevNodeFactory(boolean isDistinct, Class aggregatedValueType)
    {
        this.isDistinct = isDistinct;
        this.aggregatedValueType = aggregatedValueType;
    }

    public Class getResultType()
    {
        return Double.class;
    }

    public AggregationSpec getSpec(boolean isMatchRecognize)
    {
        return null;
    }

    public AggregationMethod getPrototypeAggregator(MethodResolutionService methodResolutionService)
    {
        AggregationMethod method = methodResolutionService.makeStddevAggregator();
        if (!isDistinct) {
            return method;
        }
        return methodResolutionService.makeDistinctAggregator(method, aggregatedValueType);
    }

    public AggregationAccessor getAccessor()
    {
        throw new UnsupportedOperationException();
    }
}
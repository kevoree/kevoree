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

public class ExprCountNodeFactory implements AggregationMethodFactory
{
    private final boolean ignoreNulls;
    private final boolean isDistinct;
    private final Class countedValueType;

    public ExprCountNodeFactory(boolean ignoreNulls, boolean isDistinct, Class countedValueType)
    {
        this.ignoreNulls = ignoreNulls;
        this.isDistinct = isDistinct;
        this.countedValueType = countedValueType;
    }

    public Class getResultType()
    {
        return Long.class;
    }

    public AggregationSpec getSpec(boolean isMatchRecognize)
    {
        return null;
    }

    public AggregationMethod getPrototypeAggregator(MethodResolutionService methodResolutionService)
    {
        AggregationMethod method = methodResolutionService.makeCountAggregator(ignoreNulls);
        if (!isDistinct) {
            return method;
        }
        return methodResolutionService.makeDistinctAggregator(method, countedValueType);
    }

    public AggregationAccessor getAccessor()
    {
        throw new UnsupportedOperationException();
    }
}
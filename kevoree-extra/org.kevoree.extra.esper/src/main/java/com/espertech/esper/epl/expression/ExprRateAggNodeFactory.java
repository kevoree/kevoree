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

public class ExprRateAggNodeFactory implements AggregationMethodFactory
{
    private final boolean isEver;
    private final long intervalMSec;

    public ExprRateAggNodeFactory(boolean isEver, long intervalMSec)
    {
        this.isEver = isEver;
        this.intervalMSec = intervalMSec;
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
        if (isEver) {
            return methodResolutionService.makeRateEverAggregator(intervalMSec);
        }
        else {
            return methodResolutionService.makeRateAggregator();
        }
    }

    public AggregationAccessor getAccessor()
    {
        throw new UnsupportedOperationException();
    }
}
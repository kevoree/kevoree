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
 * Base class for use with plug-in aggregation functions.
 */
public abstract class AggregationSupport implements AggregationMethod
{
    /**
     * Provides the aggregation function name.
     */
    protected String functionName;

    /**
     * Implemented by plug-in aggregation functions to allow such functions to validate the
     * type of values passed to the function at statement compile time and to generally
     * interrogate parameter expressions.
     * @param validationContext expression information
     */
    public abstract void validate(AggregationValidationContext validationContext);

    /**
     * Ctor.
     */
    public AggregationSupport()
    {
    }

    /**
     * Sets the aggregation function name.
     * @param functionName is the name of the aggregation function
     */
    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }

    /**
     * Returns the name of the aggregation function.
     * @return aggregation function name
     */
    public String getFunctionName()
    {
        return functionName;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService)
    {
        return methodResolutionService.makePlugInAggregator(functionName);
    }
}

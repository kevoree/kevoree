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
 * Factory for aggregation methods.
 */
public interface AggregationMethodFactory
{
    /**
     * Returns the result type of an aggregation.
     * @return result
     */
    public Class getResultType();

    /**
     * Returns the spec for aggregation.
     * @param isMatchRecognize true if match-recognize pattern
     * @return spec
     */
    public AggregationSpec getSpec(boolean isMatchRecognize);

    /**
     * Returns the prototype aggregation method for copying
     * @param methodResolutionService to obtain prototype from
     * @return prototype aggregation method
     */
    public AggregationMethod getPrototypeAggregator(MethodResolutionService methodResolutionService);

    /**
     * Returns the accessor to use.
     * @return accessor
     */
    public AggregationAccessor getAccessor();
}
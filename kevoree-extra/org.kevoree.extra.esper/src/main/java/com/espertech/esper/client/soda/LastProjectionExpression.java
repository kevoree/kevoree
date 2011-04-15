/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

/**
 * Represents the "last" aggregation function.
 */
public class LastProjectionExpression extends AccessProjectionExpressionBase
{
    private static final long serialVersionUID = -6851246065918661653L;
    
    /**
     * Ctor.
     */
    public LastProjectionExpression() {
    }

    /**
     * Ctor.
     * @param expression to aggregate
     */
    public LastProjectionExpression(Expression expression)
    {
        super(expression);
    }

    @Override
    public String getAggregationFunctionName() {
        return "last";
    }
}
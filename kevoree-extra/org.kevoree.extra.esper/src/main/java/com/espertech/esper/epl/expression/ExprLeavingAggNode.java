/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.agg.AggregationMethodFactory;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;

/**
 * Represents the leaving() aggregate function is an expression tree.
 */
public class ExprLeavingAggNode extends ExprAggregateNode
{
    private static final long serialVersionUID = -261718190573572758L;
    
    /**
     * Ctor.
     * @param distinct - flag indicating unique or non-unique value aggregation
     */
    public ExprLeavingAggNode(boolean distinct)
    {
        super(distinct);
    }

    public AggregationMethodFactory validateAggregationChild(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        String message = "The leaving aggregation function requires no parameters";
        if (this.getChildNodes().size() > 0) {
            throw new ExprValidationException(message);
        }

        return new ExprLeavingAggNodeFactory();
    }

    protected String getAggregationFunctionName()
    {
        return "leaving";
    }

    public final boolean equalsNodeAggregate(ExprAggregateNode node)
    {
        return node instanceof ExprLeavingAggNode;
    }
}
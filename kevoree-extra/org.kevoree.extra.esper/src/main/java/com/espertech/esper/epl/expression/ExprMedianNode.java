/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.agg.AggregationMethod;
import com.espertech.esper.epl.agg.AggregationMethodFactory;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;

/**
 * Represents the median(...) aggregate function is an expression tree.
 */
public class ExprMedianNode extends ExprAggregateNode
{
    private static final long serialVersionUID = 1762260589769465944L;

    /**
     * Ctor.
     * @param distinct - flag indicating unique or non-unique value aggregation
     */
    public ExprMedianNode(boolean distinct)
    {
        super(distinct);
    }

    public AggregationMethodFactory validateAggregationChild(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        Class childType = super.validateSingleNumericChild(streamTypeService);
        return new ExprMedianNodeFactory(super.isDistinct, childType);
    }

    protected String getAggregationFunctionName()
    {
        return "median";
    }

    public final boolean equalsNodeAggregate(ExprAggregateNode node)
    {
        if (!(node instanceof ExprMedianNode))
        {
            return false;
        }

        return true;
    }
}

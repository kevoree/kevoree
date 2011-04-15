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
 * Represents the nth(...) and aggregate function is an expression tree.
 */
public class ExprNthAggNode extends ExprAggregateNode
{
    private static final long serialVersionUID = -843689785630260527L;
    
    /**
     * Ctor.
     * @param distinct - flag indicating unique or non-unique value aggregation
     */
    public ExprNthAggNode(boolean distinct)
    {
        super(distinct);
    }

    public AggregationMethodFactory validateAggregationChild(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        String message = "The nth aggregation function requires two parameters, an expression returning aggregation values and a numeric index constant";
        if (this.getChildNodes().size() != 2) {
            throw new ExprValidationException(message);
        }

        ExprNode first = this.getChildNodes().get(0);
        ExprNode second = this.getChildNodes().get(1);
        if (!second.isConstantResult()) {
            throw new ExprValidationException(message);
        }

        Number num = (Number) second.getExprEvaluator().evaluate(null, true, exprEvaluatorContext);
        int size = num.intValue();

        return new ExprNthAggNodeFactory(first.getExprEvaluator().getType(), size, super.isDistinct);
    }

    protected String getAggregationFunctionName()
    {
        return "nth";
    }

    public final boolean equalsNodeAggregate(ExprAggregateNode node)
    {
        return node instanceof ExprNthAggNode;
    }
}
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
import com.espertech.esper.util.JavaClassHelper;

/**
 * Represents the rate(...) and aggregate function is an expression tree.
 */
public class ExprRateAggNode extends ExprAggregateNode
{
    private static final long serialVersionUID = -1616393720555472129L;
    
    /**
     * Ctor.
     * @param distinct - flag indicating unique or non-unique value aggregation
     */
    public ExprRateAggNode(boolean distinct)
    {
        super(distinct);
    }

    public AggregationMethodFactory validateAggregationChild(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() == 0) {
            throw new ExprValidationException("The rate aggregation function minimally requires a numeric constant or expression as a parameter.");            
        }

        ExprNode first = this.getChildNodes().get(0);
        if (first.isConstantResult()) {
            String message = "The rate aggregation function requires a numeric constant or time period as the first parameter in the constant-value notation";
            long intervalMSec;
            if (first instanceof ExprTimePeriod) {
                double secInterval = (Double) ((ExprTimePeriod) first).evaluate(null, true, exprEvaluatorContext);
                intervalMSec = Math.round(secInterval * 1000d);
            }
            else if (first instanceof ExprConstantNode) {
                if (!JavaClassHelper.isNumeric(first.getExprEvaluator().getType())) {
                    throw new ExprValidationException(message);
                }
                Number num = (Number) first.getExprEvaluator().evaluate(null, true, exprEvaluatorContext);
                intervalMSec = Math.round(num.doubleValue() * 1000d);
            }
            else {
                throw new ExprValidationException(message);
            }

            return new ExprRateAggNodeFactory(true, intervalMSec);
        }
        else {
            String message = "The rate aggregation function requires a property or expression returning a non-constant long-type value as the first parameter in the timestamp-property notation";
            Class boxedParamOne = JavaClassHelper.getBoxedType(first.getExprEvaluator().getType());
            if (boxedParamOne != Long.class) {
                throw new ExprValidationException(message);
            }
            if (first.isConstantResult()) {
                throw new ExprValidationException(message);
            }
            if (first instanceof ExprTimestampNode) {
                throw new ExprValidationException("The rate aggregation function does not allow the current engine timestamp as a parameter");
            }
            if (this.getChildNodes().size() > 1) {
                if (!JavaClassHelper.isNumeric(this.getChildNodes().get(1).getExprEvaluator().getType())) {
                    throw new ExprValidationException("The rate aggregation function accepts an expression returning a numeric value to accumulate as an optional second parameter");
                }
            }
            boolean hasDataWindows = ExprNodeUtility.hasRemoveStream(first, streamTypeService);
            if (!hasDataWindows) {
                throw new ExprValidationException("The rate aggregation function in the timestamp-property notation requires data windows");
            }
            return new ExprRateAggNodeFactory(false, -1);
        }
    }

    protected String getAggregationFunctionName()
    {
        return "rate";
    }

    public final boolean equalsNodeAggregate(ExprAggregateNode node)
    {
        return node instanceof ExprRateAggNode;
    }
}
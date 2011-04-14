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
import com.espertech.esper.epl.agg.AggregationSupport;
import com.espertech.esper.epl.agg.AggregationValidationContext;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;

/**
 * Represents a custom aggregation function in an expresson tree.
 */
public class ExprPlugInAggFunctionNode extends ExprAggregateNode
{
    private transient AggregationSupport aggregationSupport;
    private static final long serialVersionUID = 4512085880102791194L;

    /**
     * Ctor.
     * @param distinct - flag indicating unique or non-unique value aggregation
     * @param aggregationSupport - is the base class for plug-in aggregation functions
     * @param functionName is the aggregation function name
     */
    public ExprPlugInAggFunctionNode(boolean distinct, AggregationSupport aggregationSupport, String functionName)
    {
        super(distinct);
        this.aggregationSupport = aggregationSupport;
        aggregationSupport.setFunctionName(functionName);
    }

    public AggregationMethodFactory validateAggregationChild(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        Class[] parameterTypes = new Class[this.getChildNodes().size()];
        Object[] constant = new Object[this.getChildNodes().size()];
        boolean[] isConstant = new boolean[this.getChildNodes().size()];
        ExprNode[] expressions = new ExprNode[this.getChildNodes().size()];

        int count = 0;
        boolean hasDataWindows = true;
        for (ExprNode child : this.getChildNodes())
        {
            if (child.isConstantResult())
            {
                isConstant[count] = true;
                constant[count] = child.getExprEvaluator().evaluate(null, true, exprEvaluatorContext);
            }
            parameterTypes[count] = child.getExprEvaluator().getType();
            expressions[count] = child;
            
            count++;

            if (!ExprNodeUtility.hasRemoveStream(child, streamTypeService)) {
                hasDataWindows = false;
            }
        }

        AggregationValidationContext context = new AggregationValidationContext(parameterTypes, isConstant, constant, super.isDistinct(), hasDataWindows, expressions);
        try
        {
            aggregationSupport.validate(context);
        }
        catch (RuntimeException ex)
        {
            throw new ExprValidationException("Plug-in aggregation function '" + aggregationSupport.getFunctionName() + "' failed validation: " + ex.getMessage());
        }

        Class childType = null;
        if (this.getChildNodes().size() > 0)
        {
            childType = this.getChildNodes().get(0).getExprEvaluator().getType();
        }

        return new ExprPlugInAggFunctionNodeFactory(aggregationSupport, super.isDistinct(), childType);
    }

    public String getAggregationFunctionName()
    {
        return aggregationSupport.getFunctionName();
    }

    public final boolean equalsNodeAggregate(ExprAggregateNode node)
    {
        if (!(node instanceof ExprPlugInAggFunctionNode))
        {
            return false;
        }

        ExprPlugInAggFunctionNode other = (ExprPlugInAggFunctionNode) node;
        return other.getAggregationFunctionName().equals(this.getAggregationFunctionName());
    }
}

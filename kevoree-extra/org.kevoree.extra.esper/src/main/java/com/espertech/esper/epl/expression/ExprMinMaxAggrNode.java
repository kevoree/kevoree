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
import com.espertech.esper.type.MinMaxTypeEnum;

/**
 * Represents the min/max(distinct? ...) aggregate function is an expression tree.
 */
public class ExprMinMaxAggrNode extends ExprAggregateNode
{
    private final MinMaxTypeEnum minMaxTypeEnum;
    private static final long serialVersionUID = -7828413362615586145L;

    /**
     * Ctor.
     * @param distinct - indicator whether distinct values of all values min/max
     * @param minMaxTypeEnum - enum for whether to minimum or maximum compute
     */
    public ExprMinMaxAggrNode(boolean distinct, MinMaxTypeEnum minMaxTypeEnum)
    {
        super(distinct);
        this.minMaxTypeEnum = minMaxTypeEnum;
    }

    public AggregationMethodFactory validateAggregationChild(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 1)
        {
            throw new ExprValidationException(minMaxTypeEnum.toString() + " node must have exactly 1 child node");
        }
        ExprNode child = this.getChildNodes().get(0);

        boolean hasDataWindows = ExprNodeUtility.hasRemoveStream(child, streamTypeService);

        return new ExprMinMaxAggrNodeFactory(minMaxTypeEnum, child.getExprEvaluator().getType(), hasDataWindows, super.isDistinct());
    }

    public final boolean equalsNodeAggregate(ExprAggregateNode node)
    {
        if (!(node instanceof ExprMinMaxAggrNode))
        {
            return false;
        }

        ExprMinMaxAggrNode other = (ExprMinMaxAggrNode) node;
        return other.minMaxTypeEnum == this.minMaxTypeEnum;
    }

    /**
     * Returns the indicator for minimum or maximum.
     * @return min/max indicator
     */
    public MinMaxTypeEnum getMinMaxTypeEnum()
    {
        return minMaxTypeEnum;
    }

    protected String getAggregationFunctionName()
    {
        return minMaxTypeEnum.getExpressionText();
    }
}

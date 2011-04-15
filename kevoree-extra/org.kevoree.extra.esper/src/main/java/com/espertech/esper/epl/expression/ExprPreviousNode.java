/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewCapDataWindowAccess;
import com.espertech.esper.view.window.RandomAccessByIndexGetter;
import com.espertech.esper.view.window.RelativeAccessByEventNIndexMap;

import java.util.Map;

/**
 * Represents the 'prev' previous event function in an expression node tree.
 */
public class ExprPreviousNode extends ExprNode implements ViewResourceCallback, ExprEvaluator
{
    private static final long serialVersionUID = 0L;

    private final PreviousType previousType;

    private Class resultType;
    private int streamNumber;
    private Integer constantIndexNumber;
    private boolean isConstantIndex;

    private transient ExprPreviousEval evaluator;

    public ExprPreviousNode(PreviousType previousType)
    {
        this.previousType = previousType;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if ((this.getChildNodes().size() > 2) || (this.getChildNodes().isEmpty()))
        {
            throw new ExprValidationException("Previous node must have 1 or 2 child nodes");
        }

        // add constant of 1 for previous index
        if (this.getChildNodes().size() == 1)
        {
            if (previousType == PreviousType.PREV) {
                this.getChildNodes().add(0, new ExprConstantNode(1));
            }
            else {
                this.getChildNodes().add(0, new ExprConstantNode(0));
            }
        }

        // the row recognition patterns allows "prev(prop, index)", we switch index the first position
        if (this.getChildNodes().get(1) instanceof ExprConstantNode)
        {
            ExprNode first = this.getChildNodes().get(0);
            ExprNode second = this.getChildNodes().get(1);
            this.getChildNodes().clear();
            this.getChildNodes().add(second);
            this.getChildNodes().add(first);
        }
        
        // Determine if the index is a constant value or an expression to evaluate
        if (this.getChildNodes().get(0).isConstantResult())
        {
            ExprNode constantNode = this.getChildNodes().get(0);
            Object value = constantNode.getExprEvaluator().evaluate(null, false, exprEvaluatorContext);
            if (!(value instanceof Number))
            {
                throw new ExprValidationException("Previous function requires an integer index parameter or expression");
            }

            Number valueNumber = (Number) value;
            if (JavaClassHelper.isFloatingPointNumber(valueNumber))
            {
                throw new ExprValidationException("Previous function requires an integer index parameter or expression");
            }

            constantIndexNumber = valueNumber.intValue();
            isConstantIndex = true;
        }

        // Determine stream number
        if (this.getChildNodes().get(1) instanceof ExprIdentNode) {
            ExprIdentNode identNode = (ExprIdentNode) this.getChildNodes().get(1);
            streamNumber = identNode.getStreamId();
            resultType = JavaClassHelper.getBoxedType(this.getChildNodes().get(1).getExprEvaluator().getType());
        }
        else if (this.getChildNodes().get(1) instanceof ExprStreamUnderlyingNode) {
            ExprStreamUnderlyingNode streamNode = (ExprStreamUnderlyingNode) this.getChildNodes().get(1);
            streamNumber = streamNode.getStreamId();
            resultType = JavaClassHelper.getBoxedType(this.getChildNodes().get(1).getExprEvaluator().getType());
        }
        else
        {
            throw new ExprValidationException("Previous function requires an event property as parameter");
        }

        if (previousType == PreviousType.PREVCOUNT) {
            resultType = Long.class;
        }
        if (previousType == PreviousType.PREVWINDOW) {
            resultType = JavaClassHelper.getArrayType(resultType);
        }

        if (viewResourceDelegate == null)
        {
            throw new ExprValidationException("Previous function cannot be used in this context");
        }

        // Request a callback that provides the required access
        if (!viewResourceDelegate.requestCapability(streamNumber, new ViewCapDataWindowAccess(), this))
        {
            throw new ExprValidationException("Previous function requires a single data window view onto the stream");
        }
    }

    public PreviousType getPreviousType()
    {
        return previousType;
    }

    public Class getType()
    {
        return resultType;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (!isNewData) {
            return null;
        }

        return evaluator.evaluate(eventsPerStream, exprEvaluatorContext);
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(previousType.toString().toLowerCase());
        buffer.append("(");
        if ((previousType == PreviousType.PREVCOUNT || previousType == PreviousType.PREVWINDOW)) {
            buffer.append(this.getChildNodes().get(1).toExpressionString());
        }
        else {
            buffer.append(this.getChildNodes().get(0).toExpressionString());
            buffer.append(", ");
            buffer.append(this.getChildNodes().get(1).toExpressionString());
        }
        buffer.append(')');
        return buffer.toString();
    }

    @Override
    public int hashCode()
    {
        return previousType != null ? previousType.hashCode() : 0;
    }

    public boolean equalsNode(ExprNode node)
    {
        if (node == null || getClass() != node.getClass())
        {
            return false;
        }

        ExprPreviousNode that = (ExprPreviousNode) node;

        if (previousType != that.previousType)
        {
            return false;
        }

        return true;
    }

    public void setViewResource(Object resource)
    {
        RandomAccessByIndexGetter randomAccessGetter = null;
        RelativeAccessByEventNIndexMap relativeAccessGetter = null;

        if (resource instanceof RandomAccessByIndexGetter)
        {
            randomAccessGetter = (RandomAccessByIndexGetter) resource;
        }
        else if (resource instanceof RelativeAccessByEventNIndexMap)
        {
            relativeAccessGetter = (RelativeAccessByEventNIndexMap) resource;
        }
        else
        {
            throw new IllegalArgumentException("View resource " + resource.getClass() + " not recognized by expression node");
        }

        if (previousType == PreviousType.PREVWINDOW) {
            evaluator = new ExprPreviousEvalWindow(streamNumber, this.getChildNodes().get(1).getExprEvaluator(), resultType.getComponentType(),
                    randomAccessGetter, relativeAccessGetter);
        }
        else if (previousType == PreviousType.PREVCOUNT) {
            evaluator = new ExprPreviousEvalCount(streamNumber, randomAccessGetter, relativeAccessGetter);
        }
        else {
            evaluator = new ExprPreviousEvalPrev(streamNumber, this.getChildNodes().get(0).getExprEvaluator(), this.getChildNodes().get(1).getExprEvaluator(),
                    randomAccessGetter, relativeAccessGetter, isConstantIndex, constantIndexNumber, previousType == PreviousType.PREVTAIL);
        }
    }
}

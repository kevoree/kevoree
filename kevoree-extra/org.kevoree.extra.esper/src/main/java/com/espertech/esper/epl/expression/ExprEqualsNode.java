/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.SimpleNumberCoercer;
import com.espertech.esper.util.SimpleNumberCoercerFactory;
import com.espertech.esper.util.CoercionException;

import java.util.Map;

/**
 * Represents an equals (=) comparator in a filter expressiun tree.
 */
public class ExprEqualsNode extends ExprNode implements ExprEvaluator
{
    private final boolean isNotEquals;

    private boolean mustCoerce;

    private transient SimpleNumberCoercer numberCoercerLHS;
    private transient SimpleNumberCoercer numberCoercerRHS;
    private transient ExprEvaluator[] evaluators;

    private static final long serialVersionUID = 5504809379222369952L;

    /**
     * Ctor.
     * @param isNotEquals - true if this is a (!=) not equals rather then equals, false if its a '=' equals
     */
    public ExprEqualsNode(boolean isNotEquals)
    {
        this.isNotEquals = isNotEquals;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    /**
     * Returns true if this is a NOT EQUALS node, false if this is a EQUALS node.
     * @return true for !=, false for =
     */
    public boolean isNotEquals()
    {
        return isNotEquals;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        // Must have 2 child nodes
        if (this.getChildNodes().size() != 2)
        {
            throw new IllegalStateException("Equals node does not have exactly 2 child nodes");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        // Must be the same boxed type returned by expressions under this
        Class typeOne = JavaClassHelper.getBoxedType(evaluators[0].getType());
        Class typeTwo = JavaClassHelper.getBoxedType(evaluators[1].getType());

        // Null constants can be compared for any type
        if ((typeOne == null) || (typeTwo == null))
        {
            return;
        }

        if (typeOne.equals(typeTwo) || typeOne.isAssignableFrom(typeTwo))
        {
            mustCoerce = false;
            return;
        }

        // Get the common type such as Bool, String or Double and Long
        Class coercionType;
        try
        {
            coercionType = JavaClassHelper.getCompareToCoercionType(typeOne, typeTwo);
        }
        catch (CoercionException ex)
        {
            throw new ExprValidationException("Implicit conversion from datatype '" +
                    typeTwo.getSimpleName() +
                    "' to '" +
                    typeOne.getSimpleName() +
                    "' is not allowed");
        }

        // Check if we need to coerce
        if ((coercionType == JavaClassHelper.getBoxedType(typeOne)) &&
            (coercionType == JavaClassHelper.getBoxedType(typeTwo)))
        {
            mustCoerce = false;
        }
        else
        {
            if (!JavaClassHelper.isNumeric(coercionType))
            {
                throw new ExprValidationException("Cannot convert datatype '" + coercionType.getName() + "' to a numeric value");
            }
            mustCoerce = true;
            numberCoercerLHS = SimpleNumberCoercerFactory.getCoercer(typeOne, coercionType);
            numberCoercerRHS = SimpleNumberCoercerFactory.getCoercer(typeTwo, coercionType);
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object leftResult = evaluators[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        Object rightResult = evaluators[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

        if (leftResult == null)
        {
            return (rightResult == null) ^ isNotEquals;
        }
        if (rightResult == null)
        {
            return isNotEquals;
        }

        if (!mustCoerce)
        {
            return leftResult.equals(rightResult) ^ isNotEquals;
        }
        else
        {
            Number left = numberCoercerLHS.coerceBoxed((Number) leftResult);
            Number right = numberCoercerRHS.coerceBoxed((Number) rightResult);
            return left.equals(right) ^ isNotEquals;
        }
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(" = ");
        buffer.append(this.getChildNodes().get(1).toExpressionString());

        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprEqualsNode))
        {
            return false;
        }

        ExprEqualsNode other = (ExprEqualsNode) node;
        return other.isNotEquals == this.isNotEquals;
    }
}

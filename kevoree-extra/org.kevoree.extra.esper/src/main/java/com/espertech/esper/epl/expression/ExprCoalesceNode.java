/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.CoercionException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Map;

/**
 * Represents the COALESCE(a,b,...) function is an expression tree.
 */
public class ExprCoalesceNode extends ExprNode implements ExprEvaluator
{
    private Class resultType;
    private boolean[] isNumericCoercion;

    private transient ExprEvaluator[] evaluators;

    private static final long serialVersionUID = -8276568753875819730L;

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() < 2)
        {
            throw new ExprValidationException("Coalesce node must have at least 2 child nodes");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        // get child expression types
        Class[] childTypes = new Class[getChildNodes().size()];
        for (int i = 0; i < evaluators.length; i++)
        {
            childTypes[i] = evaluators[i].getType();
        }

        // determine coercion type
        try {
            resultType = JavaClassHelper.getCommonCoercionType(childTypes);
        }
        catch (CoercionException ex)
        {
            throw new ExprValidationException("Implicit conversion not allowed: " + ex.getMessage());
        }

        // determine which child nodes need numeric coercion
        isNumericCoercion = new boolean[getChildNodes().size()];
        for (int i = 0; i < evaluators.length; i++)
        {
            if ((JavaClassHelper.getBoxedType(evaluators[i].getType()) != resultType) &&
                (evaluators[i].getType() != null) && (resultType != null))
            {
                if (!JavaClassHelper.isNumeric(resultType))
                {
                    throw new ExprValidationException("Implicit conversion from datatype '" +
                            resultType.getSimpleName() +
                            "' to " + evaluators[i].getType() + " is not allowed");
                }
                isNumericCoercion[i] = true;
            }
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }    

    public Class getType()
    {
        return resultType;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object value;

        // Look for the first non-null return value
        for (int i = 0; i < evaluators.length; i++)
        {
            value = evaluators[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            if (value != null)
            {
                // Check if we need to coerce
                if (isNumericCoercion[i])
                {
                    return JavaClassHelper.coerceBoxed((Number)value, resultType);
                }
                return value;
            }
        }

        return null;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("coalesce(");

        String delimiter = "";
        for (int i = 0; i < this.getChildNodes().size(); i++)
        {
            buffer.append(delimiter);
            buffer.append(this.getChildNodes().get(i).toExpressionString());
            delimiter = ",";
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprCoalesceNode))
        {
            return false;
        }

        return true;
    }
}

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
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Map;

/**
 * Represents a simple Math (+/-/divide/*) in a filter expression tree.
 */
public class ExprConcatNode extends ExprNode implements ExprEvaluator
{
    private StringBuffer buffer;
    private transient ExprEvaluator[] evaluators;
    private static final long serialVersionUID = 5811427566733004327L;

    /**
     * Ctor.
     */
    public ExprConcatNode()
    {
        buffer = new StringBuffer();
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
        if (this.getChildNodes().size() < 2)
        {
            throw new ExprValidationException("Concat node must have at least 2 child nodes");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        for (int i = 0; i < evaluators.length; i++)
        {
            Class childType = evaluators[i].getType();
            String childTypeName = childType == null ? "null" : childType.getSimpleName();
            if (childType != String.class)
            {
                throw new ExprValidationException("Implicit conversion from datatype '" +
                        childTypeName +
                        "' to string is not allowed");
            }
        }
    }

    public Class getType()
    {
        return String.class;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        buffer.delete(0, buffer.length());
        for (ExprEvaluator child : evaluators)
        {
            String result = (String) child.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if (result == null)
            {
                return null;
            }
            buffer.append(result);
        }
        return buffer.toString();
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        String delimiter = "(";
        for (ExprNode child : this.getChildNodes())
        {
            buffer.append(delimiter);
            buffer.append(child.toExpressionString());
            delimiter = "||";
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprConcatNode))
        {
            return false;
        }

        return true;
    }
}

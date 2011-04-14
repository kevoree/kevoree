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
import com.espertech.esper.util.JavaClassHelper;

import java.util.Map;

/**
 * Represents a NOT expression in an expression tree.
 */
public class ExprNotNode extends ExprNode implements ExprEvaluator
{
    private transient ExprEvaluator evaluator;
    private static final long serialVersionUID = -5958420226808323787L;

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        // Must have a single child node
        if (this.getChildNodes().size() != 1)
        {
            throw new ExprValidationException("The NOT node requires exactly 1 child node");
        }

        evaluator = this.getChildNodes().get(0).getExprEvaluator();
        Class childType = evaluator.getType();
        if (!JavaClassHelper.isBoolean(childType))
        {
            throw new ExprValidationException("Incorrect use of NOT clause, sub-expressions do not return boolean");
        }
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Boolean evaluated = (Boolean) evaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (evaluated == null)
        {
            return null;
        }
        return !evaluated;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("NOT(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprNotNode))
        {
            return false;
        }

        return true;
    }
}

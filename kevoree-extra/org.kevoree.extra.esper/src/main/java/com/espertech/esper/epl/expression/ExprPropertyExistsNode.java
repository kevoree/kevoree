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
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Map;

/**
 * Represents the EXISTS(property) function in an expression tree.
 */
public class ExprPropertyExistsNode extends ExprNode implements ExprEvaluator
{
    private ExprIdentNode identNode;
    private static final long serialVersionUID = -6304444201237275628L;

    /**
     * Ctor.
     */
    public ExprPropertyExistsNode()
    {
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 1)
        {
            throw new ExprValidationException("Exists function node must have exactly 1 child node");
        }

        if (!(this.getChildNodes().get(0) instanceof ExprIdentNode))
        {
            throw new ExprValidationException("Exists function expects an property value expression as the child node");
        }

        identNode = (ExprIdentNode) this.getChildNodes().get(0);
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return identNode.evaluatePropertyExists(eventsPerStream, isNewData);
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("exists(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprPropertyExistsNode))
        {
            return false;
        }

        return true;
    }
}

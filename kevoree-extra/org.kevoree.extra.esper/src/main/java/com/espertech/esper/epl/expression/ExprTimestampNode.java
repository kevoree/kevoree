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

import java.util.Map;

/**
 * Represents the CURRENT_TIMESTAMP() function or reserved keyword in an expression tree.
 */
public class ExprTimestampNode extends ExprNode implements ExprEvaluator
{
    private static final long serialVersionUID = -6332243334897136751L;

    /**
     * Ctor.
     */
    public ExprTimestampNode()
    {
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public void validate(StreamTypeService streamTypeService,
                         MethodResolutionService methodResolutionService,
                         ViewResourceDelegate viewResourceDelegate,
                         TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 0)
        {
            throw new ExprValidationException("current_timestamp function node must have exactly 1 child node");
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return Long.class;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return exprEvaluatorContext.getTimeProvider().getTime();
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("current_timestamp()");
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprTimestampNode))
        {
            return false;
        }
        return true;
    }
}

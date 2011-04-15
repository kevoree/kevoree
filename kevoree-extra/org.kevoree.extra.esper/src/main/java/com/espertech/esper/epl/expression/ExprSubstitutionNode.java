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
import com.espertech.esper.client.EPException;
import com.espertech.esper.schedule.TimeProvider;

/**
 * Represents a substitution value to be substituted in an expression tree, not valid for any purpose of use
 * as an expression, however can take a place in an expression tree.
 */
public class ExprSubstitutionNode extends ExprNode
{
    private static final String ERROR_MSG = "Invalid use of substitution parameters marked by '?' in statement, use the prepare method to prepare statements with substitution parameters";
    private int index;
    private static final long serialVersionUID = -4238446583735045135L;

    /**
     * Ctor.
     * @param index is the index of the substitution parameter
     */
    public ExprSubstitutionNode(int index)
    {
        this.index = index;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        throw new ExprValidationException(ERROR_MSG);
    }

    /**
     * Returns the substitution parameter index.
     * @return index
     */
    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        throw new IllegalStateException(ERROR_MSG);
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        throw new EPException(ERROR_MSG);
    }

    public ExprEvaluator getExprEvaluator()
    {
        throw new EPException(ERROR_MSG);
    }

    public String toExpressionString()
    {
        throw new EPException(ERROR_MSG);
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprSubstitutionNode))
        {
            return false;
        }

        return true;
    }
}

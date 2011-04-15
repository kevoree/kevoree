/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern.guard;

import com.espertech.esper.pattern.*;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.MetaDefItem;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.client.EPException;

import java.util.List;
import java.io.Serializable;

/**
 * Factory for {@link TimerWithinGuard} instances.
 */
public class TimerWithinGuardFactory implements GuardFactory, MetaDefItem, Serializable
{
    private static final long serialVersionUID = -1026320055174163611L;

    /**
     * Number of milliseconds.
     */
    protected ExprNode millisecondsExpr;

    /**
     * For converting matched-events maps to events-per-stream.
     */
    protected transient MatchedEventConvertor convertor;

    public void setGuardParameters(List<ExprNode> params, MatchedEventConvertor convertor) throws GuardParameterException
    {
        String errorMessage = "Timer-within guard requires a single numeric or time period parameter";
        if (params.size() != 1)
        {
            throw new GuardParameterException(errorMessage);
        }

        if (!JavaClassHelper.isNumeric(params.get(0).getExprEvaluator().getType()))
        {
            throw new GuardParameterException(errorMessage);
        }

        this.convertor = convertor;
        this.millisecondsExpr = params.get(0);
    }

    public Guard makeGuard(PatternContext context, MatchedEventMap matchedEventMap, Quitable quitable, EvalStateNodeNumber stateNodeId, Object guardState)
    {
        Object millisecondVal = PatternExpressionUtil.evaluate("Timer-within guard", matchedEventMap, millisecondsExpr, convertor, quitable.getContext());

        if (millisecondVal == null)
        {
            throw new EPException("Timer-within guard expression returned a null-value");
        }

        Number param = (Number) millisecondVal;
        long milliseconds = Math.round(1000d * param.doubleValue());
        return new TimerWithinGuard(milliseconds, quitable);
    }
}

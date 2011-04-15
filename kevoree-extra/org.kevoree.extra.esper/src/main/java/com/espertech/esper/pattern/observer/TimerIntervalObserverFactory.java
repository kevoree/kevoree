/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern.observer;

import com.espertech.esper.client.EPException;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.pattern.EvalStateNodeNumber;
import com.espertech.esper.pattern.MatchedEventConvertor;
import com.espertech.esper.pattern.MatchedEventMap;
import com.espertech.esper.pattern.PatternContext;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;
import java.util.List;

/**
 * Factory for making observer instances.
 */
public class TimerIntervalObserverFactory implements ObserverFactory, MetaDefItem, Serializable
{
    private static final long serialVersionUID = -2808651894497586884L;

    /**
     * Parameters.
     */
    protected ExprNode parameter;

    /**
     * Convertor to events-per-stream.
     */
    protected transient MatchedEventConvertor convertor;

    /**
     * Number of milliseconds after which the interval should fire.
     */
    protected long milliseconds;

    public void setObserverParameters(List<ExprNode> params, MatchedEventConvertor convertor) throws ObserverParameterException
    {
        String errorMessage = "Timer-interval observer requires a single numeric or time period parameter";
        if (params.size() != 1)
        {
            throw new ObserverParameterException(errorMessage);
        }

        Class returnType = params.get(0).getExprEvaluator().getType();
        if (!(JavaClassHelper.isNumeric(returnType)))
        {
            throw new ObserverParameterException(errorMessage);
        }
        
        parameter = params.get(0);
        this.convertor = convertor;
    }

    public EventObserver makeObserver(PatternContext context, MatchedEventMap beginState, ObserverEventEvaluator observerEventEvaluator, EvalStateNodeNumber stateNodeId, Object observerState)
    {
        Object result = parameter.getExprEvaluator().evaluate(convertor.convert(beginState), true, context);
        if (result == null)
        {
            throw new EPException("Null value returned for guard expression");
        }

        Number param = (Number) result;
        if (JavaClassHelper.isFloatingPointNumber(param))
        {
            milliseconds = Math.round(1000d * param.doubleValue());
        }
        else
        {
            milliseconds = 1000 * param.longValue();
        }
        return new TimerIntervalObserver(milliseconds, beginState, observerEventEvaluator);
    }
}

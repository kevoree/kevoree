/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Index that simply maintains a list of boolean expressions.
 */
public final class FilterParamIndexBooleanExpr extends FilterParamIndexBase
{
    private final Map<ExprNodeAdapter, EventEvaluator> evaluatorsMap;
    private final ReadWriteLock constantsMapRWLock;

    /**
     * Constructs the index for multiple-exact matches.
     * @param eventType describes the event type and is used to obtain a getter instance for the property
     */
    public FilterParamIndexBooleanExpr(EventType eventType)
    {
        super(FilterOperator.BOOLEAN_EXPRESSION);

        evaluatorsMap = new HashMap<ExprNodeAdapter, EventEvaluator>();
        constantsMapRWLock = new ReentrantReadWriteLock();
    }

    public final EventEvaluator get(Object filterConstant)
    {
        ExprNodeAdapter keyValues = (ExprNodeAdapter) filterConstant;
        return evaluatorsMap.get(keyValues);
    }

    public final void put(Object filterConstant, EventEvaluator evaluator)
    {
        ExprNodeAdapter keys = (ExprNodeAdapter) filterConstant;
        evaluatorsMap.put(keys, evaluator);
    }

    public final boolean remove(Object filterConstant)
    {
        ExprNodeAdapter keys = (ExprNodeAdapter) filterConstant;
        return evaluatorsMap.remove(keys) != null;
    }

    public final int size()
    {
        return evaluatorsMap.size();
    }

    public final ReadWriteLock getReadWriteLock()
    {
        return constantsMapRWLock;
    }

    public final void matchEvent(EventBean eventBean, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".match (" + Thread.currentThread().getId() + ")");
        }

        List<EventEvaluator> evaluators = new ArrayList<EventEvaluator>();
        constantsMapRWLock.readLock().lock();
        for (ExprNodeAdapter exprNodeAdapter : evaluatorsMap.keySet())
        {
            if (exprNodeAdapter.evaluate(eventBean, exprEvaluatorContext))
            {
                evaluators.add(evaluatorsMap.get(exprNodeAdapter));
            }
        }
        constantsMapRWLock.readLock().unlock();

        for (EventEvaluator evaluator : evaluators)
        {
            evaluator.matchEvent(eventBean, matches, exprEvaluatorContext);
        }
    }

    private static final Log log = LogFactory.getLog(FilterParamIndexBooleanExpr.class);
}

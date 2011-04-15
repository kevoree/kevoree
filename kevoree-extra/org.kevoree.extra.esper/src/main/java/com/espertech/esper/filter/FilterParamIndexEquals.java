/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.*;
import java.util.concurrent.locks.*;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * Index for filter parameter constants to match using the equals (=) operator.
 * The implementation is based on a regular HashMap.
 */
public final class FilterParamIndexEquals extends FilterParamIndexPropBase
{
    private final Map<Object, EventEvaluator> constantsMap;
    private final ReadWriteLock constantsMapRWLock;

    /**
     * Constructs the index for exact matches.
     * @param propertyName is the name of the event property
     * @param eventType describes the event type and is used to obtain a getter instance for the property
     */
    public FilterParamIndexEquals(String propertyName, EventType eventType)
    {
        super(propertyName, FilterOperator.EQUAL, eventType);

        constantsMap = new HashMap<Object, EventEvaluator>();
        constantsMapRWLock = new ReentrantReadWriteLock();
    }

    public final EventEvaluator get(Object filterConstant)
    {
        checkType(filterConstant);
        return constantsMap.get(filterConstant);
    }

    public final void put(Object filterConstant, EventEvaluator evaluator)
    {
        checkType(filterConstant);
        constantsMap.put(filterConstant, evaluator);
    }

    public final boolean remove(Object filterConstant)
    {
        if (constantsMap.remove(filterConstant) == null)
        {
            return false;
        }
        return true;
    }

    public final int size()
    {
        return constantsMap.size();
    }

    public final ReadWriteLock getReadWriteLock()
    {
        return constantsMapRWLock;
    }

    public final void matchEvent(EventBean eventBean, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object attributeValue = this.getGetter().get(eventBean);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".match (" + Thread.currentThread().getId() + ") attributeValue=" + attributeValue);
        }

        // Look up in hashtable
        EventEvaluator evaluator = null;
        constantsMapRWLock.readLock().lock();
        try
        {
            evaluator = constantsMap.get(attributeValue);
        }
        finally
        {
            constantsMapRWLock.readLock().unlock();
        }

        // No listener found for the value, return
        if (evaluator == null)
        {
            return;
        }

        evaluator.matchEvent(eventBean, matches, exprEvaluatorContext);
    }

    private void checkType(Object filterConstant)
    {
        if (filterConstant != null)
        {
            if ((this.getPropertyBoxedType() != filterConstant.getClass()) && (!this.getPropertyBoxedType().isAssignableFrom(filterConstant.getClass())))
            {
                throw new IllegalArgumentException("Invalid type of filter constant of " +
                        filterConstant.getClass().getName() + " for property " + this.getPropertyName());
            }
        }
    }

    private static final Log log = LogFactory.getLog(FilterParamIndexEquals.class);
}

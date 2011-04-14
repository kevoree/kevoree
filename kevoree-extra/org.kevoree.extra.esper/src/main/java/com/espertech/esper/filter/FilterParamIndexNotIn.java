/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.collection.MultiKeyUntyped;
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
 * Index for filter parameter constants to match using the 'not in' operator to match against a
 * all other values then the supplied set of values.
 */
public final class FilterParamIndexNotIn extends FilterParamIndexPropBase
{
    private final Map<Object, Set<EventEvaluator>> constantsMap;
    private final Map<MultiKeyUntyped, EventEvaluator> filterValueEvaluators;
    private final Set<EventEvaluator> evaluatorsSet;
    private final ReadWriteLock constantsMapRWLock;

    /**
     * Constructs the index for multiple-exact matches.
     * @param propertyName is the name of the event property
     * @param eventType describes the event type and is used to obtain a getter instance for the property
     */
    public FilterParamIndexNotIn(String propertyName, EventType eventType)
    {
        super(propertyName, FilterOperator.NOT_IN_LIST_OF_VALUES, eventType);

        constantsMap = new HashMap<Object, Set<EventEvaluator>>();
        filterValueEvaluators = new HashMap<MultiKeyUntyped, EventEvaluator>();
        evaluatorsSet = new HashSet<EventEvaluator>();
        constantsMapRWLock = new ReentrantReadWriteLock();
    }

    public final EventEvaluator get(Object filterConstant)
    {
        MultiKeyUntyped keyValues = (MultiKeyUntyped) filterConstant;
        return filterValueEvaluators.get(keyValues);
    }

    public final void put(Object filterConstant, EventEvaluator evaluator)
    {
        // Store evaluator keyed to set of values
        MultiKeyUntyped keys = (MultiKeyUntyped) filterConstant;
        filterValueEvaluators.put(keys, evaluator);
        evaluatorsSet.add(evaluator);

        // Store each value to match against in Map with it's evaluator as a list
        Object[] keyValues = keys.getKeys();
        for (Object keyValue : keyValues)
        {
            Set<EventEvaluator> evaluators = constantsMap.get(keyValue);
            if (evaluators == null)
            {
                evaluators = new HashSet<EventEvaluator>();
                constantsMap.put(keyValue, evaluators);
            }
            evaluators.add(evaluator);
        }
    }

    public final boolean remove(Object filterConstant)
    {
        MultiKeyUntyped keys = (MultiKeyUntyped) filterConstant;

        // remove the mapping of value set to evaluator
        EventEvaluator eval = filterValueEvaluators.remove(keys);
        evaluatorsSet.remove(eval);
        boolean isRemoved = false;
        if (eval != null)
        {
            isRemoved = true;
        }

        Object[] keyValues = keys.getKeys();
        for (Object keyValue : keyValues)
        {
            Set<EventEvaluator> evaluators = constantsMap.get(keyValue);
            if (evaluators != null) // could already be removed as constants may be the same
            {
                evaluators.remove(eval);
                if (evaluators.isEmpty())
                {
                    constantsMap.remove(keyValue);
                }
            }
        }
        return isRemoved;
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

        if (attributeValue == null)
        {
            return;
        }

        // Look up in hashtable the set of not-in evaluators
        constantsMapRWLock.readLock().lock();
        Set<EventEvaluator> evalNotMatching = constantsMap.get(attributeValue);

        // if all known evaluators are matching, invoke all
        if (evalNotMatching == null)
        {
            for (EventEvaluator eval : evaluatorsSet)
            {
                eval.matchEvent(eventBean, matches, exprEvaluatorContext);
            }
            constantsMapRWLock.readLock().unlock();
            return;
        }

        // if none are matching, we are done
        if (evalNotMatching.size() == evaluatorsSet.size())
        {
            constantsMapRWLock.readLock().unlock();
            return;
        }

        // handle partial matches: loop through all evaluators and see which one should not be matching, match all else
        for (EventEvaluator eval : evaluatorsSet)
        {
            if (!(evalNotMatching.contains(eval)))
            {
                eval.matchEvent(eventBean, matches, exprEvaluatorContext);
            }
        }

        constantsMapRWLock.readLock().unlock();
    }

    private static final Log log = LogFactory.getLog(FilterParamIndexNotIn.class);
}

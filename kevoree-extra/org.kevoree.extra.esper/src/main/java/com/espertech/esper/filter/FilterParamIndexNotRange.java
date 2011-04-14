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
 * Index for filter parameter constants for the not range operators (range open/closed/half).
 * The implementation is based on the SortedMap implementation of TreeMap and stores only expression
 * parameter values of type DoubleRange.
 */
public final class FilterParamIndexNotRange extends FilterParamIndexPropBase
{
    private final TreeMap<DoubleRange, EventEvaluator> ranges;
    private final Set<EventEvaluator> evaluators;
    private final ReadWriteLock rangesRWLock;

    private double largestRangeValueDouble = Double.MIN_VALUE;

   /**
     * Constructs the index for matching ranges.
     * @param attributeName is the name of the event attribute field
    * @param filterOperator is the type of range
    * @param eventType is type of events handled
    */
    public FilterParamIndexNotRange(String attributeName, FilterOperator filterOperator, EventType eventType)
    {
        super(attributeName, filterOperator, eventType);

        ranges = new TreeMap<DoubleRange, EventEvaluator>(new DoubleRangeComparator());
        evaluators = new HashSet<EventEvaluator>();
        rangesRWLock = new ReentrantReadWriteLock();

        if (!(filterOperator.isInvertedRangeOperator()))
        {
            throw new IllegalArgumentException("Invalid filter operator " + filterOperator);
        }
    }

    public final EventEvaluator get(Object expressionValue)
    {
        if (!(expressionValue instanceof DoubleRange))
        {
            throw new IllegalArgumentException("Supplied expressionValue must be of type DoubleRange");
        }

        return ranges.get(expressionValue);
    }

    public final void put(Object expressionValue, EventEvaluator matcher)
    {
        if (!(expressionValue instanceof DoubleRange))
        {
            throw new IllegalArgumentException("Supplied expressionValue must be of type DoubleRange");
        }

        DoubleRange range = (DoubleRange) expressionValue;

        if ((range.getMax() == null) || (range.getMin() == null))
        {
            return; // null endpoints are ignored
        }

        if ( Math.abs(range.getMax() - range.getMin()) > largestRangeValueDouble)
        {
            largestRangeValueDouble = Math.abs(range.getMax() - range.getMin());
        }

        ranges.put(range, matcher);
        evaluators.add(matcher);
    }

    public final boolean remove(Object filterConstant)
    {
        EventEvaluator eval = ranges.remove(filterConstant);
        if (eval == null)
        {
            return false;
        }
        evaluators.remove(eval);
        return true;
    }

    public final int size()
    {
        return ranges.size();
    }

    public final ReadWriteLock getReadWriteLock()
    {
        return rangesRWLock;
    }

    public final void matchEvent(EventBean eventBean, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object objAttributeValue = this.getGetter().get(eventBean);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".match Finding range matches, attribute=" + this.getPropertyName() +
                      "  attrValue=" + objAttributeValue);
        }

        if (objAttributeValue == null)
        {
            return;
        }

        double attributeValue = ((Number) objAttributeValue).doubleValue();

        DoubleRange rangeStart = new DoubleRange(attributeValue - largestRangeValueDouble, attributeValue);
        DoubleRange rangeEnd = new DoubleRange(attributeValue, Double.MAX_VALUE);

        SortedMap<DoubleRange, EventEvaluator> subMap = ranges.subMap(rangeStart, rangeEnd);

        // For not including either endpoint
        // A bit awkward to duplicate the loop code, however better than checking the boolean many times over
        // This may be a bit of an early performance optimization - the optimizer after all may do this better
        Set<EventEvaluator> matchingEvals = new HashSet<EventEvaluator>();
        if (this.getFilterOperator() == FilterOperator.NOT_RANGE_OPEN)  // include neither endpoint
        {
            for (Map.Entry<DoubleRange, EventEvaluator> entry : subMap.entrySet())
            {
                if ((attributeValue > entry.getKey().getMin()) &&
                    (attributeValue < entry.getKey().getMax()))
                {
                    matchingEvals.add(entry.getValue());
                }
            }
        }
        else if (this.getFilterOperator() == FilterOperator.NOT_RANGE_CLOSED)   // include all endpoints
        {
            for (Map.Entry<DoubleRange, EventEvaluator> entry : subMap.entrySet())
            {
                if ((attributeValue >= entry.getKey().getMin()) &&
                    (attributeValue <= entry.getKey().getMax()))
                {
                    matchingEvals.add(entry.getValue());
                }
            }
        }
        else if (this.getFilterOperator() == FilterOperator.NOT_RANGE_HALF_CLOSED) // include high endpoint not low endpoint
        {
            for (Map.Entry<DoubleRange, EventEvaluator> entry : subMap.entrySet())
            {
                if ((attributeValue > entry.getKey().getMin()) &&
                    (attributeValue <= entry.getKey().getMax()))
                {
                    matchingEvals.add(entry.getValue());
                }
            }
        }
        else if (this.getFilterOperator() == FilterOperator.NOT_RANGE_HALF_OPEN) // include low endpoint not high endpoint
        {
            for (Map.Entry<DoubleRange, EventEvaluator> entry : subMap.entrySet())
            {
                if ((attributeValue >= entry.getKey().getMin()) &&
                    (attributeValue < entry.getKey().getMax()))
                {
                    matchingEvals.add(entry.getValue());
                }
            }
        }
        else
        {
            throw new IllegalStateException("Invalid filter operator " + this.getFilterOperator());
        }

        // Now we have all the matching evaluators, invoke all that don't match
        for (EventEvaluator eval : evaluators)
        {
            if (!matchingEvals.contains(eval))
            {
                eval.matchEvent(eventBean, matches, exprEvaluatorContext);
            }
        }
    }

    private static final Log log = LogFactory.getLog(FilterParamIndexNotRange.class);
}

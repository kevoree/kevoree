/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.TreeMap;
import java.util.Map;
import java.util.Collection;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Index for filter parameter constants for the comparison operators (less, greater, etc).
 * The implementation is based on the SortedMap implementation of TreeMap.
 * The index only accepts numeric constants. It keeps a lower and upper bounds of all constants in the index
 * for fast range checking, since the assumption is that frequently values fall within a range.
 */
public final class FilterParamIndexCompare extends FilterParamIndexPropBase
{
    private final TreeMap<Object, EventEvaluator> constantsMap;
    private final ReadWriteLock constantsMapRWLock;

    private Double lowerBounds;
    private Double upperBounds;

    /**
     * Constructs the index for matching comparison operators (<, >, <=, >=).
     * @param propertyName is the name of the event attribute field
     * @param filterOperator is the type of relational comparison operator
     * @param eventType describes the event type and is used to obtain a getter instance for the property
     * for fast get value access.
     */
    public FilterParamIndexCompare(String propertyName, FilterOperator filterOperator, EventType eventType)
    {
        super(propertyName, filterOperator, eventType);
        constantsMap = new TreeMap<Object, EventEvaluator>();
        constantsMapRWLock = new ReentrantReadWriteLock();

        if ((filterOperator != FilterOperator.GREATER) &&
            (filterOperator != FilterOperator.GREATER_OR_EQUAL) &&
            (filterOperator != FilterOperator.LESS) &&
            (filterOperator != FilterOperator.LESS_OR_EQUAL))
        {
            throw new IllegalArgumentException("Invalid filter operator for index of " + filterOperator);
        }

        if (!JavaClassHelper.isNumeric(this.getPropertyBoxedType()))
        {
            throw new IllegalArgumentException("Property named '" + propertyName + "' is not numeric");
        }
    }

    public final EventEvaluator get(Object filterConstant)
    {
        checkType(filterConstant);
        return constantsMap.get(filterConstant);
    }

    public final void put(Object filterConstant, EventEvaluator matcher)
    {
        checkType(filterConstant);
        constantsMap.put(filterConstant, matcher);

        // Update bounds
        Double constant = ((Number) filterConstant).doubleValue();
        if ((lowerBounds == null) || (constant < lowerBounds))
        {
            lowerBounds = constant;
        }
        if ((upperBounds == null) || (constant > upperBounds))
        {
            upperBounds = constant;
        }
    }

    public final boolean remove(Object filterConstant)
    {
        if (constantsMap.remove(filterConstant) == null)
        {
            return false;
        }

        updateBounds();

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
        Object propertyValue = this.getGetter().get(eventBean);

        if (propertyValue == null)
        {
            return;
        }

        // A undefine lower bound indicates an empty index
        if (lowerBounds == null)
        {
            return;
        }

        FilterOperator filterOperator = this.getFilterOperator();
        Double propertyValueDouble = ((Number) propertyValue).doubleValue();

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".match (" + Thread.currentThread().getId() + ") propertyValue=" + propertyValue +
                      "  filterOperator=" + filterOperator);
        }

        // Based on current lower and upper bounds check if the property value falls outside - shortcut submap generation
        if ((filterOperator == FilterOperator.GREATER) && (propertyValueDouble <= lowerBounds))
        {
            return;
        }
        else if ((filterOperator == FilterOperator.GREATER_OR_EQUAL) && (propertyValueDouble < lowerBounds))
        {
            return;
        }
        else if ((filterOperator == FilterOperator.LESS) && (propertyValueDouble >= upperBounds))
        {
            return;
        }
        else if ((filterOperator == FilterOperator.LESS_OR_EQUAL) && (propertyValueDouble > upperBounds))
        {
            return;
        }

        // Look up in table
        constantsMapRWLock.readLock().lock();

        // Get the head or tail end of the map depending on comparison type
        Map<Object, EventEvaluator> subMap;

        if ((filterOperator == FilterOperator.GREATER) ||
            (filterOperator == FilterOperator.GREATER_OR_EQUAL))
        {
            // At the head of the map are those with a lower numeric constants
            subMap = constantsMap.headMap(propertyValue);
        }
        else
        {
            subMap = constantsMap.tailMap(propertyValue);
        }

        // All entries in the subMap are elgibile, with an exception
        EventEvaluator exactEquals = null;
        if (filterOperator == FilterOperator.LESS)
        {
            exactEquals = constantsMap.get(propertyValue);
        }

        for (EventEvaluator matcher : subMap.values())
        {
            // For the LESS comparison type we ignore the exactly equal case
            // The subMap is sorted ascending, thus the exactly equals case is the first
            if (exactEquals != null)
            {
                exactEquals = null;
                continue;
            }

            matcher.matchEvent(eventBean, matches, exprEvaluatorContext);
        }

        if (filterOperator == FilterOperator.GREATER_OR_EQUAL)
        {
            EventEvaluator matcher = constantsMap.get(propertyValue);
            if (matcher != null)
            {
                matcher.matchEvent(eventBean, matches, exprEvaluatorContext);
            }
        }

        constantsMapRWLock.readLock().unlock();
    }

    private void updateBounds()
    {
        if (constantsMap.isEmpty())
        {
            lowerBounds = null;
            upperBounds = null;
            return;
        }
        lowerBounds = ((Number) constantsMap.firstKey()).doubleValue();
        upperBounds = ((Number) constantsMap.lastKey()).doubleValue();
    }

    private void checkType(Object filterConstant)
    {
        if (this.getPropertyBoxedType() != filterConstant.getClass())
        {
            throw new IllegalArgumentException("Invalid type of filter constant of " +
                    filterConstant.getClass().getName() + " for property " + this.getPropertyName());
        }
    }

    private static final Log log = LogFactory.getLog(FilterParamIndexCompare.class);
}
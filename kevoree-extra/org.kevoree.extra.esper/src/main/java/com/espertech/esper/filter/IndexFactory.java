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

/**
 * Factory for {@link FilterParamIndexBase} instances based on event property name and filter operator type.
 */
public class IndexFactory
{
    /**
     * Factory for indexes that store filter parameter constants for a given event property and filter
     * operator.
     * <p>Does not perform any check of validity of property name.
     * @param eventType is the event type to create an index for
     * @param propertyName is the event property name
     * @param filterOperator is the type of index to use
     * @return the proper index based on the filter operator type
     */
    public static FilterParamIndexBase createIndex(EventType eventType, String propertyName, FilterOperator filterOperator)
    {
        FilterParamIndexBase index;

        // Handle all EQUAL comparisons
        if (filterOperator == FilterOperator.EQUAL)
        {
            index = new FilterParamIndexEquals(propertyName, eventType);
            return index;
        }

        // Handle all NOT-EQUAL comparisons
        if (filterOperator == FilterOperator.NOT_EQUAL)
        {
            index = new FilterParamIndexNotEquals(propertyName, eventType);
            return index;
        }

        // Handle all GREATER, LESS etc. comparisons
        if ((filterOperator == FilterOperator.GREATER) ||
            (filterOperator == FilterOperator.GREATER_OR_EQUAL) ||
            (filterOperator == FilterOperator.LESS) ||
            (filterOperator == FilterOperator.LESS_OR_EQUAL))
        {
            index = new FilterParamIndexCompare(propertyName, filterOperator, eventType);
            return index;
        }

        // Handle all normal and inverted RANGE comparisons
        if (filterOperator.isRangeOperator())
        {
            index = new FilterParamIndexRange(propertyName, filterOperator, eventType);
            return index;
        }
        if (filterOperator.isInvertedRangeOperator())
        {
            index = new FilterParamIndexNotRange(propertyName, filterOperator, eventType);
            return index;
        }

        // Handle all IN and NOT IN comparisons
        if (filterOperator == FilterOperator.IN_LIST_OF_VALUES)
        {
            index = new FilterParamIndexIn(propertyName, eventType);
            return index;
        }
        if (filterOperator == FilterOperator.NOT_IN_LIST_OF_VALUES)
        {
            index = new FilterParamIndexNotIn(propertyName, eventType);
            return index;
        }

        // Handle all boolean expression
        if (filterOperator == FilterOperator.BOOLEAN_EXPRESSION)
        {
            index = new FilterParamIndexBooleanExpr(eventType);
            return index;
        }
        throw new IllegalArgumentException("Cannot create filter index instance for filter operator " + filterOperator);
    }
}


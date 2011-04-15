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
import com.espertech.esper.pattern.MatchedEventMap;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a 'in' filter parameter in an {@link com.espertech.esper.filter.FilterSpecCompiled} filter specification.
 * <p>
 * The 'in' checks for a list of values.
 */
public final class FilterSpecParamIn extends FilterSpecParam
{
    private final List<FilterSpecParamInValue> listOfValues;
    private MultiKeyUntyped inListConstantsOnly;
    private static final long serialVersionUID = 1723225284589047752L;

    /**
     * Ctor.
     * @param propertyName is the event property name
     * @param filterOperator is expected to be the IN-list operator
     * @param listofValues is a list of constants and event property names
     * @throws IllegalArgumentException for illegal args
     */
    public FilterSpecParamIn(String propertyName,
                             FilterOperator filterOperator,
                             List<FilterSpecParamInValue> listofValues)
        throws IllegalArgumentException
    {
        super(propertyName, filterOperator);
        this.listOfValues = listofValues;

        boolean isAllConstants = false;
        for (FilterSpecParamInValue value : listofValues)
        {
            if (value instanceof InSetOfValuesEventProp)
            {
                isAllConstants = false;
                break;
            }
        }

        if (isAllConstants)
        {
            Object[] constants = new Object[listOfValues.size()];
            int count = 0;
            for (FilterSpecParamInValue valuePlaceholder : listOfValues)
            {
                constants[count++] = valuePlaceholder.getFilterValue(null);
            }
            inListConstantsOnly = new MultiKeyUntyped(constants);
        }

        if ((filterOperator != FilterOperator.IN_LIST_OF_VALUES) && ((filterOperator != FilterOperator.NOT_IN_LIST_OF_VALUES)))
        {
            throw new IllegalArgumentException("Illegal filter operator " + filterOperator + " supplied to " +
                    "in-values filter parameter");
        }
    }

    public final Object getFilterValue(MatchedEventMap matchedEvents)
    {
        // If the list of values consists of all-constants and no event properties, then use cached version
        if (inListConstantsOnly != null)
        {
            return inListConstantsOnly;
        }

        // Determine actual values since the in-list of values contains one or more event properties
        Object[] actualValues = new Object[listOfValues.size()];
        int count = 0;
        for (FilterSpecParamInValue valuePlaceholder : listOfValues)
        {
            actualValues[count++] = valuePlaceholder.getFilterValue(matchedEvents);
        }
        return new MultiKeyUntyped(actualValues);
    }

    /**
     * Returns the list of values we are asking to match.
     * @return list of filter values
     */
    public List<FilterSpecParamInValue> getListOfValues()
    {
        return listOfValues;
    }

    public final String toString()
    {
        return super.toString() + "  in=(listOfValues=" + listOfValues.toString() + ')';
    }

    public int getFilterHash()
    {
        return 0;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof FilterSpecParamIn))
        {
            return false;
        }

        FilterSpecParamIn other = (FilterSpecParamIn) obj;
        if (!super.equals(other))
        {
            return false;
        }

        if (listOfValues.size() != other.listOfValues.size())
        {
            return false;
        }

        if (!(Arrays.deepEquals(listOfValues.toArray(), other.listOfValues.toArray())))
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (listOfValues != null ? listOfValues.hashCode() : 0);
        return result;
    }
}

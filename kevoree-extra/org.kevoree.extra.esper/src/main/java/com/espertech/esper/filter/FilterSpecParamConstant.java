/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.pattern.MatchedEventMap;

/**
 * This class represents a single, constant value filter parameter in an {@link FilterSpecCompiled} filter specification.
 */
public final class FilterSpecParamConstant extends FilterSpecParam
{
    private final Object filterConstant;
    private static final long serialVersionUID = 5732440503234468449L;

    /**
     * Constructor.
     * @param propertyName is the event property name
     * @param filterOperator is the type of compare
     * @param filterConstant contains the value to match against the event's property value
     * @throws IllegalArgumentException if an operator was supplied that does not take a single constant value
     */
    public FilterSpecParamConstant(String propertyName, FilterOperator filterOperator, Object filterConstant)
        throws IllegalArgumentException
    {
        super(propertyName, filterOperator);
        this.filterConstant = filterConstant;

        if (filterOperator.isRangeOperator())
        {
            throw new IllegalArgumentException("Illegal filter operator " + filterOperator + " supplied to " +
                    "constant filter parameter");
        }
    }

    public int getFilterHash()
    {
        if (filterConstant != null)
        {
            return filterConstant.hashCode();
        }
        return 0;
    }

    public Object getFilterValue(MatchedEventMap matchedEvents)
    {
        return filterConstant;
    }

    /**
     * Returns the constant value.
     * @return constant value
     */
    public Object getFilterConstant()
    {
        return filterConstant;
    }

    public final String toString()
    {
        return super.toString() + " filterConstant=" + filterConstant;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof FilterSpecParamConstant))
        {
            return false;
        }

        FilterSpecParamConstant other = (FilterSpecParamConstant) obj;
        if (!super.equals(other))
        {
            return false;
        }

        if (this.filterConstant != other.filterConstant)
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (filterConstant != null ? filterConstant.hashCode() : 0);
        return result;
    }
}

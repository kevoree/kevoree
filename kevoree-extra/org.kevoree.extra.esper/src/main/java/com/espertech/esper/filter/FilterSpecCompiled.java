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
import com.espertech.esper.pattern.MatchedEventMap;
import com.espertech.esper.epl.property.PropertyEvaluator;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Contains the filter criteria to sift through events. The filter criteria are the event class to look for and
 * a set of parameters (attribute names, operators and constant/range values).
 */
public final class FilterSpecCompiled
{
    private final EventType filterForEventType;
    private final String filterForEventTypeName;
    private final List<FilterSpecParam> parameters;
    private final PropertyEvaluator optionalPropertyEvaluator;

    /**
     * Constructor - validates parameter list against event type, throws exception if invalid
     * property names or mismatcing filter operators are found.
     * @param eventType is the event type
     * @param parameters is a list of filter parameters
     * @param eventTypeName is the name of the event type
     * @param optionalPropertyEvaluator optional if evaluating properties returned by filtered events
     * @throws IllegalArgumentException if validation invalid
     */
    public FilterSpecCompiled(EventType eventType, String eventTypeName, List<FilterSpecParam> parameters,
                              PropertyEvaluator optionalPropertyEvaluator)
    {
        this.filterForEventType = eventType;
        this.filterForEventTypeName = eventTypeName;
        this.parameters = parameters;
        this.optionalPropertyEvaluator = optionalPropertyEvaluator;
    }

    /**
     * Returns type of event to filter for.
     * @return event type
     */
    public final EventType getFilterForEventType()
    {
        return filterForEventType;
    }

    /**
     * Returns list of filter parameters.
     * @return list of filter params
     */
    public final List<FilterSpecParam> getParameters()
    {
        return parameters;
    }

    /**
     * Returns the event type name.
     * @return event type name
     */
    public String getFilterForEventTypeName()
    {
        return filterForEventTypeName;
    }

    /**
     * Return the evaluator for property value if any is attached, or none if none attached.
     * @return property evaluator
     */
    public PropertyEvaluator getOptionalPropertyEvaluator()
    {
        return optionalPropertyEvaluator;
    }

    /**
     * Returns the result event type of the filter specification.
     * @return event type
     */
    public EventType getResultEventType()
    {
        if (optionalPropertyEvaluator != null)
        {
            return optionalPropertyEvaluator.getFragmentEventType();
        }
        else
        {
            return filterForEventType;
        }
    }

    /**
     * Returns the values for the filter, using the supplied result events to ask filter parameters
     * for the value to filter for.
     * @param matchedEvents contains the result events to use for determining filter values
     * @return filter values
     */
    public FilterValueSet getValueSet(MatchedEventMap matchedEvents)
    {
        List<FilterValueSetParam> valueList = new LinkedList<FilterValueSetParam>();

        // Ask each filter specification parameter for the actual value to filter for
        for (FilterSpecParam specParam : parameters)
        {
            Object filterForValue = specParam.getFilterValue(matchedEvents);

            FilterValueSetParam valueParam = new FilterValueSetParamImpl(specParam.getPropertyName(),
                    specParam.getFilterOperator(), filterForValue);
            valueList.add(valueParam);
        }
        return new FilterValueSetImpl(filterForEventType, valueList);
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    public final String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("FilterSpecCompiled type=" + this.filterForEventType);
        buffer.append(" parameters=" + Arrays.toString(parameters.toArray()));
        return buffer.toString();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof FilterSpecCompiled))
        {
            return false;
        }

        FilterSpecCompiled other = (FilterSpecCompiled) obj;
        if (!equalsTypeAndFilter(other))
        {
            return false;
        }

        if ((this.optionalPropertyEvaluator == null) && (other.optionalPropertyEvaluator == null))
        {
            return true;
        }       
        if ((this.optionalPropertyEvaluator != null) && (other.optionalPropertyEvaluator == null))
        {
            return false;
        }
        if ((this.optionalPropertyEvaluator == null) && (other.optionalPropertyEvaluator != null))
        {
            return false;
        }

        return this.optionalPropertyEvaluator.compareTo(other.optionalPropertyEvaluator);
    }

    /**
     * Compares only the type and filter portion and not the property evaluation portion.
     * @param other filter to compare
     * @return true if same
     */
    public boolean equalsTypeAndFilter(FilterSpecCompiled other)
    {
        if (this.filterForEventType != other.filterForEventType)
        {
            return false;
        }
        if (this.parameters.size() != other.parameters.size())
        {
            return false;
        }

        Iterator<FilterSpecParam> iterOne = parameters.iterator();
        Iterator<FilterSpecParam> iterOther = other.parameters.iterator();
        while (iterOne.hasNext())
        {
            if (!iterOne.next().equals(iterOther.next()))
            {
                return false;
            }
        }

        return true;
    }

    public int hashCode()
    {
        int hashCode = filterForEventType.hashCode();
        for (FilterSpecParam param : parameters)
        {
            hashCode = 31*hashCode;
            hashCode ^= param.getPropertyName().hashCode();
            hashCode ^= (31 * param.getFilterHash());
        }
        return hashCode;
    }       
}

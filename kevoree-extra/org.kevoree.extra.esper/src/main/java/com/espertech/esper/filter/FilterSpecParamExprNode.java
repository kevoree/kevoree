/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeVariableVisitor;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.pattern.MatchedEventMap;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents an arbitrary expression node returning a boolean value as a filter parameter in an {@link FilterSpecCompiled} filter specification.
 */
public final class FilterSpecParamExprNode extends FilterSpecParam
{
    private final ExprNode exprNode;
    private final LinkedHashMap<String, Pair<EventType, String>> taggedEventTypes;
    private final LinkedHashMap<String, Pair<EventType, String>> arrayEventTypes;
    private final transient EventAdapterService eventAdapterService;
    private final transient VariableService variableService;
    private final boolean hasVariable;
    private static final long serialVersionUID = 2298436088557677833L;

    /**
     * Ctor.
     * @param propertyName is the event property name
     * @param filterOperator is expected to be the BOOLEAN_EXPR operator
     * @param exprNode represents the boolean expression
     * @param taggedEventTypes is null if the expression doesn't need other streams, or is filled with a ordered list of stream names and types
     * @param arrayEventTypes is a map of name tags and event type per tag for repeat-expressions that generate an array of events
     * @param variableService - provides access to variables
     * @param eventAdapterService for creating event types and event beans
     * @throws IllegalArgumentException for illegal args
     */
    public FilterSpecParamExprNode(String propertyName,
                             FilterOperator filterOperator,
                             ExprNode exprNode,
                             LinkedHashMap<String, Pair<EventType, String>> taggedEventTypes,
                             LinkedHashMap<String, Pair<EventType, String>> arrayEventTypes,
                             VariableService variableService,
                             EventAdapterService eventAdapterService)
        throws IllegalArgumentException
    {
        super(propertyName, filterOperator);
        if (filterOperator != FilterOperator.BOOLEAN_EXPRESSION)
        {
            throw new IllegalArgumentException("Invalid filter operator for filter expression node");
        }
        this.exprNode = exprNode;
        this.taggedEventTypes = taggedEventTypes;
        this.arrayEventTypes = arrayEventTypes;
        this.variableService = variableService;
        this.eventAdapterService = eventAdapterService;

        ExprNodeVariableVisitor visitor = new ExprNodeVariableVisitor();
        exprNode.accept(visitor);
        this.hasVariable = visitor.isHasVariables();
    }

    /**
     * Returns the expression node of the boolean expression this filter parameter represents.
     * @return expression node
     */
    public ExprNode getExprNode()
    {
        return exprNode;
    }

    /**
     * Returns the map of tag/stream names to event types that the filter expressions map use (for patterns)
     * @return map
     */
    public LinkedHashMap<String, Pair<EventType, String>> getTaggedEventTypes()
    {
        return taggedEventTypes;
    }

    public final Object getFilterValue(MatchedEventMap matchedEvents)
    {
        EventBean[] events = null;

        if ((taggedEventTypes != null) || (arrayEventTypes != null))
        {
            int size = 0;
            size += (taggedEventTypes != null) ? taggedEventTypes.size() : 0;
            size += (arrayEventTypes != null) ? arrayEventTypes.size() : 0;
            events = new EventBean[size + 1];

            int count = 1;
            if (taggedEventTypes != null)
            {
                for (String tag : taggedEventTypes.keySet())
                {
                    events[count] = matchedEvents.getMatchingEvent(tag);
                    count++;
                }
            }

            if (arrayEventTypes != null)
            {
                for (Map.Entry<String, Pair<EventType, String>> entry : arrayEventTypes.entrySet())
                {
                    EventType compositeEventType = entry.getValue().getFirst();
                    events[count] = eventAdapterService.adaptorForTypedMap(matchedEvents.getMatchingEvents(), compositeEventType);
                    count++;
                }
            }
        }

        if (hasVariable)
        {
            return new ExprNodeAdapter(exprNode, events, variableService);
        }
        else
        {
            return new ExprNodeAdapter(exprNode, events, null);
        }
    }

    public final String toString()
    {
        return super.toString() + "  exprNode=" + exprNode.toString();
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

        if (!(obj instanceof FilterSpecParamExprNode))
        {
            return false;
        }

        FilterSpecParamExprNode other = (FilterSpecParamExprNode) obj;
        if (!super.equals(other))
        {
            return false;
        }

        if (exprNode != other.exprNode)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + exprNode.hashCode();
        return result;
    }
}

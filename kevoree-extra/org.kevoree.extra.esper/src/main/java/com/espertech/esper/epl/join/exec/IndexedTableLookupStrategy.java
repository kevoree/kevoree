/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.exec;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.epl.join.table.PropertyIndexedEventTable;
import com.espertech.esper.epl.join.rep.Cursor;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Set;
import java.util.Arrays;

/**
 * Lookup on an index using a set of properties as key values.
 */
public class IndexedTableLookupStrategy implements TableLookupStrategy
{
    private final EventType eventType;
    private final String[] properties;
    private final PropertyIndexedEventTable index;
    private final EventPropertyGetter[] propertyGetters;

    /**
     * Ctor.
     * @param eventType - event type to expect for lookup
     * @param properties - key properties
     * @param index - index to look up in
     */
    public IndexedTableLookupStrategy(EventType eventType, String[] properties, PropertyIndexedEventTable index)
    {
        this.eventType = eventType;
        this.properties = properties;
        this.index = index;

        propertyGetters = new EventPropertyGetter[properties.length];
        for (int i = 0; i < properties.length; i++)
        {
            propertyGetters[i] = eventType.getGetter(properties[i]);

            if (propertyGetters[i] == null)
            {
                throw new IllegalArgumentException("Property named '" + properties[i] + "' is invalid for type " + eventType);
            }
        }
    }

    /**
     * Returns event type of the lookup event.
     * @return event type of the lookup event
     */
    public EventType getEventType()
    {
        return eventType;
    }

    /**
     * Returns properties to use from lookup event to look up in index.
     * @return properties to use from lookup event
     */
    public String[] getProperties()
    {
        return properties;
    }

    /**
     * Returns index to look up in.
     * @return index to use
     */
    public PropertyIndexedEventTable getIndex()
    {
        return index;
    }

    public Set<EventBean> lookup(EventBean event, Cursor cursor, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object[] keys = getKeys(event);
        return index.lookup(keys);
    }

    private Object[] getKeys(EventBean event)
    {
        return EventBeanUtility.getPropertyArray(event, propertyGetters);
    }

    public String toString()
    {
        return "IndexedTableLookupStrategy indexProps=" + Arrays.toString(properties) +
                " index=(" + index + ')';
    }
}

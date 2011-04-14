/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.epl.join.table.PropertyIndexedEventTable;

import java.util.Iterator;
import java.util.Set;

/**
 * Index lookup strategy into a poll-based cache result.
 */
public class HistoricalIndexLookupStrategyIndex implements HistoricalIndexLookupStrategy
{
    private final EventPropertyGetter[] propertyGetters;

    /**
     * Ctor.
     * @param eventType - event type to expect for lookup
     * @param properties - key properties
     */
    public HistoricalIndexLookupStrategyIndex(EventType eventType, String[] properties)
    {
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

    public Iterator<EventBean> lookup(EventBean lookupEvent, EventTable indexTable)
    {
        // The table may not be indexed as the cache may not actively cache, in which case indexing doesn't makes sense
        if (indexTable instanceof PropertyIndexedEventTable)
        {
            PropertyIndexedEventTable index = (PropertyIndexedEventTable) indexTable;
            Object[] keys = getKeys(lookupEvent);

            Set<EventBean> events = index.lookup(keys);
            if (events != null)
            {
                return events.iterator();
            }
            return null;
        }

        return indexTable.iterator();
    }

    private Object[] getKeys(EventBean event)
    {
        return EventBeanUtility.getPropertyArray(event, propertyGetters);
    }
}

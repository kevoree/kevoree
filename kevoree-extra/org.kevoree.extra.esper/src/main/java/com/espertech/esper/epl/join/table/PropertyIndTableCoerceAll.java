/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.util.JavaClassHelper;

import java.util.Set;

/**
 * Index that organizes events by the event property values into hash buckets. Based on a HashMap
 * with {@link com.espertech.esper.collection.MultiKeyUntyped} keys that store the property values.
 * <p>
 * Performs coercion of the index keys before storing the keys, and coercion of the lookup keys before lookup.
 * <p>
 * Takes a list of property names as parameter. Doesn't care which event type the events have as long as the properties
 * exist. If the same event is added twice, the class throws an exception on add.
 */
public class PropertyIndTableCoerceAll extends PropertyIndTableCoerceAdd
{
    private static Log log = LogFactory.getLog(PropertyIndTableCoerceAll.class);
    private final Class[] coercionTypes;

    /**
     * Ctor.
     * @param streamNum is the stream number of the indexed stream
     * @param eventType is the event type of the indexed stream
     * @param propertyNames are the property names to get property values
     * @param coercionType are the classes to coerce indexed values to
     */
    public PropertyIndTableCoerceAll(int streamNum, EventType eventType, String[] propertyNames, Class[] coercionType)
    {
        super(streamNum, eventType, propertyNames, coercionType);
        this.coercionTypes = coercionType;
    }

    /**
     * Returns the set of events that have the same property value as the given event.
     * @param keys to compare against
     * @return set of events with property value, or null if none found (never returns zero-sized set)
     */
    public Set<EventBean> lookup(Object[] keys)
    {
        for (int i = 0; i < keys.length; i++)
        {
            Class coercionType = coercionTypes[i];
            Object key = keys[i];
            if ((key != null) && (!key.getClass().equals(coercionType)))
            {
                if (key instanceof Number)
                {
                    key = JavaClassHelper.coerceBoxed((Number) key, coercionTypes[i]);
                    keys[i] = key;
                }
            }
        }
        MultiKeyUntyped key = new MultiKeyUntyped(keys);
        return propertyIndex.get(key);
    }

}

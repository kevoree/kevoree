/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.table;

import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.SimpleNumberCoercer;
import com.espertech.esper.util.SimpleNumberCoercerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Index that organizes events by the event property values into hash buckets. Based on a HashMap
 * with {@link com.espertech.esper.collection.MultiKeyUntyped} keys that store the property values.
 * <p>
 * Performs coercion of the index keys before storing the keys.
 * <p>
 * Takes a list of property names as parameter. Doesn't care which event type the events have as long as the properties
 * exist. If the same event is added twice, the class throws an exception on add.
 */
public class PropertyIndTableCoerceAdd extends PropertyIndexedEventTable
{
    private static Log log = LogFactory.getLog(PropertyIndTableCoerceAdd.class);
    private final SimpleNumberCoercer[] coercers;
    private final Class[] coercionType;

    /**
     * Ctor.
     * @param streamNum is the stream number of the indexed stream
     * @param eventType is the event type of the indexed stream
     * @param propertyNames are the property names to get property values
     * @param coercionType are the classes to coerce indexed values to
     */
    public PropertyIndTableCoerceAdd(int streamNum, EventType eventType, String[] propertyNames, Class[] coercionType)
    {
        super(streamNum, eventType, propertyNames, coercionType);
        this.coercionType = coercionType;
        coercers = new SimpleNumberCoercer[coercionType.length];
        for (int i = 0; i < coercionType.length; i++)
        {
            if (JavaClassHelper.isNumeric(coercionType[i])) {
                coercers[i] = SimpleNumberCoercerFactory.getCoercer(null, coercionType[i]);
            }
        }
    }

    protected MultiKeyUntyped getMultiKey(EventBean event)
    {
        Object[] keyValues = new Object[propertyGetters.length];
        for (int i = 0; i < propertyGetters.length; i++)
        {
            Object value = propertyGetters[i].get(event);
            if ((value != null) && (!value.getClass().equals(coercionType[i])))
            {
                if (value instanceof Number)
                {
                    value = coercers[i].coerceBoxed((Number) value);
                }
            }
            keyValues[i] = value;
        }
        return new MultiKeyUntyped(keyValues);
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;

import java.util.Map;

/**
 * A getter that works on EventBean events residing within a Map as an event property.
 */
public class MapEventBeanEntryPropertyGetter implements MapEventPropertyGetter {

    private final String propertyMap;
    private final EventPropertyGetter eventBeanEntryGetter;

    /**
     * Ctor.
     * @param propertyMap the property to look at
     * @param eventBeanEntryGetter the getter for the map entry
     */
    public MapEventBeanEntryPropertyGetter(String propertyMap, EventPropertyGetter eventBeanEntryGetter) {
        this.propertyMap = propertyMap;
        this.eventBeanEntryGetter = eventBeanEntryGetter;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        // If the map does not contain the key, this is allowed and represented as null
        Object value = map.get(propertyMap);

        if (value == null)
        {
            return null;
        }

        // Object within the map
        EventBean event = (EventBean) value;
        return eventBeanEntryGetter.get(event);
    }

    public boolean isMapExistsProperty(Map<String, Object> map)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object get(EventBean obj)
    {
        Object underlying = obj.getUnderlying();

        // The underlying is expected to be a map
        if (!(underlying instanceof Map))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type java.lang.Map");
        }

        Map map = (Map) underlying;
        return getMap(map);
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object getFragment(EventBean obj)
    {
        Object underlying = obj.getUnderlying();

        // The underlying is expected to be a map
        if (!(underlying instanceof Map))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type java.lang.Map");
        }

        Map map = (Map) underlying;

        // If the map does not contain the key, this is allowed and represented as null
        Object value = map.get(propertyMap);

        if (value == null)
        {
            return null;
        }

        // Object within the map
        EventBean event = (EventBean) value;
        return eventBeanEntryGetter.getFragment(event);
    }
}

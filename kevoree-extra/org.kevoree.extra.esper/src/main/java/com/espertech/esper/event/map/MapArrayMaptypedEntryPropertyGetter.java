package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;
import java.lang.reflect.Array;

/**
 * A getter that works on EventBean events residing within a Map as an event property.
 */
public class MapArrayMaptypedEntryPropertyGetter implements MapEventPropertyGetter {

    private final String propertyMap;
    private final int index;
    private final EventPropertyGetter eventBeanEntryGetter;
    private final EventType innerType;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param propertyMap the property to look at
     * @param eventBeanEntryGetter the getter for the map entry
     * @param index the index to fetch the array element for
     * @param innerType type of the entry returned
     * @param eventAdapterService factory for event beans and event types
     */
    public MapArrayMaptypedEntryPropertyGetter(String propertyMap, int index, EventPropertyGetter eventBeanEntryGetter, EventType innerType, EventAdapterService eventAdapterService) {
        this.propertyMap = propertyMap;
        this.index = index;
        this.eventBeanEntryGetter = eventBeanEntryGetter;
        this.innerType = innerType;
        this.eventAdapterService = eventAdapterService;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        Object value = map.get(propertyMap);
        if (value == null)
        {
            return null;
        }
        if (!value.getClass().isArray())
        {
            return null;
        }
        if (Array.getLength(value) <= index)
        {
            return null;
        }
        Object valueMap = Array.get(value, index);
        if (!(valueMap instanceof Map))
        {
            if (value instanceof EventBean) {
                return eventBeanEntryGetter.get((EventBean) value);
            }
            return null;
        }

        // If the map does not contain the key, this is allowed and represented as null
        EventBean eventBean = eventAdapterService.adaptorForTypedMap((Map) valueMap, innerType);
        return eventBeanEntryGetter.get(eventBean);
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

        Map<String, Object> map = (Map<String, Object>) underlying;
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

        Object value = map.get(propertyMap);
        if (value == null)
        {
            return null;
        }
        if (!value.getClass().isArray())
        {
            return null;
        }
        if (Array.getLength(value) <= index)
        {
            return null;
        }
        Object valueMap = Array.get(value, index);
        if (!(valueMap instanceof Map))
        {
            if (value instanceof EventBean) {
                return eventBeanEntryGetter.getFragment((EventBean) value);
            }
            return null;
        }

        // If the map does not contain the key, this is allowed and represented as null
        EventBean eventBean = eventAdapterService.adaptorForTypedMap((Map) valueMap, innerType);
        return eventBeanEntryGetter.getFragment(eventBean);        
    }
}

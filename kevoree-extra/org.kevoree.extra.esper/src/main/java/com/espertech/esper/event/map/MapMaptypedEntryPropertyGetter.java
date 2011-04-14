package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;

/**
 * A getter that works on EventBean events residing within a Map as an event property.
 */
public class MapMaptypedEntryPropertyGetter implements MapEventPropertyGetter {

    private final String propertyMap;
    private final EventPropertyGetter eventBeanEntryGetter;
    private final MapEventType fragmentType;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param propertyMap the property to look at
     * @param eventBeanEntryGetter the getter for the map entry
     * @param eventAdapterService factory for event beans and event types
     * @param fragmentType type of the entry returned
     */
    public MapMaptypedEntryPropertyGetter(String propertyMap, EventPropertyGetter eventBeanEntryGetter, MapEventType fragmentType, EventAdapterService eventAdapterService) {
        this.propertyMap = propertyMap;
        this.eventBeanEntryGetter = eventBeanEntryGetter;
        this.fragmentType = fragmentType;
        this.eventAdapterService = eventAdapterService;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        Object value = map.get(propertyMap);
        if (value == null)
        {
            return null;
        }
        if (!(value instanceof Map))
        {
            if (value instanceof EventBean) {
                return eventBeanEntryGetter.get((EventBean) value);
            }
            return null;
        }

        // If the map does not contain the key, this is allowed and represented as null
        EventBean eventBean = eventAdapterService.adaptorForTypedMap((Map) value, fragmentType);
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
        if (!(value instanceof Map))
        {
            if (value instanceof EventBean) {
                return eventBeanEntryGetter.getFragment((EventBean) value);
            }
            return null;
        }

        // If the map does not contain the key, this is allowed and represented as null
        EventBean eventBean = eventAdapterService.adaptorForTypedMap((Map) value, fragmentType);
        return eventBeanEntryGetter.getFragment(eventBean);
    }
}

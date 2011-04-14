package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;

/**
 * Getter for map entry.
 */
public class MapMaptypedPropertyGetter implements MapEventPropertyGetter
{
    private final String propertyName;
    private final EventType fragmentEventType;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param propertyNameAtomic property name
     * @param fragmentEventType fragment type
     * @param eventAdapterService factory for event beans and event types
     */
    public MapMaptypedPropertyGetter(String propertyNameAtomic, EventType fragmentEventType, EventAdapterService eventAdapterService)
    {
        this.propertyName = propertyNameAtomic;
        this.fragmentEventType = fragmentEventType;
        this.eventAdapterService = eventAdapterService;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        return map.get(propertyName);
    }

    public boolean isMapExistsProperty(Map<String, Object> map)
    {
        return true;
    }

    public Object get(EventBean obj) throws PropertyAccessException
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
        return true;
    }

    public Object getFragment(EventBean eventBean) throws PropertyAccessException
    {
        Object value = get(eventBean);
        if (!(value instanceof Map))
        {
            if (value instanceof EventBean) {
                return value;
            }
            return null;
        }
        Map mapTypedSubEvent = (Map) value;
        return eventAdapterService.adaptorForTypedMap(mapTypedSubEvent, fragmentEventType);
    }
}

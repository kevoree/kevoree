package com.espertech.esper.event.map;

import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;

/**
 * Getter for retrieving a value from a map.
 */
public class MapMaptypedUndPropertyGetter implements MapEventPropertyGetter
{
    private final String propertyName;
    private final EventAdapterService eventAdapterService;
    private final EventType fragmentType;

    /**
     * Ctor.
     * @param propertyNameAtomic property name
     * @param eventAdapterService factory for event beans and event types
     * @param fragmentType type of the entry returned
     */
    public MapMaptypedUndPropertyGetter(String propertyNameAtomic, EventAdapterService eventAdapterService, EventType fragmentType)
    {
        propertyName = propertyNameAtomic;
        this.fragmentType = fragmentType;
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

        return map.get(propertyName);
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true;
    }

    public Object getFragment(EventBean obj) throws PropertyAccessException
    {
        Map<String, Object> value = (Map<String, Object>) get(obj);

        if (value == null)
        {
            return null;
        }

        return eventAdapterService.adaptorForTypedMap(value, fragmentType);
    }
}

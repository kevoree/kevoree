package com.espertech.esper.event.map;

import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;
import java.lang.reflect.Array;

/**
 * Getter for Map-entries with well-defined fragment type.
 */
public class MapArrayMaptypedUndPropertyGetter implements MapEventPropertyGetter
{
    private final String propertyName;
    private final int index;
    private final EventAdapterService eventAdapterService;
    private final EventType fragmentType;

    /**
     * Ctor.
     * @param propertyNameAtomic property name
     * @param index array index
     * @param eventAdapterService factory for event beans and event types
     * @param fragmentType type of the entry returned
     */
    public MapArrayMaptypedUndPropertyGetter(String propertyNameAtomic, int index, EventAdapterService eventAdapterService, EventType fragmentType)
    {
        this.propertyName = propertyNameAtomic;
        this.index = index;
        this.fragmentType = fragmentType;
        this.eventAdapterService = eventAdapterService;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        Object value = map.get(propertyName);

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

        return Array.get(value, index);
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

        Map<String, Object> map = (Map<String, Object>) underlying;
        return getMap(map);
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
package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.bean.BaseNativePropertyGetter;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;
import java.lang.reflect.Array;

/**
 * A getter that works on arrays residing within a Map as an event property.
 */
public class MapArrayPOJOEntryIndexedPropertyGetter extends BaseNativePropertyGetter implements MapEventPropertyGetter
{
    private final String propertyMap;
    private final int index;

    /**
     * Ctor.
     * @param propertyMap the property to use for the map lookup
     * @param index the index to fetch the array element for
     * @param eventAdapterService factory for event beans and event types
     * @param returnType type of the entry returned
     */
    public MapArrayPOJOEntryIndexedPropertyGetter(String propertyMap, int index, EventAdapterService eventAdapterService, Class returnType)
    {
        super(eventAdapterService, returnType, null);
        this.propertyMap = propertyMap;
        this.index = index;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        // If the map does not contain the key, this is allowed and represented as null
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
        return Array.get(value, index);
    }

    public boolean isMapExistsProperty(Map<String, Object> map)
    {
        return map.containsKey(propertyMap);
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
        Object underlying = eventBean.getUnderlying();

        // The underlying is expected to be a map
        if (!(underlying instanceof Map))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type java.lang.Map");
        }

        Map map = (Map) underlying;
        return map.containsKey(propertyMap);
    }
}

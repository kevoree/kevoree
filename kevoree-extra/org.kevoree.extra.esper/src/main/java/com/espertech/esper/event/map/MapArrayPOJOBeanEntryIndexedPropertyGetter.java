package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.bean.BaseNativePropertyGetter;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;
import java.lang.reflect.Array;

/**
 * A getter that works on POJO events residing within a Map as an event property.
 */
public class MapArrayPOJOBeanEntryIndexedPropertyGetter extends BaseNativePropertyGetter implements MapEventPropertyGetter {

    private final String propertyMap;
    private final int index;
    private final EventPropertyGetter mapEntryGetter;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param propertyMap the property to look at
     * @param mapEntryGetter the getter for the map entry
     * @param eventAdapterService for producing wrappers to objects
     * @param index the index to fetch the array element for
     * @param returnType type of the entry returned
     */
    public MapArrayPOJOBeanEntryIndexedPropertyGetter(String propertyMap, int index, EventPropertyGetter mapEntryGetter, EventAdapterService eventAdapterService, Class returnType) {
        super(eventAdapterService, returnType, null);
        this.propertyMap = propertyMap;
        this.index = index;
        this.mapEntryGetter = mapEntryGetter;
        this.eventAdapterService = eventAdapterService;
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
        Object arrayItem = Array.get(value, index);
        if (arrayItem == null)
        {
            return null;
        }

        // Object within the map
        EventBean event = eventAdapterService.adapterForBean(arrayItem);
        return mapEntryGetter.get(event);
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
}

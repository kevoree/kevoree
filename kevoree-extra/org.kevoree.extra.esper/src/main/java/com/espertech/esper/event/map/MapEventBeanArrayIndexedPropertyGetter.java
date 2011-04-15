package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;

import java.util.Map;

/**
 * Getter for array events.
 */
public class MapEventBeanArrayIndexedPropertyGetter implements MapEventPropertyGetter
{
    private final String propertyName;
    private final int index;

    /**
     * Ctor.
     * @param propertyName property name
     * @param index array index
     */
    public MapEventBeanArrayIndexedPropertyGetter(String propertyName, int index)
    {
        this.propertyName = propertyName;
        this.index = index;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        // If the map does not contain the key, this is allowed and represented as null
        EventBean[] wrapper = (EventBean[]) map.get(propertyName);

        if (wrapper == null)
        {
            return null;
        }
        if (wrapper.length <= index)
        {
            return null;
        }

        return wrapper[index].getUnderlying();
    }

    public boolean isMapExistsProperty(Map<String, Object> map)
    {
        return true;
    }

    public Object get(EventBean obj)
    {
        // The underlying is expected to be a map
        if (!(obj.getUnderlying() instanceof Map))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type java.lang.Map");
        }

        Map<String, Object> map = (Map<String, Object>) obj.getUnderlying();

        return getMap(map);
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object getFragment(EventBean obj)
    {
        // The underlying is expected to be a map
        if (!(obj.getUnderlying() instanceof Map))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type java.lang.Map");
        }

        Map map = (Map) obj.getUnderlying();

        // If the map does not contain the key, this is allowed and represented as null
        EventBean[] wrapper = (EventBean[]) map.get(propertyName);

        if (wrapper == null)
        {
            return null;
        }
        if (wrapper.length <= index)
        {
            return null;
        }

        return wrapper[index];
    }
}
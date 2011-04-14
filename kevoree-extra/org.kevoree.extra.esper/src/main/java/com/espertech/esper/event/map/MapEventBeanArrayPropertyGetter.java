package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Returns the event bean or the underlying array.
 */
public class MapEventBeanArrayPropertyGetter implements MapEventPropertyGetter
{
    private final String propertyName;
    private final Class underlyingType;

    /**
     * Ctor.
     * @param propertyName property to get
     * @param underlyingType type of property
     */
    public MapEventBeanArrayPropertyGetter(String propertyName, Class underlyingType)
    {
        this.propertyName = propertyName;
        this.underlyingType = underlyingType;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        Object mapValue = map.get(propertyName);

        // If the map does not contain the key, this is allowed and represented as null
        EventBean[] wrapper = (EventBean[]) mapValue;
        if (wrapper !=  null)
        {
            Object array = Array.newInstance(underlyingType, wrapper.length);
            for (int i = 0; i < wrapper.length; i++)
            {
                Array.set(array, i, wrapper[i].getUnderlying());
            }
            return array;
        }

        return null;
    }

    public boolean isMapExistsProperty(Map<String, Object> map)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object get(EventBean obj)
    {
        // The underlying is expected to be a map
        if (!(obj.getUnderlying() instanceof Map))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type java.lang.Map");
        }

        Map map = (Map) obj.getUnderlying();
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
        return map.get(propertyName);
    }
}

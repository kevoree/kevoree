package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;

import java.util.Map;

/**
 * Getter for map array.
 */
public class MapMaptypedArrayPropertyGetter implements MapEventPropertyGetter
{
    private final String propertyName;
    private final EventType fragmentEventType;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param propertyNameAtomic property type
     * @param fragmentEventType event type of fragment
     * @param eventAdapterService for creating event instances
     */
    public MapMaptypedArrayPropertyGetter(String propertyNameAtomic, EventType fragmentEventType, EventAdapterService eventAdapterService)
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
        if (!(value instanceof Map[]))
        {
            return null;
        }
        Map[] mapTypedSubEvents = (Map[]) value;

        int countNull = 0;
        for (Map map : mapTypedSubEvents)
        {
            if (map != null)
            {
                countNull++;
            }
        }

        EventBean[] mapEvents = new EventBean[countNull];
        int count = 0;
        for (Map map : mapTypedSubEvents)
        {
            if (map != null)
            {
                mapEvents[count++] = eventAdapterService.adaptorForTypedMap(map, fragmentEventType);
            }
        }

        return mapEvents;
    }
}

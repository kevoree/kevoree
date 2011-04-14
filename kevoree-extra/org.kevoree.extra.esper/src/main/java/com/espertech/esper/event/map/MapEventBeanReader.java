package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.EventBeanReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reader method for reading all properties of a Map event.
 */
public class MapEventBeanReader implements EventBeanReader
{
    private MapEventPropertyGetter[] getterArray;

    /**
     * Ctor.
     * @param type map to read
     */
    public MapEventBeanReader(MapEventType type)
    {
        String[] properties = type.getPropertyNames();
        List<MapEventPropertyGetter> getters = new ArrayList<MapEventPropertyGetter>();
        for (String property : properties)
        {
            MapEventPropertyGetter getter = type.getGetter(property);
            if (getter != null)
            {
                getters.add(getter);
            }
        }
        getterArray = getters.toArray(new MapEventPropertyGetter[getters.size()]);
    }

    public Object[] read(EventBean event)
    {
        Map<String, Object> underlying = (Map<String, Object>) event.getUnderlying();
        Object[] values = new Object[getterArray.length];
        for (int i = 0; i < getterArray.length; i++)
        {
            values[i] = getterArray[i].getMap(underlying);
        }
        return values;
    }
}
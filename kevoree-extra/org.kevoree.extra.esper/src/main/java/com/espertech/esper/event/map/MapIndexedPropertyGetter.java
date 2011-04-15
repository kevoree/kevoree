/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Getter for a dynamic indexed property for maps.
 */
public class MapIndexedPropertyGetter implements MapEventPropertyGetter
{
    private final int index;
    private final String fieldName;

    /**
     * Ctor.
     * @param fieldName property name
     * @param index index to get the element at
     */
    public MapIndexedPropertyGetter(String fieldName, int index)
    {
        this.index = index;
        this.fieldName = fieldName;
    }

    public Object getMap(Map<String, Object> map) throws PropertyAccessException
    {
        Object value = map.get(fieldName);
        if (value == null)
        {
            return null;
        }
        if (!value.getClass().isArray())
        {
            return null;
        }
        if (index >= Array.getLength(value))
        {
            return null;
        }
        return Array.get(value, index);
    }

    public boolean isMapExistsProperty(Map<String, Object> map)
    {
        Object value = map.get(fieldName);
        if (value == null)
        {
            return false;
        }
        if (!value.getClass().isArray())
        {
            return false;
        }
        if (index >= Array.getLength(value))
        {
            return false;
        }
        return true;
    }

    public Object get(EventBean eventBean) throws PropertyAccessException
    {
        Object underlying = eventBean.getUnderlying();
        if (!(underlying instanceof Map))
        {
            return null;
        }
        Map map = (Map) underlying;
        return getMap(map);
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        Object underlying = eventBean.getUnderlying();
        if (!(underlying instanceof Map))
        {
            return false;
        }
        Map map = (Map) underlying;
        return isMapExistsProperty(map);
    }

    public Object getFragment(EventBean eventBean)
    {
        return null;
    }
}

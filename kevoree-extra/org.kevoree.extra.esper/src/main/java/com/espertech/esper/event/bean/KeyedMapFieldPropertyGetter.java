/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.util.JavaClassHelper;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Getter for a key property identified by a given key value, using vanilla reflection.
 */
public class KeyedMapFieldPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final Field field;
    private final Object key;

    /**
     * Constructor.
     * @param field is the field to use to retrieve a value from the object.
     * @param key is the key to supply as parameter to the mapped property getter
     * @param eventAdapterService factory for event beans and event types
     */
    public KeyedMapFieldPropertyGetter(Field field, Object key, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, JavaClassHelper.getGenericFieldTypeMap(field, false), null);
        this.key = key;
        this.field = field;
    }


    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            Object result = field.get(object);
            if (!(result instanceof Map)) {
                return null;
            }
            Map resultMap = (Map) result;
            return resultMap.get(key);
        }
        catch (ClassCastException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
        }
        catch (IllegalAccessException e)
        {
            throw new PropertyAccessException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new PropertyAccessException(e);
        }
    }

    public boolean isBeanExistsProperty(Object object)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public final Object get(EventBean obj) throws PropertyAccessException
    {
        Object underlying = obj.getUnderlying();
        return getBeanProp(underlying);
    }

    public String toString()
    {
        return "KeyedMapFieldPropertyGetter " +
                " field=" + field.toString() +
                " key=" + key;
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
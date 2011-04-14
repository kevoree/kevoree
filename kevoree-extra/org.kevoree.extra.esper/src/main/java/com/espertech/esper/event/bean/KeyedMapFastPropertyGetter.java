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
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Getter for a key property identified by a given key value of a map, using the CGLIB fast method.
 */
public class KeyedMapFastPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final FastMethod fastMethod;
    private final Object key;

    /**
     * Constructor.
     * @param method the underlying method
     * @param fastMethod is the method to use to retrieve a value from the object.
     * @param key is the key to supply as parameter to the mapped property getter
     * @param eventAdapterService factory for event beans and event types
     */
    public KeyedMapFastPropertyGetter(Method method, FastMethod fastMethod, Object key, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, JavaClassHelper.getGenericReturnTypeMap(method, false), null);
        this.key = key;
        this.fastMethod = fastMethod;
    }

    public boolean isBeanExistsProperty(Object object)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            Object result = fastMethod.invoke(object, null);
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
        catch (InvocationTargetException e)
        {
            throw new PropertyAccessException(e);
        }
    }

    public final Object get(EventBean obj) throws PropertyAccessException
    {
        Object underlying = obj.getUnderlying();
        return getBeanProp(underlying);
    }

    public String toString()
    {
        return "KeyedMapFastPropertyGetter " +
                " fastMethod=" + fastMethod.toString() +
                " key=" + key;
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
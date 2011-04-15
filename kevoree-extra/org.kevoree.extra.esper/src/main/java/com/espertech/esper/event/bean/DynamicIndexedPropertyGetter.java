/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.bean;

import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.bean.DynamicPropertyDescriptor;

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Getter for a dynamic indexed property (syntax field.indexed[0]?), using vanilla reflection.
 */
public class DynamicIndexedPropertyGetter extends DynamicPropertyGetterBase
{
    private final String getterMethodName;
    private final Object[] params;
    private final int index;

    /**
     * Ctor.
     * @param fieldName property name
     * @param index index to get the element at
     * @param eventAdapterService factory for event beans and event types
     */
    public DynamicIndexedPropertyGetter(String fieldName, int index, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService);
        getterMethodName = getGetterMethodName(fieldName);
        this.params = new Object[] {index};
        this.index = index;
    }

    protected Method determineMethod(Class clazz)
    {
        Method method;

        try
        {
            return clazz.getMethod(getterMethodName, int.class);
        }
        catch (NoSuchMethodException ex1)
        {
            try
            {
                method = clazz.getMethod(getterMethodName);
            }
            catch (NoSuchMethodException e)
            {
                return null;
            }
            if (!method.getReturnType().isArray())
            {
                return null;
            }
            return method;
        }
    }

    protected Object call(DynamicPropertyDescriptor descriptor, Object underlying)
    {
        try
        {
            if (descriptor.isHasParameters())
            {
                return descriptor.getMethod().invoke(underlying, params);
            }
            else
            {
                Object array = descriptor.getMethod().invoke(underlying, null);
                if (array == null)
                {
                    return null;
                }
                if (Array.getLength(array) <= index)
                {
                    return null;
                }
                return Array.get(array, index);
            }
        }
        catch (ClassCastException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
        }
        catch (InvocationTargetException e)
        {
            throw new PropertyAccessException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new PropertyAccessException(e);
        }
    }

    private static String getGetterMethodName(String propertyName)
    {
        StringWriter writer = new StringWriter();
        writer.write("get");
        writer.write(Character.toUpperCase(propertyName.charAt(0)));
        writer.write(propertyName.substring(1));
        return writer.toString();
    }
}

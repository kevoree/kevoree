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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Getter for a dynamic property (syntax field.inner?), using vanilla reflection.
 */
public class DynamicSimplePropertyGetter extends DynamicPropertyGetterBase 
{
    private final String getterMethodName;
    private final String isMethodName;

    /**
     * Ctor.
     * @param fieldName the property name
     * @param eventAdapterService factory for event beans and event types
     */
    public DynamicSimplePropertyGetter(String fieldName, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService);
        getterMethodName = getGetterMethodName(fieldName);
        isMethodName = getIsMethodName(fieldName);
    }

    protected Object call(DynamicPropertyDescriptor descriptor, Object underlying)
    {
        try
        {
            return descriptor.getMethod().invoke(underlying, null);
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

    protected Method determineMethod(Class clazz)
    {
        try
        {
            return clazz.getMethod(getterMethodName);
        }
        catch (NoSuchMethodException ex1)
        {
            try
            {
                return clazz.getMethod(isMethodName);
            }
            catch (NoSuchMethodException ex2)
            {
                return null;
            }
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

    private static String getIsMethodName(String propertyName)
    {
        StringWriter writer = new StringWriter();
        writer.write("is");
        writer.write(Character.toUpperCase(propertyName.charAt(0)));
        writer.write(propertyName.substring(1));
        return writer.toString();
    }
}

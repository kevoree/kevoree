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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Getter for an array property identified by a given index, using vanilla reflection.
 */
public class ArrayMethodPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final Method method;
    private final int index;

    /**
     * Constructor.
     * @param method is the method to use to retrieve a value from the object
     * @param index is tge index within the array to get the property from
     * @param eventAdapterService factory for event beans and event types
     */
    public ArrayMethodPropertyGetter(Method method, int index, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, method.getReturnType().getComponentType(), null);
        this.index = index;
        this.method = method;

        if (index < 0)
        {
            throw new IllegalArgumentException("Invalid negative index value");
        }
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            Object value = method.invoke(object, (Object[]) null);
            if (Array.getLength(value) <= index)
            {
                return null;
            }
            return Array.get(value, index);
        }
        catch (ClassCastException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
        }
        catch (InvocationTargetException e)
        {
            throw new PropertyAccessException(e);
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
        return "ArrayMethodPropertyGetter " +
                " method=" + method.toString() +
                " index=" + index;
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}

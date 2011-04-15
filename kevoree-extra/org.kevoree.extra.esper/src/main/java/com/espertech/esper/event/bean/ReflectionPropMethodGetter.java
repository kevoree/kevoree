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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Property getter for methods using Java's vanilla reflection.
 */
public final class ReflectionPropMethodGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final Method method;

    /**
     * Constructor.
     * @param method is the regular reflection method to use to obtain values for a field.
     * @param eventAdapterService factory for event beans and event types
     */
    public ReflectionPropMethodGetter(Method method, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, method.getReturnType(), JavaClassHelper.getGenericReturnType(method, false));
        this.method = method;
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            return method.invoke(object, (Object[]) null);
        }
        catch (IllegalArgumentException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
        }
        catch (IllegalAccessException e)
        {
            throw new PropertyAccessException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new PropertyAccessException(e);
        }
    }

    public boolean isBeanExistsProperty(Object object)
    {
        return true;
    }

    public final Object get(EventBean obj) throws PropertyAccessException
    {
        Object underlying = obj.getUnderlying();
        return getBeanProp(underlying);
    }

    public String toString()
    {
        return "ReflectionPropMethodGetter " +
                "method=" + method.toGenericString();
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}

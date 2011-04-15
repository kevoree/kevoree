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

/**
 * Property getter using CGLib's FastMethod instance.
 */
public class CGLibPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final FastMethod fastMethod;

    /**
     * Constructor.
     * @param method the underlying method
     * @param fastMethod is the method to use to retrieve a value from the object.
     * @param eventAdapterService factory for event beans and event types
     */
    public CGLibPropertyGetter(Method method, FastMethod fastMethod, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, fastMethod.getReturnType(), JavaClassHelper.getGenericReturnType(method, true));
        this.fastMethod = fastMethod;
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            return fastMethod.invoke(object, null);
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
        return "CGLibPropertyGetter " +
                "fastMethod=" + fastMethod.toString();
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}

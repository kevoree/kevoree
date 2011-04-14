/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.cglib.reflect.FastMethod;
import net.sf.cglib.reflect.FastClass;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.NaturalEventBean;
import com.espertech.esper.collection.UniformPair;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;

/**
 * A result delivery strategy that uses an "update" method that accepts a underlying array
 * for use in wildcard selection.
 */
public class ResultDeliveryStrategyTypeArr implements ResultDeliveryStrategy
{
    private static Log log = LogFactory.getLog(ResultDeliveryStrategyImpl.class);
    private final Object subscriber;
    private final FastMethod fastMethod;
    private final Class componentType;

    /**
     * Ctor.
     * @param subscriber is the receiver to method invocations
     * @param method is the method to deliver to
     */
    public ResultDeliveryStrategyTypeArr(Object subscriber, Method method)
    {
        this.subscriber = subscriber;
        FastClass fastClass = FastClass.create(Thread.currentThread().getContextClassLoader(), subscriber.getClass());
        this.fastMethod = fastClass.getMethod(method);
        componentType = method.getParameterTypes()[0].getComponentType();
    }

    public void execute(UniformPair<EventBean[]> result)
    {
        Object newData = convert(result.getFirst());
        Object oldData = convert(result.getSecond());

        Object params[] = new Object[] {newData, oldData};
        try {
            fastMethod.invoke(subscriber, params);
        }
        catch (InvocationTargetException e) {
            ResultDeliveryStrategyImpl.handle(log, e, params, subscriber, fastMethod);
        }
    }

    private Object convert(EventBean[] events)
    {
        if ((events == null) || (events.length == 0))
        {
            return null;
        }

        Object array = Array.newInstance(componentType, events.length);
        int length = 0;
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof NaturalEventBean)
            {
                NaturalEventBean natural = (NaturalEventBean) events[i];
                Array.set(array, length, natural.getNatural()[0]);
                length++;
            }
        }

        if (length == 0)
        {
            return null;
        }
        if (length != events.length)
        {
            Object reduced = Array.newInstance(componentType, events.length);
            System.arraycopy(array, 0, reduced, 0, length);
            array = reduced;
        }
        return array;
    }
}

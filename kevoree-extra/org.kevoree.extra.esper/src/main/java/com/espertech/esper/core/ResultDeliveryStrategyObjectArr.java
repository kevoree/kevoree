/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.NaturalEventBean;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A result delivery strategy that uses an "update" method that accepts a pair of object array array.
 */
public class ResultDeliveryStrategyObjectArr implements ResultDeliveryStrategy
{
    private static Log log = LogFactory.getLog(ResultDeliveryStrategyImpl.class);
    private final Object subscriber;
    private final FastMethod fastMethod;

    /**
     * Ctor.
     * @param subscriber is the subscriber to deliver to
     * @param method the method to invoke
     */
    public ResultDeliveryStrategyObjectArr(Object subscriber, Method method)
    {
        this.subscriber = subscriber;
        FastClass fastClass = FastClass.create(Thread.currentThread().getContextClassLoader(), subscriber.getClass());
        this.fastMethod = fastClass.getMethod(method);
    }

    public void execute(UniformPair<EventBean[]> result)
    {
        Object[][] newData = convert(result.getFirst());
        Object[][] oldData = convert(result.getSecond());

        Object[] params = new Object[] {newData, oldData};
        try {
            fastMethod.invoke(subscriber, params);
        }
        catch (InvocationTargetException e) {
            ResultDeliveryStrategyImpl.handle(log, e, params, subscriber, fastMethod);
        }
    }

    private Object[][] convert(EventBean[] events)
    {
        if ((events == null) || (events.length == 0))
        {
            return null;
        }

        Object[][] result = new Object[events.length][];
        int length = 0;
        for (int i = 0; i < result.length; i++)
        {
            if (events[i] instanceof NaturalEventBean)
            {
                NaturalEventBean natural = (NaturalEventBean) events[i];
                result[length] = natural.getNatural();
                length++;
            }
        }

        if (length == 0)
        {
            return null;
        }
        if (length != events.length)
        {
            Object[][] reduced = new Object[length][];
            System.arraycopy(result, 0, reduced, 0, length);
            result = reduced;
        }
        return result;
    }
}

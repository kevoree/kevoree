package com.espertech.esper.event.bean;

import com.espertech.esper.event.EventBeanCopyMethod;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.SerializableObjectCopier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.sf.cglib.reflect.FastMethod;

/**
 * Copies an event for modification.
 */
public class BeanEventBeanConfiguredCopyMethod implements EventBeanCopyMethod
{
    private static final Log log = LogFactory.getLog(BeanEventBeanConfiguredCopyMethod.class);

    private final BeanEventType beanEventType;
    private final EventAdapterService eventAdapterService;
    private final FastMethod copyMethod;

    /**
     * Ctor.
     * @param beanEventType type of bean to copy
     * @param eventAdapterService for creating events
     * @param copyMethod method to copy the event
     */
    public BeanEventBeanConfiguredCopyMethod(BeanEventType beanEventType, EventAdapterService eventAdapterService, FastMethod copyMethod)
    {
        this.beanEventType = beanEventType;
        this.eventAdapterService = eventAdapterService;
        this.copyMethod = copyMethod;
    }

    public EventBean copy(EventBean event)
    {
        Object underlying = event.getUnderlying();
        Object copied;
        try
        {
            copied = copyMethod.invoke(underlying, null);
        }
        catch (InvocationTargetException e)
        {
            log.error("InvocationTargetException copying event object for update: " + e.getMessage(), e);
            return null;
        }
        catch (RuntimeException e)
        {
            log.error("RuntimeException copying event object for update: " + e.getMessage(), e);
            return null;
        }

        return eventAdapterService.adapterForTypedBean(copied, beanEventType);
    }
}

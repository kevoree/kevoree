package com.espertech.esper.event.bean;

import com.espertech.esper.event.EventBeanCopyMethod;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.SerializableObjectCopier;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copy method for bean events utilizing serializable.
 */
public class BeanEventBeanSerializableCopyMethod implements EventBeanCopyMethod
{
    private static final Log log = LogFactory.getLog(BeanEventBeanSerializableCopyMethod.class);

    private final BeanEventType beanEventType;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param beanEventType event type
     * @param eventAdapterService for creating the event object
     */
    public BeanEventBeanSerializableCopyMethod(BeanEventType beanEventType, EventAdapterService eventAdapterService)
    {
        this.beanEventType = beanEventType;
        this.eventAdapterService = eventAdapterService;
    }

    public EventBean copy(EventBean event)
    {
        Object underlying = event.getUnderlying();
        Object copied;
        try
        {
            copied = SerializableObjectCopier.copy(underlying);
        }
        catch (IOException e)
        {
            log.error("IOException copying event object for update: " + e.getMessage(), e);
            return null;
        }
        catch (ClassNotFoundException e)
        {
            log.error("Exception copying event object for update: " + e.getMessage(), e);
            return null;
        }

        return eventAdapterService.adapterForTypedBean(copied, beanEventType);

    }
}

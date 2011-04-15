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
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;

/**
 * Wrapper for Java bean (POJO or regular) Java objects the represent events.
 * Allows access to event properties, which is done through the getter supplied by the event type.
 * {@link EventType} instances containing type information are obtained from {@link BeanEventTypeFactory}.
 * Two BeanEventBean instances are equal if they have the same event type and refer to the same instance of event object.
 * Clients that need to compute equality between Java beans wrapped by this class need to obtain the underlying object.
 */
public class BeanEventBean implements EventBean
{
    private EventType eventType;
    private Object event;

    /**
     * Constructor.
     * @param event is the event object
     * @param eventType is the schema information for the event object.
     */
    public BeanEventBean(Object event, EventType eventType)
    {
        this.eventType = eventType;
        this.event = event;
    }

    public Object getUnderlying()
    {
        return event;
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public Object get(String property) throws PropertyAccessException
    {
        EventPropertyGetter getter = eventType.getGetter(property);
        if (getter == null)
        {
            throw new PropertyAccessException("Property named '" + property + "' is not a valid property name for this type");
        }
        return getter.get(this);
    }

    public String toString()
    {
        return "BeanEventBean" +
               " eventType=" + eventType +
               " bean=" + event;
    }

    public Object getFragment(String propertyExpression)
    {
        EventPropertyGetter getter = eventType.getGetter(propertyExpression);
        if (getter == null)
        {
            throw new PropertyAccessException("Property named '" + propertyExpression + "' is not a valid property name for this type");
        }
        return getter.getFragment(this);
    }
}

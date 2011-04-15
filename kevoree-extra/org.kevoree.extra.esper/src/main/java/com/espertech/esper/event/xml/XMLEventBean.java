/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.xml;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.PropertyAccessException;

import org.w3c.dom.Node;

/**
 * EventBean wrapper for XML documents.
 * Currently only instances of org.w3c.dom.Node can be used
 *
 * @author pablo
 *
 */
public class XMLEventBean implements EventBean
{
	private EventType eventType;
	private Node event;

    /**
     * Ctor.
     * @param event is the node with event property information
     * @param type is the event type for this event wrapper
     */
    public XMLEventBean(Node event, EventType type) {
		this.event = event;
		eventType = type;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Object get(String property) throws PropertyAccessException {
		EventPropertyGetter getter = eventType.getGetter(property);
		if (getter == null)
			 throw new PropertyAccessException("Property named '" + property + "' is not a valid property name for this type");
		return getter.get(this);
	}

	public Object getUnderlying() {
		return event;
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

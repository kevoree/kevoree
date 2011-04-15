package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Writer for a single property value to an event.
 */
public interface EventPropertyWriter
{
    /**
     * Value to write to a property.
     * @param value value to write
     * @param target property to write to
     */
    public void write(Object value, EventBean target);
}

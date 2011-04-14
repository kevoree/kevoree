package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Interface for writing a set of event properties to an event.
 */
public interface EventBeanWriter
{
    /**
     * Write property values to the event.
     * @param values to write
     * @param event to write to
     */
    public void write(Object[] values, EventBean event);
}

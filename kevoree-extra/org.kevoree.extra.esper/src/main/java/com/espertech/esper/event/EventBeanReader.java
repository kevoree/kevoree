package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Interface for reading all event properties of an event.
 */
public interface EventBeanReader
{
    /**
     * Returns all event properties in the exact order they appear as properties.
     * @param event to read
     * @return property values
     */
    public Object[] read(EventBean event);
}

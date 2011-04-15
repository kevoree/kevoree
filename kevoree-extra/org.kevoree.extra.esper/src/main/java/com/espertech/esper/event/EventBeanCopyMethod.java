package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Implementations copy the event object for controlled modification (shallow copy).
 */
public interface EventBeanCopyMethod
{
    /**
     * Copy the event bean returning a shallow copy.
     * @param event to copy
     * @return shallow copy
     */
    public EventBean copy(EventBean event);
}

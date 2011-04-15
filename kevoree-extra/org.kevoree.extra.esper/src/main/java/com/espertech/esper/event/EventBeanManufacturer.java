package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Factory for creating an event bean instance by writing property values to an underlying object.
 */
public interface EventBeanManufacturer
{
    /**
     * Make an event object populating property values.
     * @param properties values to populate
     * @return event object
     */
    public EventBean make(Object[] properties);
}

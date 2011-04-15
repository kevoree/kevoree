package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

import java.util.Map;

/**
 * Writer for values to a wrapper event.
 */
public class WrapperEventBeanUndWriter implements EventBeanWriter
{
    private final EventBeanWriter undWriter;

    /**
     * Ctor.
     * @param undWriter writer to the underlying object
     */
    public WrapperEventBeanUndWriter(EventBeanWriter undWriter)
    {
       this.undWriter = undWriter;
    }

    public void write(Object[] values, EventBean event)
    {
        DecoratingEventBean wrappedEvent = (DecoratingEventBean) event;
        EventBean eventWrapped = wrappedEvent.getUnderlyingEvent();
        undWriter.write(values, eventWrapped);
    }
}

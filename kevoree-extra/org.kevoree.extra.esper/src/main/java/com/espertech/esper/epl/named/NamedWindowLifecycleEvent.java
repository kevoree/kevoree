package com.espertech.esper.epl.named;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.core.StatementLifecycleEvent;

/**
 * Event indicating named window lifecycle management.
 */
public class NamedWindowLifecycleEvent
{
    private String name;
    private NamedWindowProcessor processor;
    private NamedWindowLifecycleEvent.LifecycleEventType eventType;
    private Object[] params;

    /**
     * Event types.
     */
    public static enum LifecycleEventType {
        /**
         * Named window created.
         */
        CREATE,

        /**
         * Named window removed.
         */
        DESTROY
    }

    /**
     * Ctor.
     * @param name is the name of the named window
     * @param processor instance for processing the named window contents
     * @param eventType the type of event
     * @param params event parameters
     */
    protected NamedWindowLifecycleEvent(String name, NamedWindowProcessor processor, NamedWindowLifecycleEvent.LifecycleEventType eventType, Object... params)
    {
        this.name = name;
        this.processor = processor;
        this.eventType = eventType;
        this.params = params;
    }

    /**
     * Returns the named window name.
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Return the processor originating the event.
     * @return processor
     */
    public NamedWindowProcessor getProcessor()
    {
        return processor;
    }

    /**
     * Returns the event type.
     * @return type of event
     */
    public NamedWindowLifecycleEvent.LifecycleEventType getEventType() {
        return eventType;
    }

    /**
     * Returns event parameters.
     * @return params
     */
    public Object[] getParams() {
        return params;
    }
}

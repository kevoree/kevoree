package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Copy method for underlying events.
 */
public class WrapperEventBeanUndCopyMethod implements EventBeanCopyMethod
{
    private final WrapperEventType wrapperEventType;
    private final EventAdapterService eventAdapterService;
    private final EventBeanCopyMethod underlyingCopyMethod;

    /**
     * Ctor.
     * @param wrapperEventType wrapper type
     * @param eventAdapterService for creating events
     * @param underlyingCopyMethod for copying the underlying event
     */
    public WrapperEventBeanUndCopyMethod(WrapperEventType wrapperEventType, EventAdapterService eventAdapterService, EventBeanCopyMethod underlyingCopyMethod)
    {
        this.wrapperEventType = wrapperEventType;
        this.eventAdapterService = eventAdapterService;
        this.underlyingCopyMethod = underlyingCopyMethod;
    }

    public EventBean copy(EventBean event)
    {
        DecoratingEventBean decorated = (DecoratingEventBean) event;
        EventBean decoratedUnderlying = decorated.getUnderlyingEvent();
        EventBean copiedUnderlying = underlyingCopyMethod.copy(decoratedUnderlying);
        if (copiedUnderlying == null)
        {
            return null;
        }
        return eventAdapterService.adaptorForTypedWrapper(copiedUnderlying, decorated.getDecoratingProperties(), wrapperEventType);        
    }
}

package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Copy method for wrapper events.
 */
public class WrapperEventBeanCopyMethod implements EventBeanCopyMethod
{
    private final WrapperEventType wrapperEventType;
    private final EventAdapterService eventAdapterService;
    private final EventBeanCopyMethod underlyingCopyMethod;

    /**
     * Ctor.
     * @param wrapperEventType wrapper type
     * @param eventAdapterService event adapter creation
     * @param underlyingCopyMethod copy method for the underlying event
     */
    public WrapperEventBeanCopyMethod(WrapperEventType wrapperEventType, EventAdapterService eventAdapterService, EventBeanCopyMethod underlyingCopyMethod)
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
        Map<String, Object> copiedMap = new HashMap<String, Object>(decorated.getDecoratingProperties());
        return eventAdapterService.adaptorForTypedWrapper(copiedUnderlying, copiedMap, wrapperEventType);        
    }
}

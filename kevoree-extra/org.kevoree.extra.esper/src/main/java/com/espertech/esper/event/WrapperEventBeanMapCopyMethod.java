package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

import java.util.Map;
import java.util.HashMap;

/**
 * Copy method for wrapper events.
 */
public class WrapperEventBeanMapCopyMethod implements EventBeanCopyMethod
{
    private final WrapperEventType wrapperEventType;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param wrapperEventType wrapper type
     * @param eventAdapterService event adapter
     */
    public WrapperEventBeanMapCopyMethod(WrapperEventType wrapperEventType, EventAdapterService eventAdapterService)
    {
        this.wrapperEventType = wrapperEventType;
        this.eventAdapterService = eventAdapterService;
    }

    public EventBean copy(EventBean event)
    {
        DecoratingEventBean decorated = (DecoratingEventBean) event;
        EventBean decoratedUnderlying = decorated.getUnderlyingEvent();
        Map<String, Object> copiedMap = new HashMap<String, Object>(decorated.getDecoratingProperties());
        return eventAdapterService.adaptorForTypedWrapper(decoratedUnderlying, copiedMap, wrapperEventType);        
    }
}

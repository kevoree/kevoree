/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventSender;
import com.espertech.esper.core.EPRuntimeEventSender;
import com.espertech.esper.core.thread.InboundUnitSendWrapped;
import com.espertech.esper.core.thread.ThreadingOption;
import com.espertech.esper.core.thread.ThreadingService;
import com.espertech.esper.event.map.MapEventType;

import java.util.Map;

/**
 * Event sender for map-backed events.
 * <p>
 * Allows sending only event objects of type map, does not check map contents. Any other event object generates an error.
 */
public class EventSenderMap implements EventSender
{
    private final EPRuntimeEventSender runtimeEventSender;
    private final EventAdapterService eventAdapterService;
    private final MapEventType mapEventType;
    private final ThreadingService threadingService;

    /**
     * Ctor.
     * @param runtimeEventSender for processing events
     * @param mapEventType the event type
     * @param threadingService for inbound threading
     * @param eventAdapterService for event bean creation
     */
    public EventSenderMap(EPRuntimeEventSender runtimeEventSender, MapEventType mapEventType, EventAdapterService eventAdapterService, ThreadingService threadingService)
    {
        this.runtimeEventSender = runtimeEventSender;
        this.mapEventType = mapEventType;
        this.threadingService = threadingService;
        this.eventAdapterService = eventAdapterService;
    }

    public void sendEvent(Object event)
    {
        if (!(event instanceof Map))
        {
            throw new EPException("Unexpected event object of type " + event.getClass().getName() + ", expected " + Map.class.getName());
        }
        
        Map<String, Object> map = (Map<String, Object>) event;
        EventBean mapEvent = eventAdapterService.adaptorForTypedMap(map, mapEventType);

        if ((ThreadingOption.isThreadingEnabled) && (threadingService.isInboundThreading()))
        {
            threadingService.submitInbound(new InboundUnitSendWrapped(mapEvent, runtimeEventSender));
        }
        else
        {
            runtimeEventSender.processWrappedEvent(mapEvent);
        }
    }

    public void route(Object event)
    {
        if (!(event instanceof Map))
        {
            throw new EPException("Unexpected event object of type " + event.getClass().getName() + ", expected " + Map.class.getName());
        }
        Map<String, Object> map = (Map<String, Object>) event;
        EventBean mapEvent = eventAdapterService.adaptorForTypedMap(map, mapEventType);
        runtimeEventSender.routeEventBean(mapEvent);
    }
}

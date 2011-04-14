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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Event sender for use with plug-in event representations.
 * <p>
 * The implementation asks a list of event bean factoryies originating from plug-in event representations
 * to each reflect on the event and generate an event bean. The first one to return an event bean
 * wins.
 */
public class EventSenderImpl implements EventSender
{
    private static Log log = LogFactory.getLog(EventSenderImpl.class);
    private final List<EventSenderURIDesc> handlingFactories;
    private final EPRuntimeEventSender epRuntime;
    private final ThreadingService threadingService;

    /**
     * Ctor.
     * @param handlingFactories list of factories
     * @param epRuntime the runtime to use to process the event
     * @param threadingService for inbound threading
     */
    public EventSenderImpl(List<EventSenderURIDesc> handlingFactories, EPRuntimeEventSender epRuntime, ThreadingService threadingService)
    {
        this.handlingFactories = handlingFactories;
        this.epRuntime = epRuntime;
        this.threadingService = threadingService;
    }

    public void sendEvent(Object event) throws EPException
    {
        sendIn(event, false);
    }

    public void route(Object event) throws EPException
    {
        sendIn(event, true);
    }

    private void sendIn(Object event, boolean isRoute) throws EPException
    {
        // Ask each factory in turn to take care of it
        for (EventSenderURIDesc entry : handlingFactories)
        {
            EventBean eventBean = null;

            try
            {
                eventBean = entry.getBeanFactory().create(event, entry.getResolutionURI());
            }
            catch (RuntimeException ex)
            {
                log.warn("Unexpected exception thrown by plug-in event bean factory '" + entry.getBeanFactory() + "' processing event " + event, ex);
            }

            if (eventBean != null)
            {
                if (isRoute)
                {
                    epRuntime.routeEventBean(eventBean);
                }
                else
                {
                    if ((ThreadingOption.isThreadingEnabled) && (threadingService.isInboundThreading()))
                    {
                        threadingService.submitInbound(new InboundUnitSendWrapped(eventBean, epRuntime));
                    }
                    else
                    {
                        epRuntime.processWrappedEvent(eventBean);
                    }
                }
                return;
            }
        }
    }
}

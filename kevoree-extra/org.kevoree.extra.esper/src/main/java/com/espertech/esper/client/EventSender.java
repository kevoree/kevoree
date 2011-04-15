/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

/**
 * Returns a facility to process event objects that are of a known type.
 * <p>
 * Obtained via the method {@link EPRuntime#getEventSender(String)} the sender is specific to a given
 * event type and may not process event objects of any other event type; See the method documentatiom for more details.
 * <p>
 * Obtained via the method {@link EPRuntime#getEventSender(java.net.URI[])} the sender
 * cooperates with plug-in event representations to reflect upon the event object to determine an appropriate event type
 * to process the event.
 */
public interface EventSender
{
    /**
     * Processes the event object.
     * <p>
     * Use the route method for sending events into the runtime from within UpdateListener code.
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     * @param event to process
     * @throws EPException if a runtime error occured.
     */
    public void sendEvent(Object event) throws EPException;

    /**
     * Route the event object back to the event stream processing runtime for internal dispatching,
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     * The route event is processed just like it was sent to the runtime, that is any
     * active expressions seeking that event receive it. The routed event has priority over other
     * events sent to the runtime. In a single-threaded application the routed event is
     * processed before the next event is sent to the runtime through the
     * EPRuntime.sendEvent method.
     * @param event to process
     * @throws EPException is thrown when the processing of the event lead to an error
     */
    public void route(Object event) throws EPException;
}

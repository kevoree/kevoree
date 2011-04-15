/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

import java.util.Map;

/**
 * Runtime interface for the isolated service provider, for controlling event visibility and scheduling
 * for the statements contained within the isolated service.
 */
public interface EPRuntimeIsolated
{
    /**
     * Send an event represented by a plain Java object to the event stream processing runtime.
     * <p>
     * Use the route method for sending events into the runtime from within UpdateListener code,
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     *
     * @param object is the event to sent to the runtime
     * @throws com.espertech.esper.client.EPException is thrown when the processing of the event lead to an error
     */
    public void sendEvent(Object object) throws EPException;

    /**
     * Send a map containing event property values to the event stream processing runtime.
     * <p>
     * Use the route method for sending events into the runtime from within UpdateListener code.
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     *
     * @param map - map that contains event property values. Keys are expected to be of type String while values
     * can be of any type. Keys and values should match those declared via Configuration for the given eventTypeName.
     * @param eventTypeName - the name for the Map event type that was previously configured
     * @throws com.espertech.esper.client.EPException - when the processing of the event leads to an error
     */
    public void sendEvent(Map map, String eventTypeName) throws EPException;

    /**
     * Send an event represented by a DOM node to the event stream processing runtime.
     * <p>
     * Use the route method for sending events into the runtime from within UpdateListener code.
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     *
     * @param node is the DOM node as an event
     * @throws com.espertech.esper.client.EPException is thrown when the processing of the event lead to an error
     */
    public void sendEvent(org.w3c.dom.Node node) throws EPException;

    /**
     * Returns current engine time.
     * <p>
     * If time is provided externally via timer events, the function returns current time as externally provided.
     * @return current engine time
     */
    public long getCurrentTime();

    /**
     * Returns the time at which the next schedule execution is expected, returns null if no schedule execution is
     * outstanding.
     * @return time of next schedule if any
     */
    public Long getNextScheduledTime();
}
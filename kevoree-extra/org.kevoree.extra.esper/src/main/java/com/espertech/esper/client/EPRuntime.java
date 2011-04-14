/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

import com.espertech.esper.client.util.EventRenderer;

import java.util.Map;
import java.util.Set;
import java.net.URI;

/**
 * Interface to event stream processing runtime services.
 */
public interface EPRuntime
{
    /**
     * Send an event represented by a plain Java object to the event stream processing runtime.
     * <p>
     * Use the route method for sending events into the runtime from within UpdateListener code,
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     *
     * @param object is the event to sent to the runtime
     * @throws EPException is thrown when the processing of the event lead to an error
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
     * @throws EPException - when the processing of the event leads to an error
     */
    public void sendEvent(Map map, String eventTypeName) throws EPException;

    /**
     * Send an event represented by a DOM node to the event stream processing runtime.
     * <p>
     * Use the route method for sending events into the runtime from within UpdateListener code.
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     *
     * @param node is the DOM node as an event
     * @throws EPException is thrown when the processing of the event lead to an error
     */
    public void sendEvent(org.w3c.dom.Node node) throws EPException;

    /**
     * Number of events evaluated over the lifetime of the event stream processing runtime,
     * or since the last resetStats() call.
     * @return number of events received
     */
    public long getNumEventsEvaluated();

    /**
     * Reset number of events received and emitted
     */
    public void resetStats();

    /**
     * Route the event object back to the event stream processing runtime for internal dispatching,
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     * The route event is processed just like it was sent to the runtime, that is any
     * active expressions seeking that event receive it. The routed event has priority over other
     * events sent to the runtime. In a single-threaded application the routed event is
     * processed before the next event is sent to the runtime through the
     * EPRuntime.sendEvent method.
     * @param event to route internally for processing by the event stream processing runtime
     */
    public void route(final Object event);

    /**
     * Route the event object back to the event stream processing runtime for internal dispatching,
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     * The route event is processed just like it was sent to the runtime, that is any
     * active expressions seeking that event receive it. The routed event has priority over other
     * events sent to the runtime. In a single-threaded application the routed event is
     * processed before the next event is sent to the runtime through the
     * EPRuntime.sendEvent method.
     * @param map - map that contains event property values. Keys are expected to be of type String while values
     * can be of any type. Keys and values should match those declared via Configuration for the given eventTypeName.
     * @param eventTypeName - the name for Map event type that was previously configured
     * @throws EPException - when the processing of the event leads to an error
     */
    public void route(Map map, String eventTypeName) throws EPException;

    /**
     * Route the event object back to the event stream processing runtime for internal dispatching,
     * to avoid the possibility of a stack overflow due to nested calls to sendEvent.
     * The route event is processed just like it was sent to the runtime, that is any
     * active expressions seeking that event receive it. The routed event has priority over other
     * events sent to the runtime. In a single-threaded application the routed event is
     * processed before the next event is sent to the runtime through the
     * EPRuntime.sendEvent method.
     * @param node is the DOM node as an event
     * @throws EPException is thrown when the processing of the event lead to an error
     */
    public void route(org.w3c.dom.Node node) throws EPException;

    /**
     * Sets a listener to receive events that are unmatched by any statement.
     * <p>
     * Events that can be unmatched are all events that are send into a runtime via one
     * of the sendEvent methods, or that have been generated via insert-into clause.
     * <p>
     * For an event to be unmatched by any statement, the event must not match any
     * statement's event stream filter criteria (a where-clause is NOT a filter criteria for a stream, as below).
     * <p>
     * Note: In the following statement a MyEvent event does always match
     * this statement's event stream filter criteria, regardless of the value of the 'quantity' property.
     * <pre>select * from MyEvent where quantity > 5</pre>
     * <br>
     * In the following statement only a MyEvent event with a 'quantity' property value of 5 or less does not match
     * this statement's event stream filter criteria:
     * <pre>select * from MyEvent(quantity > 5)</pre>
     * <p>
     * For patterns, if no pattern sub-expression is active for such event, the event is also unmatched.
     * @param listener is the listener to receive notification of unmatched events, or null to unregister a
     * previously registered listener
     */
    public void setUnmatchedListener(UnmatchedListener listener);

    /**
     * Returns the current variable value. A null value is a valid value for a variable.
     * @param variableName is the name of the variable to return the value for
     * @return current variable value
     * @throws VariableNotFoundException if a variable by that name has not been declared
     */
    public Object getVariableValue(String variableName) throws VariableNotFoundException;

    /**
     * Returns current variable values for each of the variable names passed in,
     * guaranteeing consistency in the face of concurrent updates to the variables.
     * @param variableNames is a set of variable names for which to return values
     * @return map of variable name and variable value
     * @throws VariableNotFoundException if any of the variable names has not been declared
     */
    public Map<String, Object> getVariableValue(Set<String> variableNames) throws VariableNotFoundException;

    /**
     * Returns current variable values for all variables,
     * guaranteeing consistency in the face of concurrent updates to the variables.
     * @return map of variable name and variable value
     */
    public Map<String, Object> getVariableValueAll();

    /**
     * Sets the value of a single variable.
     * @param variableName is the name of the variable to change the value of
     * @param variableValue is the new value of the variable, with null an allowed value
     * @throws VariableValueException if the value does not match variable type or cannot be safely coerced
     * to the variable type
     * @throws VariableNotFoundException if the variable name has not been declared
     */
    public void setVariableValue(String variableName, Object variableValue) throws VariableValueException, VariableNotFoundException;

    /**
     * Sets the value of multiple variables in one update, applying all or none of the changes
     * to variable values in one atomic transaction.
     * @param variableValues is the map of variable name and variable value, with null an allowed value
     * @throws VariableValueException if any value does not match variable type or cannot be safely coerced
     * to the variable type
     * @throws VariableNotFoundException if any of the variable names has not been declared
     */
    public void setVariableValue(Map<String, Object> variableValues) throws VariableValueException, VariableNotFoundException;

    /**
     * Returns a facility to process event objects that are of a known type.
     * <p>
     * Given an event type name this method returns a sender that allows to send in
     * event objects of that type. The event objects send in via the event sender
     * are expected to match the event type, thus the event sender does
     * not inspect the event object other then perform basic checking.
     * <p>
     * For events backed by a Java class (JavaBean events), the sender ensures that the
     * object send in matches in class, or implements or extends the class underlying the event type
     * for the given event type name.
     * <p>
     * For events backed by a java.util.Map (Map events), the sender does not perform any checking other
     * then checking that the event object indeed implements Map.
     * <p>
     * For events backed by a org.w3c.Node (XML DOM events), the sender checks that the root element name
     * indeed does match the root element name for the event type name.
     * @param eventTypeName is the name of the event type
     * @return sender for fast-access processing of event objects of known type (and content)
     * @throws EventTypeException thrown to indicate that the name does not exist
     */
    public EventSender getEventSender(String eventTypeName) throws EventTypeException;

    /**
     * For use with plug-in event representations, returns a facility to process event objects that are of one of a number of types
     * that one or more of the registered plug-in event representation extensions can reflect upon and provide an
     * event for.
     * @param uris is the URIs that specify which plug-in event representations may process an event object.
     * <p>URIs do not need to match event representation URIs exactly, a child (hierarchical) match is enough
     * for an event representation to participate.
     * <p>The order of URIs is relevant as each event representation's factory is asked in turn to
     * process the event, until the first factory processes the event.
     * @return sender for processing of event objects of one of the plug-in event representations
     * @throws EventTypeException thrown to indicate that the URI list was invalid
     */
    public EventSender getEventSender(URI[] uris) throws EventTypeException;

    /**
     * Execute an on-demand query.
     * <p>
     * On-demand queries are EPL queries that execute non-continuous fire-and-forget queries against named windows.
     * @param epl is the EPL to execute
     * @return query result
     */
    public EPOnDemandQueryResult executeQuery(String epl);

    /**
     * Prepare an on-demand query before execution and for repeated execution.
     * @param epl to prepare
     * @return proxy to execute upon, that also provides the event type of the returned results
     */
    public EPOnDemandPreparedQuery prepareQuery(String epl);

    /**
     * Returns the event renderer for events generated by this runtime.
     * @return event renderer
     */
    public EventRenderer getEventRenderer();

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

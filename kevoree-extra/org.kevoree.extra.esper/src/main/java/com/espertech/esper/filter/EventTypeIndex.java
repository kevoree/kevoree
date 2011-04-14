/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Mapping of event type to a tree-like structure
 * containing filter parameter constants in indexes {@link FilterParamIndexBase} and filter callbacks in {@link FilterHandleSetNode}.
 * <p>
 * This class evaluates events for the purpose of filtering by (1) looking up the event's {@link EventType}
 * and (2) asking the subtree for this event type to evaluate the event.
 * <p>
 * The class performs all the locking required for multithreaded access.
 */
public class EventTypeIndex implements EventEvaluator
{
    private Map<EventType, FilterHandleSetNode> eventTypes;
    private ReadWriteLock eventTypesRWLock;

    /**
     * Constructor.
     */
    public EventTypeIndex()
    {
        eventTypes = new HashMap<EventType, FilterHandleSetNode>();
        eventTypesRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Destroy the service.
     */
    public void destroy()
    {
        eventTypes.clear();
    }

    /**
     * Add a new event type to the index and use the specified node for the root node of its subtree.
     * If the event type already existed, the method will throw an IllegalStateException.
     * @param eventType is the event type to be added to the index
     * @param rootNode is the root node of the subtree for filter constant indizes and callbacks
     */
    public void add(EventType eventType, FilterHandleSetNode rootNode)
    {
        eventTypesRWLock.writeLock().lock();
        try
        {
            if (eventTypes.containsKey(eventType))
            {
                throw new IllegalStateException("Event type already in index, add not performed, type=" + eventType);
            }
            eventTypes.put(eventType, rootNode);
        }
        finally
        {
            eventTypesRWLock.writeLock().unlock();
        }
    }

    /**
     * Returns the root node for the given event type, or null if this event type has not been seen before.
     * @param eventType is an event type
     * @return the subtree's root node
     */
    public FilterHandleSetNode get(EventType eventType)
    {
        eventTypesRWLock.readLock().lock();
        FilterHandleSetNode result = eventTypes.get(eventType);
        eventTypesRWLock.readLock().unlock();

        return result;
    }

    public void matchEvent(EventBean event, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".matchEvent Event received for matching, event=" + event);
        }

        EventType eventType = event.getEventType();

        // Attempt to match exact type
        matchType(eventType, event, matches, exprEvaluatorContext);

        // No supertype means we are done
        if (eventType.getSuperTypes() == null)
        {
            return;
        }

        for (Iterator<EventType> it = eventType.getDeepSuperTypes(); it.hasNext();)
        {
            EventType superType = it.next();
            matchType(superType, event, matches, exprEvaluatorContext);
        }
    }

    /**
     * Returns the current size of the known event types.
     * @return collection size
     */
    protected int size()
    {
        return eventTypes.size();
    }

    private void matchType(EventType eventType, EventBean eventBean, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext)
    {
        eventTypesRWLock.readLock().lock();
        FilterHandleSetNode rootNode = null;
        try
        {
            rootNode = eventTypes.get(eventType);
        }
        finally
        {
            eventTypesRWLock.readLock().unlock();
        }

        // If the top class node is null, no filters have yet been registered for this event type.
        // In this case, log a message and done.
        if (rootNode == null)
        {
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                String message = "Event type is not known to the filter service, eventType=" + eventType;
                log.debug(".matchEvent " + message);
            }
            return;
        }

        rootNode.matchEvent(eventBean, matches, exprEvaluatorContext);
    }

    private static final Log log = LogFactory.getLog(EventTypeIndex.class);
}

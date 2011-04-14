/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * This class holds a list of indizes storing filter constants in {@link FilterParamIndexBase} nodes
 * and a set of {@link FilterHandle}.
 * An instance of this class represents a leaf-node (no indizes stored, just filter callbacks)
 * but can also be non-leaf (some indizes exist) in a filter evaluation tree.
 * Events are evaluated by asking each of the indizes to evaluate the event and by
 * adding any filter callbacks in this node to the "matches" list of callbacks.
 */
public final class FilterHandleSetNode implements EventEvaluator
{
    private final Set<FilterHandle> callbackSet;
    private final List<FilterParamIndexBase> indizes;
    private final ReadWriteLock nodeRWLock;

    /**
     * Constructor.
     */
    public FilterHandleSetNode()
    {
        callbackSet = new LinkedHashSet<FilterHandle>();
        indizes = new LinkedList<FilterParamIndexBase>();
        nodeRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Returns an indication of whether there are any callbacks or index nodes at all in this set.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @return true if there are neither indizes nor filter callbacks stored, false if either exist.
     */
    protected boolean isEmpty()
    {
        return callbackSet.isEmpty() && indizes.isEmpty();
    }

    /**
     * Returns the number of filter callbacks stored.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @return number of filter callbacks stored
     */
    protected int getFilterCallbackCount()
    {
        return callbackSet.size();
    }

    /**
     * Returns to lock to use for making changes to the filter callback or inzides collections stored by this node.
     * @return lock to use in multithreaded environment
     */
    protected final ReadWriteLock getNodeRWLock()
    {
        return nodeRWLock;
    }

    /**
     * Returns list of indexes - not returning an iterator. Client classes should not change this collection.
     * @return list of indizes
     */
    public List<FilterParamIndexBase> getIndizes()
    {
        return indizes;
    }

    /**
     * Evaluate an event by asking each index to match the event. Any filter callbacks at this node automatically
     * match the event and do not need to be further evaluated, and are thus added to the "matches" list of callbacks.
     * NOTE: This client should not use the lock before calling this method.
     * @param eventBean is the event wrapper supplying the event property values
     * @param matches is the list of callbacks to add to for any matches found
     */
    public final void matchEvent(EventBean eventBean, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext)
    {
        nodeRWLock.readLock().lock();

        // Ask each of the indizes to match against the attribute values
        for (FilterParamIndexBase index : indizes)
        {
            index.matchEvent(eventBean, matches, exprEvaluatorContext);
        }

        // Add each filter callback stored in this node to the matching list
        for (FilterHandle filterCallback : callbackSet)
        {
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug(".match (" + Thread.currentThread().getId() + ") Found a match, filterCallbackHash=" + filterCallback.hashCode() +
                        "  me=" + this +
                        "  filterCallback=" + filterCallback);
            }

            matches.add(filterCallback);
        }

        nodeRWLock.readLock().unlock();
    }

    /**
     * Returns an indication whether the filter callback exists in this node.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @param filterCallback is the filter callback to check for
     * @return true if callback found, false if not
     */
    protected boolean contains(FilterHandle filterCallback)
    {
        return callbackSet.contains(filterCallback);
    }

    /**
     * Add an index. The same index can be added twice - there is no checking done.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @param index - index to add
     */
    protected final void add(FilterParamIndexBase index)
    {
        indizes.add(index);
    }

    /**
     * Remove an index, returning true if it was found and removed or false if not in collection.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @param index is the index to remove
     * @return true if found, false if not existing
     */
    protected final boolean remove(FilterParamIndexBase index)
    {
        return indizes.remove(index);
    }

    /**
     * Add a filter callback. The filter callback set allows adding the same callback twice with no effect.
     * If a client to the class needs to check that the callback already existed, the contains method does that.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @param filterCallback is the callback to add
     */
    protected final void add(FilterHandle filterCallback)
    {
        callbackSet.add(filterCallback);
    }

    /**
     * Remove a filter callback, returning true if it was found and removed or false if not in collection.
     * NOTE: the client to this method must use the read-write lock of this object to lock, if required by the client
     * code.
     * @param filterCallback is the callback to remove
     * @return true if found, false if not existing
     */
    protected final boolean remove(FilterHandle filterCallback)
    {
        return callbackSet.remove(filterCallback);
    }

    private static final Log log = LogFactory.getLog(FilterHandleSetNode.class);
}

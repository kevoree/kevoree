/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.Pair;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for changes to {@link EventTypeIndex} for addition and removal of filters.
 * It delegates the work to make modifications to the filter parameter tree to an {@link IndexTreeBuilder}.
 * It enforces a policy that a filter callback can only be added once.
 */
public class EventTypeIndexBuilder
{
    private final Map<FilterHandle, Pair<FilterValueSet, IndexTreePath>> callbacks;
    private final Lock callbacksLock;
    private final EventTypeIndex eventTypeIndex;

    /**
     * Constructor - takes the event type index to manipulate as its parameter.
     * @param eventTypeIndex - index to manipulate
     */
    public EventTypeIndexBuilder(EventTypeIndex eventTypeIndex)
    {
        this.eventTypeIndex = eventTypeIndex;

        this.callbacks = new HashMap<FilterHandle, Pair<FilterValueSet, IndexTreePath>>();
        this.callbacksLock = new ReentrantLock();
    }

    /**
     * Destroy the service.
     */
    public void destroy()
    {
        callbacks.clear();
    }

    /**
     * Add a filter to the event type index structure, and to the filter subtree.
     * Throws an IllegalStateException exception if the callback is already registered.
     * @param filterValueSet is the filter information
     * @param filterCallback is the callback
     */
    public final void add(FilterValueSet filterValueSet, FilterHandle filterCallback)
    {
        EventType eventType = filterValueSet.getEventType();

        // Check if a filter tree exists for this event type
        FilterHandleSetNode rootNode = eventTypeIndex.get(eventType);

        // Make sure we have a root node
        if (rootNode == null)
        {
            callbacksLock.lock();
            try
            {
                rootNode = eventTypeIndex.get(eventType);
                if (rootNode == null)
                {
                    rootNode = new FilterHandleSetNode();
                    eventTypeIndex.add(eventType, rootNode);
                }
            }
            finally
            {
                callbacksLock.unlock();
            }
        }

        // Make sure the filter callback doesn't already exist
        callbacksLock.lock();
        try
        {
            if (callbacks.containsKey(filterCallback))
            {
                throw new IllegalStateException("Callback for filter specification already exists in collection");
            }
        }
        finally
        {
            callbacksLock.unlock();
        }

        // Now add to tree
        IndexTreeBuilder treeBuilder = new IndexTreeBuilder();
        IndexTreePath path = treeBuilder.add(filterValueSet, filterCallback, rootNode);

        callbacksLock.lock();
        try
        {
            callbacks.put(filterCallback, new Pair<FilterValueSet, IndexTreePath>(filterValueSet, path));
        }
        finally
        {
            callbacksLock.unlock();
        }
    }

    /**
     * Remove a filter callback from the given index node.
     * @param filterCallback is the callback to remove
     */
    public final void remove(FilterHandle filterCallback)
    {
        Pair<FilterValueSet, IndexTreePath> pair = null;
        callbacksLock.lock();
        try
        {
            pair = callbacks.get(filterCallback);
        }
        finally
        {
            callbacksLock.unlock();
        }

        if (pair == null)
        {
            throw new IllegalArgumentException("Filter callback to be removed not found");
        }

        FilterHandleSetNode rootNode = eventTypeIndex.get(pair.getFirst().getEventType());

        // Now remove from tree
        IndexTreeBuilder treeBuilder = new IndexTreeBuilder();
        treeBuilder.remove(filterCallback, pair.getSecond(), rootNode);

        // Remove from callbacks list
        callbacksLock.lock();
        try
        {
            callbacks.remove(filterCallback);
        }
        finally
        {
            callbacksLock.unlock();
        }
    }

    /**
     * Returns filters for the statement ids.
     * @param statementIds ids to take
     * @return set of filters for taken statements
     */
    public final FilterSet take(Set<String> statementIds)
    {
        List<FilterSetEntry> list = new ArrayList<FilterSetEntry>();
        callbacksLock.lock();
        try
        {
            for (Map.Entry<FilterHandle, Pair<FilterValueSet, IndexTreePath>> entry : callbacks.entrySet())
            {
                Pair<FilterValueSet, IndexTreePath> pair = entry.getValue();
                if (statementIds.contains(entry.getKey().getStatementId()))
                {
                    list.add(new FilterSetEntry(entry.getKey(), pair.getFirst()));

                    FilterHandleSetNode rootNode = eventTypeIndex.get(pair.getFirst().getEventType());

                    // Now remove from tree
                    IndexTreeBuilder treeBuilder = new IndexTreeBuilder();
                    treeBuilder.remove(entry.getKey(), pair.getSecond(), rootNode);
                }
            }
            
            for (FilterSetEntry removed : list)
            {
                callbacks.remove(removed.getHandle());
            }
        }
        finally
        {
            callbacksLock.unlock();
        }

        return new FilterSet(list);
    }

    /**
     * Add the filters, from previously-taken filters.
     * @param filterSet to add
     */
    public void apply(FilterSet filterSet)
    {
        for (FilterSetEntry entry : filterSet.getFilters())
        {
            add(entry.getFilterValueSet(), entry.getHandle());
        }
    }
}

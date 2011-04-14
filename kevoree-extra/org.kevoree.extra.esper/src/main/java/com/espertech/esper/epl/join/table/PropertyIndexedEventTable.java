/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.table;

import java.util.*;

import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.util.ExecutionPathDebugLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Index that organizes events by the event property values into hash buckets. Based on a HashMap
 * with {@link com.espertech.esper.collection.MultiKeyUntyped} keys that store the property values.
 *
 * Takes a list of property names as parameter. Doesn't care which event type the events have as long as the properties
 * exist. If the same event is added twice, the class throws an exception on add.
 */
public class PropertyIndexedEventTable implements EventTable
{
    private final int streamNum;
    private final String[] propertyNames;
    private final Class[] propertyCoercedTypes;

    /**
     * Getters for properties.
     */
    protected final EventPropertyGetter[] propertyGetters;

    /**
     * Index table.
     */
    protected final Map<MultiKeyUntyped, Set<EventBean>> propertyIndex;

    /**
     * Ctor.
     * @param streamNum - the stream number that is indexed
     * @param eventType - types of events indexed
     * @param propertyNames - property names to use for indexing
     * @param propertyCoercedTypes - property types
     */
    public PropertyIndexedEventTable(int streamNum, EventType eventType, String[] propertyNames, Class[] propertyCoercedTypes)
    {
        this.streamNum = streamNum;
        this.propertyNames = propertyNames;
        this.propertyCoercedTypes = propertyCoercedTypes;

        // Init getters
        propertyGetters = new EventPropertyGetter[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++)
        {
            propertyGetters[i] = eventType.getGetter(propertyNames[i]);
        }

        propertyIndex = new HashMap<MultiKeyUntyped, Set<EventBean>>();
    }

    /**
     * Determine multikey for index access.
     * @param event to get properties from for key
     * @return multi key
     */
    protected MultiKeyUntyped getMultiKey(EventBean event)
    {
        return EventBeanUtility.getMultiKey(event, propertyGetters);
    }

    /**
     * Add an array of events. Same event instance is not added twice. Event properties should be immutable.
     * Allow null passed instead of an empty array.
     * @param events to add
     * @throws IllegalArgumentException if the event was already existed in the index
     */
    public void add(EventBean[] events)
    {
        if (events == null)
        {
            return;
        }
        for (EventBean event : events)
        {
            add(event);
        }
    }

    /**
     * Remove events.
     * @param events to be removed, can be null instead of an empty array.
     * @throws IllegalArgumentException when the event could not be removed as its not in the index
     */
    public void remove(EventBean[] events)
    {
        if (events == null)
        {
            return;
        }
        for (EventBean event : events)
        {
            remove(event);
        }
    }

    /**
     * Returns the set of events that have the same property value as the given event.
     * @param keys to compare against
     * @return set of events with property value, or null if none found (never returns zero-sized set)
     */
    public Set<EventBean> lookup(Object[] keys)
    {
        MultiKeyUntyped key = new MultiKeyUntyped(keys);
        return propertyIndex.get(key);
    }

    private void add(EventBean event)
    {
        MultiKeyUntyped key = getMultiKey(event);

        Set<EventBean> events = propertyIndex.get(key);
        if (events == null)
        {
            events = new HashSet<EventBean>();
            propertyIndex.put(key, events);
        }

        if (events.contains(event))
        {
            throw new IllegalArgumentException("Event already in index, event=" + event);
        }

        events.add(event);
    }

    private void remove(EventBean event)
    {
        MultiKeyUntyped key = getMultiKey(event);

        Set<EventBean> events = propertyIndex.get(key);
        if (events == null)
        {
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug(".remove Event could not be located in index, event " + event);
            }

            return;
        }

        if (!events.remove(event))
        {
            // Not an error, its possible that an old-data event is artificial (such as for statistics) and
            // thus did not correspond to a new-data event raised earlier.
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug(".remove Event could not be located in index, event " + event);
            }
            return;
        }

        if (events.isEmpty())
        {
            propertyIndex.remove(key);
        }
    }

    public boolean isEmpty()
    {
        return propertyIndex.isEmpty();
    }

    public Iterator<EventBean> iterator()
    {
        return new PropertyIndexedEventTableIterator(propertyIndex);
    }

    public void clear()
    {
        propertyIndex.clear();
    }

    /**
     * Returns index property names.
     * @return property names
     */
    public String[] getPropertyNames() {
        return propertyNames;
    }

    /**
     * Returns property types.
     * @return types
     */
    public Class[] getPropertyCoercedTypes()
    {
        return propertyCoercedTypes;
    }

    public String toString()
    {
        return "PropertyIndexedEventTable" +
                " streamNum=" + streamNum +
                " propertyNames=" + Arrays.toString(propertyNames);
    }

    private static Log log = LogFactory.getLog(PropertyIndexedEventTable.class);
}

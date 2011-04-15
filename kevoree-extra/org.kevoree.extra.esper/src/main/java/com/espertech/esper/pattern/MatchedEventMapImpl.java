/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.client.EventBean;

import java.util.Map;
import java.util.HashMap;

/**
 * Collection for internal use similar to the MatchedEventMap class in the client package
 * that holds the one or more events that could match any defined event expressions.
 * The optional tag value supplied when an event expression is created is used as a key for placing
 * matching event objects into this collection.
 */
public final class MatchedEventMapImpl implements MatchedEventMap
{
    // Keyed by tag name; Values can be {EventBean, EventBean[]} as metadata is aware
    private Map<String, Object> events = new HashMap<String, Object>();

    /**
     * Constructor creates an empty collection of events.
     */
    public MatchedEventMapImpl()
    {
    }

    /**
     * Ctor.
     * @param events is the name-value pairs of tag and event
     */
    public MatchedEventMapImpl(Map<String, Object> events)
    {
        this.events = events;
    }

    /**
     * Add an event to the collection identified by the given tag.
     * @param tag is an identifier to retrieve the event from
     * @param event is the event object or array of event object to be added
     */
    public void add(final String tag, final Object event)
    {
        events.put(tag, event);
    }

    /**
     * Returns a map containing the events where the key is the event tag string and the value is the event
     * instance.
     * @return Hashtable containing event instances
     */
    public Map<String, Object> getMatchingEvents()
    {
        return events;
    }

    /**
     * Returns a single event instance given the tag identifier, or null if the tag could not be located.
     * @param tag is the identifier to look for
     * @return event instances for the tag
     */
    public EventBean getMatchingEvent(final String tag)
    {
        return (EventBean) events.get(tag);
    }

    public Object getMatchingEventAsObject(final String tag)
    {
        return events.get(tag);
    }

    public boolean equals(final Object otherObject)
    {
        if (otherObject == this)
        {
            return true;
        }

        if (otherObject == null)
        {
            return false;
        }

        if (getClass() != otherObject.getClass())
        {
            return false;
        }

        final MatchedEventMapImpl other = (MatchedEventMapImpl) otherObject;

        if (events.size() != other.events.size())
        {
            return false;
        }

        // Compare entry by entry
        for (Map.Entry<String, Object> entry : events.entrySet())
        {
            final String tag = entry.getKey();
            final Object event = entry.getValue();

            if (other.getMatchingEvent(tag) != event)
            {
                return false;
            }
        }

        return true;
    }

    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        int count = 0;

        for (Map.Entry<String, Object> entry : events.entrySet())
        {
            buffer.append(" (");
            buffer.append(count++);
            buffer.append(") ");
            buffer.append("tag=");
            buffer.append(entry.getKey());
            buffer.append("  event=");
            buffer.append(entry.getValue());
        }

        return buffer.toString();
    }

    public int hashCode()
    {
        return events.hashCode();
    }

    /**
     * Make a shallow copy of this collection.
     * @return shallow copy
     */
    public MatchedEventMapImpl shallowCopy()
    {
        Map<String, Object> copy = new HashMap<String, Object>();
        copy.putAll(events);
        return new MatchedEventMapImpl(copy);
    }

    /**
     * Merge the state of an other match event structure into this one by adding all entries
     * within the MatchedEventMap to this match event.
     * @param other is the other instance to merge in.
     */
    public void merge(final MatchedEventMap other)
    {
        if (!(other instanceof MatchedEventMapImpl))
        {
            throw new UnsupportedOperationException("Merge requires same types");
        }
        MatchedEventMapImpl otherImpl = (MatchedEventMapImpl) other;
        events.putAll(otherImpl.events);
    }
}

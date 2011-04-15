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

import java.util.*;

/**
 * Collection for internal use similar to the MatchedEventMap class in the client package
 * that holds the one or more events that could match any defined event expressions.
 * The optional tag value supplied when an event expression is created is used as a key for placing
 * matching event objects into this collection.
 */
public interface MatchedEventMap
{
    /**
     * Add an event to the collection identified by the given tag.
     * @param tag is an identifier to retrieve the event from
     * @param event is the event object or array of event object to be added
     */
    public void add(final String tag, final Object event);

    /**
     * Returns a map containing the events where the key is the event tag string and the value is the event
     * instance.
     * @return Map containing event instances
     */
    public Map getMatchingEvents();

    /**
     * Returns a single event instance given the tag identifier, or null if the tag could not be located.
     * @param tag is the identifier to look for
     * @return event instances for the tag
     */
    public EventBean getMatchingEvent(final String tag);

    /**
     * Returns the object for the matching event, be it the event bean array or the event bean.
     * @param tag is the tag to return the object for
     * @return event bean or event bean array
     */
    public Object getMatchingEventAsObject(final String tag);

    /**
     * Make a shallow copy of this collection.
     * @return shallow copy
     */
    public MatchedEventMap shallowCopy();

    /**
     * Merge the state of an other match event structure into this one by adding all entries
     * within the MatchedEventMap to this match event.
     * @param other is the other instance to merge in.
     */
    public void merge(final MatchedEventMap other);
}

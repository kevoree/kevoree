/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.client.EventBean;

import java.util.Iterator;

/**
 * Base interface for providing access-aggregations, i.e. aggregations that mirror a data window
 * but group by the group-by clause and that do not mirror the data windows sorting policy.
 */
public interface AggregationAccess
{
    /**
     * Enter an event.
     * @param eventsPerStream all events in all streams, typically implementations pick the relevant stream's events to add
     */
    void applyEnter(EventBean[] eventsPerStream);

    /**
     * Remove an event.
     * @param eventsPerStream all events in all streams, typically implementations pick the relevant stream's events to remove
     */
    void applyLeave(EventBean[] eventsPerStream);

    /**
     * Returns the first (oldest) value entered.
     * @return first value
     */
    public EventBean getFirstValue();

    /**
     * Returns the newest (last) value entered.
     * @return last value
     */
    public EventBean getLastValue();

    /**
     * Counting from the first element to the last, returns the oldest (first) value entered for index zero
     * and the n-th oldest value for index N.
     * @param index index
     * @return last value
     */
    public EventBean getFirstNthValue(int index);

    /**
     * Counting from the last element to the first, returns the newest (last) value entered for index zero
     * and the n-th newest value for index N.
     * @param index index
     * @return last value
     */
    public EventBean getLastNthValue(int index);

    /**
     * Returns all events for the group.
     * @return group event iterator
     */
    public Iterator<EventBean> iterator();

    /**
     * Returns the number of events in the group.
     * @return size
     */
    public int size();

    /**
     * Clear all events in the group.
     */
    public void clear();
}

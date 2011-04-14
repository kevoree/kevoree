/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * A processor for ordering output events according to the order specified in the order-by clause.
 */
public interface OrderByProcessor {

	/**
	 * Sort the output events. If the order-by processor needs group-by
	 * keys to evaluate the expressions in the order-by clause, these will
	 * be computed from the generating events.
	 * @param outgoingEvents - the events to be sorted
	 * @param generatingEvents - the events that generated the output events (each event has a corresponding array of generating events per different event streams)
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param exprEvaluatorContext context for expression evalauation
	 * @return an array containing the output events in sorted order
	 */
	public EventBean[] sort(EventBean[] outgoingEvents, EventBean[][] generatingEvents, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext);

	/**
	 * Sort the output events, using the provided group-by keys for 
	 * evaluating grouped aggregation functions, and avoiding the cost of
	 * recomputing the keys.
	 * @param outgoingEvents - the events to sort
	 * @param generatingEvents - the events that generated the output events (each event has a corresponding array of generating events per different event streams)
	 * @param groupByKeys - the keys to use for determining the group-by group of output events 
     * @param isNewData - indicates whether we are dealing with new data (istream) or old data (rstream)
     * @param exprEvaluatorContext context for expression evalauation
	 * @return an array containing the output events in sorted order
	 */
	public EventBean[] sort(EventBean[] outgoingEvents, EventBean[][] generatingEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext);

    /**
     * Returns the sort key for a given row.
     * @param eventsPerStream is the row consisting of one event per stream
     * @param isNewData is true for new data
     * @param exprEvaluatorContext context for expression evalauation
     * @return sort key
     */
    public MultiKeyUntyped getSortKey(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext);

    /**
     * Returns the sort key for a each row where a row is a single event (no join, single stream).
     * @param generatingEvents is the rows consisting of one event per row
     * @param isNewData is true for new data
     * @param exprEvaluatorContext context for expression evalauation
     * @return sort key for each row
     */
    public MultiKeyUntyped[] getSortKeyPerRow(EventBean[] generatingEvents, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext);

    /**
     * Sort a given array of outgoing events using the sort keys returning a sorted outgoing event array.
     * @param outgoingEvents is the events to sort
     * @param orderKeys is the keys to sort by
     * @param exprEvaluatorContext context for expression evalauation
     * @return sorted events
     */
    public EventBean[] sort(EventBean[] outgoingEvents, MultiKeyUntyped[] orderKeys, ExprEvaluatorContext exprEvaluatorContext);
}

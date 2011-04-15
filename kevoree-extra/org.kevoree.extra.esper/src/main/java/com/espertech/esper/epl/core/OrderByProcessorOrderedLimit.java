/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * Sorter and row limiter in one: sorts using a sorter and row limits
 */
public class OrderByProcessorOrderedLimit implements OrderByProcessor
{
    private final OrderByProcessorImpl orderByProcessor;
    private final OrderByProcessorRowLimit orderByProcessorRowLimit;

    /**
     * Ctor.
     * @param orderByProcessor the sorter
     * @param orderByProcessorRowLimit the row limiter
     */
    public OrderByProcessorOrderedLimit(OrderByProcessorImpl orderByProcessor, OrderByProcessorRowLimit orderByProcessorRowLimit)
    {
        this.orderByProcessor = orderByProcessor;
        this.orderByProcessorRowLimit = orderByProcessorRowLimit;
    }

    public EventBean[] sort(EventBean[] outgoingEvents, EventBean[][] generatingEvents, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean[] sorted = orderByProcessor.sort(outgoingEvents, generatingEvents, isNewData, exprEvaluatorContext);
        return orderByProcessorRowLimit.applyLimit(sorted);
    }

    public EventBean[] sort(EventBean[] outgoingEvents, EventBean[][] generatingEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean[] sorted = orderByProcessor.sort(outgoingEvents, generatingEvents, groupByKeys, isNewData, exprEvaluatorContext);
        return orderByProcessorRowLimit.applyLimit(sorted);
    }

    public MultiKeyUntyped getSortKey(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return orderByProcessor.getSortKey(eventsPerStream, isNewData, exprEvaluatorContext);
    }

    public MultiKeyUntyped[] getSortKeyPerRow(EventBean[] generatingEvents, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return orderByProcessor.getSortKeyPerRow(generatingEvents, isNewData, exprEvaluatorContext);
    }

    public EventBean[] sort(EventBean[] outgoingEvents, MultiKeyUntyped[] orderKeys, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean[] sorted = orderByProcessor.sort(outgoingEvents, orderKeys, exprEvaluatorContext);
        return orderByProcessorRowLimit.applyLimit(sorted);
    }
}

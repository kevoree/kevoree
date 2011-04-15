package com.espertech.esper.epl.agg;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluator;

/**
 * Represents the aggregation accessor that provides the result for the "first" aggregation function without index.
 */
public class AggregationAccessorFirst implements AggregationAccessor
{
    private final int streamNum;
    private final ExprEvaluator childNode;
    private final EventBean[] eventsPerStream;

    /**
     * Ctor.
     * @param streamNum stream id
     * @param childNode expression
     */
    public AggregationAccessorFirst(int streamNum, ExprEvaluator childNode)
    {
        this.streamNum = streamNum;
        this.childNode = childNode;
        this.eventsPerStream = new EventBean[streamNum + 1];
    }

    public Object getValue(AggregationAccess access) {
        EventBean bean = access.getFirstValue();
        if (bean == null) {
            return null;
        }
        eventsPerStream[streamNum] = bean;
        return childNode.evaluate(eventsPerStream, true, null);
    }
}
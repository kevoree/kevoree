package com.espertech.esper.epl.agg;

/**
 * A row in aggregation state.
 */
public class AggregationMethodRow
{
    private long refcount;
    private AggregationMethod[] methods;

    /**
     * Ctor.
     * @param refcount number of items in state
     * @param methods aggregations
     */
    public AggregationMethodRow(long refcount, AggregationMethod[] methods)
    {
        this.refcount = refcount;
        this.methods = methods;
    }

    /**
     * Returns number of data points.
     * @return data points
     */
    public long getRefcount()
    {
        return refcount;
    }

    /**
     * Returns aggregation state.
     * @return state
     */
    public AggregationMethod[] getMethods()
    {
        return methods;
    }

    /**
     * Increase number of data points by one.
     */
    public void increaseRefcount()
    {
        refcount++;
    }

    /**
     * Decrease number of data points by one.
     */
    public void decreaseRefcount()
    {
        refcount--;
    }
}

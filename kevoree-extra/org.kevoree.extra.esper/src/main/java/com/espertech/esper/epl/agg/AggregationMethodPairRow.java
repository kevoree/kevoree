package com.espertech.esper.epl.agg;

/**
 * A row in aggregation state.
 */
public class AggregationMethodPairRow
{
    private long refcount;
    private final AggregationMethod[] methods;
    private final AggregationAccess[] accesses;

    /**
     * Ctor.
     * @param refcount number of items in state
     * @param methods aggregations
     * @param accesses for first/last/window type access
     */
    public AggregationMethodPairRow(long refcount, AggregationMethod[] methods, AggregationAccess[] accesses)
    {
        this.refcount = refcount;
        this.methods = methods;
        this.accesses = accesses;
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

    /**
     * Returns the accesses for first/last/window aggregation functions.
     * @return accesses
     */
    public AggregationAccess[] getAccesses()
    {
        return accesses;
    }
}
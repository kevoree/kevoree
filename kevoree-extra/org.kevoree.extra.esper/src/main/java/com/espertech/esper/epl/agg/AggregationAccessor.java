package com.espertech.esper.epl.agg;

/**
 * Accessor for first/last/window access aggregation functions.
 */
public interface AggregationAccessor
{
    /**
     * Returns the value for a first/last/window access aggregation function.
     * @param access access
     * @return value
     */
    public Object getValue(AggregationAccess access);
}

package com.espertech.esper.epl.agg;

/**
 * Pair of aggregation methods and accesses (first/last/window) data window representations.
 */
public class AggregationRowPair
{
    private final AggregationMethod[] methods;
    private final AggregationAccess[] accesses;

    /**
     * Ctor.
     * @param methods aggregation methods/state
     * @param accesses access is data window representations
     */
    public AggregationRowPair(AggregationMethod[] methods, AggregationAccess[] accesses)
    {
        this.methods = methods;
        this.accesses = accesses;
    }

    /**
     * Returns aggregation methods.
     * @return aggregation methods
     */
    public AggregationMethod[] getMethods()
    {
        return methods;
    }

    /**
     * Returns accesses to data window state.
     * @return accesses
     */
    public AggregationAccess[] getAccesses()
    {
        return accesses;
    }
}

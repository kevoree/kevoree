package com.espertech.esper.epl.agg;

/**
 * For handling access aggregation functions "first, last, window" a pair of slow and accessor.
 */
public class AggregationAccessorSlotPair
{
    private final int slot;
    private final AggregationAccessor accessor;

    /**
     * Ctor.
     * @param slot number of accessor
     * @param accessor accessor
     */
    public AggregationAccessorSlotPair(int slot, AggregationAccessor accessor)
    {
        this.slot = slot;
        this.accessor = accessor;
    }

    /**
     * Returns the slot.
     * @return slow
     */
    public int getSlot()
    {
        return slot;
    }

    /**
     * Returns the accessor.
     * @return accessor
     */
    public AggregationAccessor getAccessor()
    {
        return accessor;
    }
}

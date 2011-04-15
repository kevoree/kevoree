package com.espertech.esper.client.hook;

/**
 * Indicates that the followed-by pattern operator, when parameterized with a max number of sub-expressions,
 * has reached that limit at runtime.
 */
public class ConditionPatternSubexpressionMax implements BaseCondition
{
    private final int max;

    /**
     * Ctor.
     * @param max limit reached
     */
    public ConditionPatternSubexpressionMax(int max) {
        this.max = max;
    }

    /**
     * Returns the limit reached.
     * @return limit
     */
    public int getMax() {
        return max;
    }
}

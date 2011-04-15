package com.espertech.esper.client.soda;

/**
 * Pattern precendences.
 */
public enum PatternExprPrecedenceEnum {

    /**
     * Precedence.
     */
    MAXIMIM(Integer.MAX_VALUE),

    /**
     * Precedence.
     */
    ATOM(7),
    /**
     * Precedence.
     */
    GUARD(6),
    /**
     * Precedence.
     */
    EVERY_NOT(5),
    /**
     * Precedence.
     */
    MATCH_UNTIL(4),
    /**
     * Precedence.
     */
    AND(3),
    /**
     * Precedence.
     */
    OR(2),
    /**
     * Precedence.
     */
    FOLLOWED_BY(1),

    /**
     * Precedence.
     */
    MINIMUM(Integer.MIN_VALUE);

    private final int level;

    private PatternExprPrecedenceEnum(int level) {
        this.level = level;
    }

    /**
     * Returns precedence.
     * @return precedence
     */
    public int getLevel() {
        return level;
    }
}

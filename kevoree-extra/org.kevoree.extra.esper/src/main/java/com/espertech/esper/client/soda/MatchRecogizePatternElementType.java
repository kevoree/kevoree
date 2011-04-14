package com.espertech.esper.client.soda;

/**
 * Enum for match recognize pattern atom types.
 */
public enum MatchRecogizePatternElementType 
{
    /**
     * For single multiplicity.
     */
    SINGLE(""),

    /**
     * For greedy '*' multiplicity.
     */
    ZERO_TO_MANY("*"),

    /**
     * For greedy '+' multiplicity.
     */
    ONE_TO_MANY("+"),

    /**
     * For greedy '?' multiplicity.
     */
    ONE_OPTIONAL("?"),

    /**
     * For reluctant '*' multiplicity.
     */
    ZERO_TO_MANY_RELUCTANT("*?"),

    /**
     * For reluctant '+' multiplicity.
     */
    ONE_TO_MANY_RELUCTANT("+?"),

    /**
     * For reluctant '?' multiplicity.
     */
    ONE_OPTIONAL_RELUCTANT("??");

    private String text;

    MatchRecogizePatternElementType(String text) {
        this.text = text;
    }

    /**
     * Returns the multiplicity text.
     * @return text
     */
    public String getText() {
        return text;
    }
}

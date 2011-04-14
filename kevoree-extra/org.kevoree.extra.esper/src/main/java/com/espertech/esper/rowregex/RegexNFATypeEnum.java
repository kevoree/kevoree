package com.espertech.esper.rowregex;

/**
 * Enum for NFA types.
 */
public enum RegexNFATypeEnum
{
    /**
     * For single multiplicity.
     */
    SINGLE(false, false, null),

    /**
     * For greedy '*' multiplicity.
     */
    ZERO_TO_MANY(true, true, true),

    /**
     * For greedy '+' multiplicity.
     */
    ONE_TO_MANY(true, false, true),

    /**
     * For greedy '?' multiplicity.
     */
    ONE_OPTIONAL(false, true, true),

    /**
     * For reluctant '*' multiplicity.
     */
    ZERO_TO_MANY_RELUCTANT(true, true, false),

    /**
     * For reluctant '+' multiplicity.
     */
    ONE_TO_MANY_RELUCTANT(true, false, false),

    /**
     * For reluctant '?' multiplicity.
     */
    ONE_OPTIONAL_RELUCTANT(false, true, false);

    private boolean multipleMatches;
    private boolean optional;
    private Boolean greedy;

    private RegexNFATypeEnum(boolean multipleMatches, boolean optional, Boolean greedy) {
        this.multipleMatches = multipleMatches;
        this.optional = optional;
        this.greedy = greedy;
    }

    /**
     * Returns indicator if single or multiple matches.
     * @return indicator
     */
    public boolean isMultipleMatches() {
        return multipleMatches;
    }

    /**
     * Returns indicator if optional matches.
     * @return indicator
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Returns indicator if greedy or reluctant.
     * @return indicator
     */
    public Boolean isGreedy()
    {
        return greedy;
    }

    /**
     * Inspect code and return enum for code.
     * @param code to inspect
     * @param reluctantQuestion null for greedy or questionmark for reluctant
     * @return enum
     */
    public static RegexNFATypeEnum fromString(String code, String reluctantQuestion)
    {
        boolean reluctant = false;
        if (reluctantQuestion != null)
        {
            if (!reluctantQuestion.equals("?"))
            {
                throw new IllegalArgumentException("Invalid code for pattern type: " + code + " reluctant '" + reluctantQuestion + "'");
            }
            reluctant = true;
        }

        if (code == null)
        {
            return SINGLE;
        }
        if (code.equals("*"))
        {
            return reluctant ? ZERO_TO_MANY_RELUCTANT : ZERO_TO_MANY;
        }
        if (code.equals("+"))
        {
            return reluctant ? ONE_TO_MANY_RELUCTANT : ONE_TO_MANY;
        }
        if (code.equals("?"))
        {
            return reluctant ? ONE_OPTIONAL_RELUCTANT : ONE_OPTIONAL;
        }
        throw new IllegalArgumentException("Invalid code for pattern type: " + code);
    }

    /**
     * Return postfix.
     * @return postfix
     */
    public String getOptionalPostfix() {
        if (this == SINGLE)
        {
            return "";
        }
        if (this == ZERO_TO_MANY)
        {
            return "*";
        }
        if (this == ONE_TO_MANY)
        {
            return "+";
        }
        if (this == ONE_OPTIONAL)
        {
            return "?";
        }
        throw new IllegalArgumentException("Invalid pattern type: " + this);
    }
}

package com.espertech.esper.client.soda;

/**
 * Type of schedule item.
 */
public enum ScheduleItemType
{
    /**
     * Wildcard means any value.
     */
    WILDCARD("*"),

    /**
     * Last day of week or month.
     */
    LASTDAY("last"),

    /**
     * Weekday (nearest to a date)
     */
    WEEKDAY("weekday"),

    /**
     * Last weekday in a month
     */
    LASTWEEKDAY("lastweekday");

    private String syntax;

    private ScheduleItemType(String s)
    {
        this.syntax = s;
    }

    /**
     * Returns the syntax string.
     * @return syntax
     */
    public String getSyntax()
    {
        return syntax;
    }
}

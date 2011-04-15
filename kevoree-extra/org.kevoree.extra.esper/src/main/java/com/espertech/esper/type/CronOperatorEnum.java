package com.espertech.esper.type;

import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import com.espertech.esper.client.EPException;

import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;

/**
 * Enumeration for special keywords in crontab timer.
 */
public enum CronOperatorEnum
{
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

    private CronOperatorEnum(String s)
    {
        syntax = s;        
    }

    /**
     * Returns the syntax string for the operator.
     * @return syntax string
     */
    public String getSyntax()
    {
        return syntax;
    }
}

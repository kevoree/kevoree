package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.expression.ExprTimePeriod;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Interval specification within match_recognize.
 */
public class MatchRecognizeInterval implements MetaDefItem, Serializable
{
    private final ExprTimePeriod timePeriodExpr;
    private final long msec;
    private static final long serialVersionUID = 9015877742992218244L;

    /**
     * Ctor.
     * @param timePeriodExpr time period
     */
    public MatchRecognizeInterval(ExprTimePeriod timePeriodExpr)
    {
        this.timePeriodExpr = timePeriodExpr;
        double seconds = (Double) timePeriodExpr.evaluate(null, true, null);
        msec = (long) (seconds * 1000);
    }

    /**
     * Returns the time period.
     * @return time period
     */
    public ExprTimePeriod getTimePeriodExpr()
    {
        return timePeriodExpr;
    }

    /**
     * Returns the number of milliseconds.
     * @return msec
     */
    public long getMSec()
    {
        return msec;
    }
}
package com.espertech.esper.client.soda;

import java.io.Serializable;

/**
 * Interval used within match recognize.
 */
public class MatchRecognizeIntervalClause implements Serializable {
    private static final long serialVersionUID = 3883389636579120071L;
    private Expression expression;

    /**
     * Ctor.
     */
    public MatchRecognizeIntervalClause() {
    }

    /**
     * Ctor.
     * @param expression interval expression
     */
    public MatchRecognizeIntervalClause(TimePeriodExpression expression) {
        this.expression = expression;
    }

    /**
     * Returns the interval expression.
     * @return expression
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the interval expression.
     * @param expression to set
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}

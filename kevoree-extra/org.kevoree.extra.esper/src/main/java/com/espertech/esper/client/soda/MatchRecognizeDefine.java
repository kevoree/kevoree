package com.espertech.esper.client.soda;

import java.io.Serializable;

/**
 * Define-clause in match-recognize expression.
 */
public class MatchRecognizeDefine implements Serializable {
    private static final long serialVersionUID = -2665038146328267165L;
    
    private String name;
    private Expression expression;

    /**
     * Ctor.
     */
    public MatchRecognizeDefine() {
    }

    /**
     * Ctor.
     * @param name variable name
     * @param expression expression
     */
    public MatchRecognizeDefine(String name, Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    /**
     * Returns the variable name.
     * @return variable name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the variable name.
     * @param name variable name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the expression.
     * @return expression
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the expression.
     * @param expression to set
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}

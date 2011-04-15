package com.espertech.esper.client.deploy;

import java.util.Calendar;
import java.util.Set;

/**
 * Statement level information for deployed modules.
 */
public class DeploymentInformationItem
{
    private String statementName;
    private String expression;

    /**
     * Ctor.
     * @param statementName name of statement
     * @param expression EPL text
     */
    public DeploymentInformationItem(String statementName, String expression) {
        this.statementName = statementName;
        this.expression = expression;
    }

    /**
     * Returns statement name.
     * @return name
     */
    public String getStatementName() {
        return statementName;
    }

    /**
     * Returns EPL text.
     * @return expression
     */
    public String getExpression() {
        return expression;
    }

    public String toString() {
        return "name '" + statementName + "' " +
               " expression " + expression;
    }
}
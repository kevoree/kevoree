package com.espertech.esper.core;

/**
 * Statement metadata.
 */
public class StatementMetadata
{
    private StatementType statementType;

    /**
     * Ctor.
     * @param statementType the type of statement
     */
    public StatementMetadata(StatementType statementType)
    {
        this.statementType = statementType;
    }

    /**
     * Returns the statement type.
     * @return statement type.
     */
    public StatementType getStatementType()
    {
        return statementType;
    }
}

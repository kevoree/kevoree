package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * Frequency expression for use in crontab expressions.
 */
public class CrontabFrequencyExpression extends ExpressionBase
{
    private static final long serialVersionUID = -5781607347729616944L;

    /**
     * Ctor.
     */
    public CrontabFrequencyExpression()
    {
    }

    /**
     * Ctor.
     * @param numericParameter the frequency value
     */
    public CrontabFrequencyExpression(Expression numericParameter)
    {
        this.getChildren().add(numericParameter);
    }

    public ExpressionPrecedenceEnum getPrecedence()
    {
        return ExpressionPrecedenceEnum.UNARY;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        writer.append("*/");
        this.getChildren().get(0).toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
    }
}

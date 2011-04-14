package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * An expression for use in crontab provides all child expression as part of a parameter list.
 */
public class CrontabParameterSetExpression extends ExpressionBase
{
    private static final long serialVersionUID = -8683887158482697984L;

    /**
     * Ctor.
     */
    public CrontabParameterSetExpression()
    {
    }

    public ExpressionPrecedenceEnum getPrecedence()
    {
        return ExpressionPrecedenceEnum.UNARY;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        String delimiter = "";
        writer.write("[");
        for (Expression expr : this.getChildren())
        {
            writer.append(delimiter);
            expr.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
            delimiter = ", ";
        }
        writer.write("]");
    }
}

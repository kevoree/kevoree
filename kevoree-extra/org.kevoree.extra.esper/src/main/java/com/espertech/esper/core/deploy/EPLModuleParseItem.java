package com.espertech.esper.core.deploy;

public class EPLModuleParseItem
{
    private String expression;
    private int lineNum;
    private int startChar;
    private int endChar;

    public EPLModuleParseItem(String expression, int lineNum, int startChar, int endChar)
    {
        this.expression = expression;
        this.lineNum = lineNum;
        this.startChar = startChar;
        this.endChar = endChar;
    }

    public int getLineNum()
    {
        return lineNum;
    }

    public String getExpression()
    {
        return expression;
    }

    public int getStartChar()
    {
        return startChar;
    }

    public int getEndChar()
    {
        return endChar;
    }
}

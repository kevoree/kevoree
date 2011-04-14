package com.espertech.esper.client.hook;

/**
 * For use with {@link SQLColumnTypeConversion}, context of parameter conversion.
 */
public class SQLInputParameterContext
{
    private int parameterNumber;
    private Object parameterValue;

    /**
     * Ctor.
     */
    public SQLInputParameterContext()
    {
    }

    /**
     * Set parameter value.
     * @param parameterValue to set
     */
    public void setParameterValue(Object parameterValue)
    {
        this.parameterValue = parameterValue;
    }

    /**
     * Set parameter number
     * @param parameterNumber to set
     */
    public void setParameterNumber(int parameterNumber)
    {
        this.parameterNumber = parameterNumber;
    }

    /**
     * Returns the parameter number.
     * @return number of parameter
     */
    public int getParameterNumber()
    {
        return parameterNumber;
    }

    /**
     * Returns the parameter value.
     * @return parameter value
     */
    public Object getParameterValue()
    {
        return parameterValue;
    }
}

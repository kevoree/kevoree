/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * Represents the base expression for "first", "last" and "window" aggregation functions.
 */
public abstract class AccessProjectionExpressionBase extends ExpressionBase
{
    private boolean wildcard;
    private String streamWildcard;

    /**
     * Ctor.
     */
    public AccessProjectionExpressionBase() {
    }

    /**
     * Returns the function name of the aggregation function.
     * @return function name
     */
    public abstract String getAggregationFunctionName();

    /**
     * Ctor.
     * @param expression to aggregate
     */
    public AccessProjectionExpressionBase(Expression expression)
    {
        this.getChildren().add(expression);
    }

    public ExpressionPrecedenceEnum getPrecedence()
    {
        return ExpressionPrecedenceEnum.UNARY;
    }

    /**
     * Returns true for wildcard parameter.
     * @return wildcard indicator
     */
    public boolean isWildcard() {
        return wildcard;
    }

    /**
     * Set to true for wildcard parameter.
     * @param wildcard wildcard indicator
     */
    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    /**
     * Return stream name for stream-wild, if any.
     * @return stream wildcard
     */
    public String getStreamWildcard() {
        return streamWildcard;
    }

    /**
     * Set stream name for stream-wild, if any.
     * @param streamWildcard stream wildcard
     */
    public void setStreamWildcard(String streamWildcard) {
        this.streamWildcard = streamWildcard;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        writer.write(getAggregationFunctionName());
        writer.write('(');
        String delimiter = "";
        if (wildcard) {
            writer.write('*');
            delimiter = ", ";
        }
        if (streamWildcard != null) {
            writer.write(streamWildcard);
            writer.write(".*");
            delimiter = ", ";
        }
        if (this.getChildren().size() > 0)
        {
            writer.write(delimiter);
            this.getChildren().get(0).toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        }
        writer.write(")");
    }
}
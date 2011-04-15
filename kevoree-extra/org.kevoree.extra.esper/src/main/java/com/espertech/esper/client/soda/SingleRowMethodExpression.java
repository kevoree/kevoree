/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import com.espertech.esper.collection.Pair;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic single-row method call consists of a method name and parameters, possibly chained.
 */
public class SingleRowMethodExpression extends ExpressionBase
{
    private static final long serialVersionUID = -8698785052124988195L;
    private List<Pair<String, List<Expression>>> chain = new ArrayList<Pair<String, List<Expression>>>();

    /**
     * Ctor.
     * @param method method name
     * @param parameters an optiona array of parameters
     */
    public SingleRowMethodExpression(String method, Object[] parameters)
    {
        List<Expression> parameterList = new ArrayList<Expression>();
        for (int i = 0; i < parameters.length; i++)
        {
            if (parameters[i] instanceof Expression)
            {
                parameterList.add((Expression)parameters[i]);
            }
            else
            {
                parameterList.add(new ConstantExpression(parameters[i]));
            }
        }
        chain.add(new Pair<String, List<Expression>>(method, parameterList));
    }

    /**
     * Returns the optional method invocation chain for the single-row method consisting of
     * pairs of method name and list of parameters.
     * @return chain of method invocations
     */
    public List<Pair<String, List<Expression>>> getChain()
    {
        return chain;
    }

    /**
     * Ctor.
     * @param chain of method invocations with at least one element, each pair a method name and list of parameter expressions
     */
    public SingleRowMethodExpression(List<Pair<String, List<Expression>>> chain)
    {
        this.chain = chain;
    }

    public ExpressionPrecedenceEnum getPrecedence()
    {
        return ExpressionPrecedenceEnum.UNARY;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        String methodDelimiter = "";
        for (Pair<String, List<Expression>> pair : chain)
        {
            writer.write(methodDelimiter);
            methodDelimiter = ".";
            writer.write(pair.getFirst());

            writer.write("(");
            String delimiter = "";
            for (Expression param : pair.getSecond()) {
                writer.write(delimiter);
                delimiter = ", ";
                param.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
            }
            writer.write(")");
        }
    }
}

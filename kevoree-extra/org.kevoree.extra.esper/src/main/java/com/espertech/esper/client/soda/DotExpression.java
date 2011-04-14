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
 * Dot-expresson is for use in "(inner_expression).dot_expression".
 */
public class DotExpression extends ExpressionBase
{
    private static final long serialVersionUID = -7597133103354244332L;
    private List<Pair<String, List<Expression>>> chain = new ArrayList<Pair<String, List<Expression>>>();
    
    /**
     * Ctor.
     */
    public DotExpression() {
    }

    /**
     * Ctor.
     * @param innerExpression the expression in parenthesis
     */
    public DotExpression(Expression innerExpression)
    {
        this.getChildren().add(innerExpression);
    }

    /**
     * Add a method to the chain of methods after the dot.
     * @param methodName to add
     * @param parameters parameters to method
     */
    public void add(String methodName, List<Expression> parameters)
    {
        chain.add(new Pair<String, List<Expression>>(methodName, parameters));
    }

    /**
     * Returns the method chain of all methods after the dot.
     * @return method name ane list of parameters
     */
    public List<Pair<String, List<Expression>>> getChain()
    {
        return chain;
    }

    public ExpressionPrecedenceEnum getPrecedence()
    {
        return ExpressionPrecedenceEnum.RELATIONAL_BETWEEN_IN;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        writer.write("(");
        this.getChildren().get(0).toEPL(writer, getPrecedence());
        writer.write(")");
        renderChain(chain, writer);
    }

    /**
     * Renders a method invocation chain
     * @param chain pairs of method name and parameters
     * @param writer to render to
     */
    protected static void renderChain(List<Pair<String, List<Expression>>> chain, StringWriter writer) {
        for (Pair<String, List<Expression>> pair : chain)
        {
            writer.write(".");
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

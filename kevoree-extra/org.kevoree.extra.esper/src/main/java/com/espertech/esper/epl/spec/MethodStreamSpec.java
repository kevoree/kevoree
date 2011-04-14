/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.util.MetaDefItem;

import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * Specification object for historical data poll via database SQL statement.
 */
public class MethodStreamSpec extends StreamSpecBase implements StreamSpecRaw, StreamSpecCompiled, MetaDefItem, Serializable
{
    private String ident;
    private String className;
    private String methodName;
    private List<ExprNode> expressions;
    private static final long serialVersionUID = -5290682188045211532L;

    /**
     * Ctor.
     * @param optionalStreamName is the stream name or null if none defined
     * @param viewSpecs is an list of view specifications
     * @param ident the prefix in the clause
     * @param className the class name
     * @param methodName the method name
     * @param expressions the parameter expressions
     */
    public MethodStreamSpec(String optionalStreamName, List<ViewSpec> viewSpecs, String ident, String className, String methodName, List<ExprNode> expressions)
    {
        super(optionalStreamName, viewSpecs, new StreamSpecOptions());
        this.ident = ident;
        this.className = className;
        this.methodName = methodName;
        this.expressions = expressions;
    }

    /**
     * Returns the prefix (method) for the method invocation syntax.
     * @return identifier
     */
    public String getIdent()
    {
        return ident;
    }

    /**
     * Returns the class name.
     * @return class name
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Returns the method name.
     * @return method name
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * Returns the parameter expressions.
     * @return parameter expressions
     */
    public List<ExprNode> getExpressions()
    {
        return expressions;
    }

    public StreamSpecCompiled compile(StatementContext context, Set<String> eventTypeReferences, boolean isInsertInto) throws ExprValidationException
    {
        if (!ident.equals("method"))
        {
            throw new ExprValidationException("Expecting keyword 'method', found '" + ident + "'");
        }
        if (methodName == null)
        {
            throw new ExprValidationException("No method name specified for method-based join");
        }
        return this;
    }
}

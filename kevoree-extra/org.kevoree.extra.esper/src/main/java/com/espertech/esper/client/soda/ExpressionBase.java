/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.util.ArrayList;
import java.util.List;
import java.io.StringWriter;

/**
 * Base expression.
 */
public abstract class ExpressionBase implements Expression
{
    private static final long serialVersionUID = 0L;

    private String treeObjectName;
    private List<Expression> children;

    public String getTreeObjectName()
    {
        return treeObjectName;
    }

    public void setTreeObjectName(String treeObjectName)
    {
        this.treeObjectName = treeObjectName;
    }

    /**
     * Ctor.
     */
    public ExpressionBase()
    {
        children = new ArrayList<Expression>();
    }

    /**
     * Returns the list of sub-expressions to the current expression.
     * @return list of child expressions
     */
    public List<Expression> getChildren()
    {
        return children;
    }

    /**
     * Adds a new child expression to the current expression.
     * @param expression to add
     */
    public void addChild(Expression expression)
    {
        children.add(expression);
    }

    public void setChildren(List<Expression> children) {
        this.children = children;
    }

    public final void toEPL(StringWriter writer, ExpressionPrecedenceEnum parentPrecedence) {
        if (this.getPrecedence().getLevel() < parentPrecedence.getLevel()) {
            writer.write("(");
            toPrecedenceFreeEPL(writer);
            writer.write(")");
        }
        else {
            toPrecedenceFreeEPL(writer);
        }
    }

    /**
     * Renders the expressions and all it's child expression, in full tree depth, as a string in
     * language syntax.
     * @param writer is the output to use
     */
    public abstract void toPrecedenceFreeEPL(StringWriter writer);
}

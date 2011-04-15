/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.collection.Pair;

import java.util.List;
import java.util.LinkedList;

/**
 * Visitor that collects event property identifier information under expression nodes.
 */
public class ExprNodeIdentifierCollectVisitor implements ExprNodeVisitor
{
    private final List<ExprIdentNode> exprProperties;

    /**
     * Ctor.
     */
    public ExprNodeIdentifierCollectVisitor()
    {
        this.exprProperties = new LinkedList<ExprIdentNode>();
    }

    public boolean isVisit(ExprNode exprNode)
    {
        return true;
    }

    /**
     * Returns list of event property stream numbers and names that uniquely identify which
     * property is from whcih stream, and the name of each.
     * @return list of event property statement-unique info
     */
    public List<ExprIdentNode> getExprProperties()
    {
        return exprProperties;
    }

    public void visit(ExprNode exprNode)
    {
        if (!(exprNode instanceof ExprIdentNode))
        {
            return;
        }

        ExprIdentNode identNode = (ExprIdentNode) exprNode;
        exprProperties.add(identNode);
    }
}
/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import java.util.LinkedList;
import java.util.List;

/**
 * Visitor that collects {@link ExprSubselectNode} instances.
 */
public class ExprNodeSubselectVisitor implements ExprNodeVisitor
{
    private final List<ExprSubselectNode> subselects;

    /**
     * Ctor.
     */
    public ExprNodeSubselectVisitor()
    {
        subselects = new LinkedList<ExprSubselectNode>();
    }

    /**
     * Returns a list of lookup expression nodes.
     * @return lookup nodes
     */
    public List<ExprSubselectNode> getSubselects() {
        return subselects;
    }

    public boolean isVisit(ExprNode exprNode)
    {
        return true;
    }

    public void visit(ExprNode exprNode)
    {
        if (!(exprNode instanceof ExprSubselectNode))
        {
            return;
        }

        ExprSubselectNode subselectNode = (ExprSubselectNode) exprNode;
        subselects.add(subselectNode);
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.join.exec.ExecNode;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.epl.join.table.HistoricalStreamIndexList;
import com.espertech.esper.epl.join.exec.NestedIterationExecNode;
import com.espertech.esper.util.IndentWriter;
import com.espertech.esper.view.Viewable;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Plan to perform a nested iteration over child nodes.
 */
public class NestedIterationNode extends QueryPlanNode
{
    private final LinkedList<QueryPlanNode> childNodes;
    private final int[] nestingOrder;

    /**
     * Ctor.
     * @param nestingOrder - order of streams in nested iteration
     */
    public NestedIterationNode(int[] nestingOrder)
    {
        this.nestingOrder = nestingOrder;
        this.childNodes = new LinkedList<QueryPlanNode>();

        if (nestingOrder.length == 0)
        {
            throw new IllegalArgumentException("Invalid empty nesting order");
        }
    }

    /**
     * Adds a child node.
     * @param childNode is the child evaluation tree node to add
     */
    public final void addChildNode(QueryPlanNode childNode)
    {
        childNodes.add(childNode);
    }

    /**
     * Returns list of child nodes.
     * @return list of child nodes
     */
    protected final LinkedList<QueryPlanNode> getChildNodes()
    {
        return childNodes;
    }

    public ExecNode makeExec(EventTable[][] indexPerStream, EventType[] streamTypes, Viewable[] streamViews, HistoricalStreamIndexList[] historicalStreamIndexList)
    {
        if (childNodes.isEmpty())
        {
            throw new IllegalStateException("Zero child nodes for nested iteration");
        }

        NestedIterationExecNode execNode = new NestedIterationExecNode(nestingOrder);
        for (QueryPlanNode child : childNodes)
        {
            ExecNode childExec = child.makeExec(indexPerStream, streamTypes, streamViews, historicalStreamIndexList);
            execNode.addChildNode(childExec);
        }
        return execNode;
    }

    public void print(IndentWriter indentWriter)
    {
        indentWriter.println("NestedIterationNode with nesting order " + Arrays.toString(nestingOrder));
        indentWriter.incrIndent();
        for (QueryPlanNode child : childNodes)
        {
            child.print(indentWriter);
        }
        indentWriter.decrIndent();
    }
}

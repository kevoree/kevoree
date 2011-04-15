/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import java.util.List;
import java.util.ArrayList;

/**
 * Result of analysis of pattern expression node tree.
 */
public class EvalNodeAnalysisResult
{
    private List<EvalNode> activeNodes = new ArrayList<EvalNode>();

    /**
     * Add a node found.
     * @param node found
     */
    public void addNode(EvalNode node)
    {
        activeNodes.add(node);
    }

    /**
     * Returns all nodes found.
     * @return pattern nodes
     */
    public List<EvalNode> getActiveNodes()
    {
        return activeNodes;
    }

    /**
     * Returns filter nodes.
     * @return filter nodes
     */
    public List<EvalFilterNode> getFilterNodes()
    {
        List<EvalFilterNode> filterNodes = new ArrayList<EvalFilterNode>();
        for (EvalNode node : activeNodes)
        {
            if (node instanceof EvalFilterNode)
            {
                filterNodes.add((EvalFilterNode) node);
            }
        }
        return filterNodes;
    }

    /**
     * Returns the repeat-nodes.
     * @return repeat nodes
     */
    public List<EvalMatchUntilNode> getRepeatNodes()
    {
        List<EvalMatchUntilNode> filterNodes = new ArrayList<EvalMatchUntilNode>();
        for (EvalNode node : activeNodes)
        {
            if (node instanceof EvalMatchUntilNode)
            {
                filterNodes.add((EvalMatchUntilNode) node);
            }
        }
        return filterNodes;
    }

    /**
     * Returns the every-distinct-nodes.
     * @return distinct nodes
     */
    public List<EvalEveryDistinctNode> getDistinctNodes()
    {
        List<EvalEveryDistinctNode> filterNodes = new ArrayList<EvalEveryDistinctNode>();
        for (EvalNode node : activeNodes)
        {
            if (node instanceof EvalEveryDistinctNode)
            {
                filterNodes.add((EvalEveryDistinctNode) node);
            }
        }
        return filterNodes;
    }
}

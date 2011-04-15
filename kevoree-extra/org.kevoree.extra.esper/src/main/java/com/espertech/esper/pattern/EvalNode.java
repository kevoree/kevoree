/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.util.MetaDefItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Superclass of all nodes in an evaluation tree representing an event pattern expression.
 * Follows the Composite pattern. Child nodes do not carry references to parent nodes, the tree
 * is unidirectional.
 */
public abstract class EvalNode implements MetaDefItem, Serializable
{
    private static final Log log = LogFactory.getLog(EvalNode.class);
    private static final long serialVersionUID = 0L;

    private final List<EvalNode> childNodes;
    private EvalNodeNumber nodeNumber;

    /**
     * Create the evaluation state node containing the truth value state for each operator in an
     * event expression.
     * @param parentNode is the parent evaluator node that this node indicates a change in truth value to
     * @param beginState is the container for events that makes up the start state
     * @param context is the handle to services required for evaluation
     * @param stateNodeId is the new state object's identifier
     * @return state node containing the truth value state for the operator
     */
    public abstract EvalStateNode newState(Evaluator parentNode,
                                           MatchedEventMap beginState,
                                           PatternContext context,
                                           EvalStateNodeNumber stateNodeId);

    /**
     * Constructor creates a list of child nodes.
     */
    EvalNode()
    {
        childNodes = new ArrayList<EvalNode>();
    }

    /**
     * Returns the evaluation node's relative node number in the evaluation node tree.
     * @return node number
     */
    public EvalNodeNumber getNodeNumber()
    {
        return nodeNumber;
    }

    /**
     * Sets the evaluation node's relative node number.
     * @param nodeNumber is the node number to set
     */
    public void setNodeNumber(EvalNodeNumber nodeNumber)
    {
        this.nodeNumber = nodeNumber;
    }

    /**
     * Adds a child node.
     * @param childNode is the child evaluation tree node to add
     */
    public final void addChildNode(EvalNode childNode)
    {
        childNodes.add(childNode);
    }

    /**
     * Returns list of child nodes.
     * @return list of child nodes
     */
    public final List<EvalNode> getChildNodes()
    {
        return childNodes;
    }

    /**
     * Recursively print out all nodes.
     * @param prefix is printed out for naming the printed info
     */
    public final void dumpDebug(String prefix)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".dumpDebug " + prefix + this.toString());
        }
        for (EvalNode node : childNodes)
        {
            node.dumpDebug(prefix + "  ");
        }
    }

    /**
     * Searched recursivly for pattern evaluation filter nodes.
     * @param currentNode is the root node
     * @return list of filter nodes
     */
    public static EvalNodeAnalysisResult recursiveAnalyzeChildNodes(EvalNode currentNode)
    {
        EvalNodeAnalysisResult evalNodeAnalysisResult = new EvalNodeAnalysisResult();
        recursiveAnalyzeChildNodes(evalNodeAnalysisResult, currentNode);
        return evalNodeAnalysisResult;
    }

    private static void recursiveAnalyzeChildNodes(EvalNodeAnalysisResult evalNodeAnalysisResult, EvalNode currentNode)
    {
        if ((currentNode instanceof EvalFilterNode) ||
            (currentNode instanceof EvalGuardNode) ||
            (currentNode instanceof EvalObserverNode) ||
            (currentNode instanceof EvalMatchUntilNode) ||
            (currentNode instanceof EvalEveryDistinctNode))
        {
            evalNodeAnalysisResult.addNode(currentNode);
        }

        for (EvalNode node : currentNode.getChildNodes())
        {
            recursiveAnalyzeChildNodes(evalNodeAnalysisResult, node);
        }
    }

    /**
     * Returns all child nodes as a set.
     * @param currentNode parent node
     * @return all child nodes
     */
    public static Set<EvalNode> recursiveGetChildNodes(EvalNode currentNode)
    {
        Set<EvalNode> result = new HashSet<EvalNode>();
        recursiveGetChildNodes(result, currentNode);
        return result;
    }

    private static void recursiveGetChildNodes(Set<EvalNode> set, EvalNode currentNode)
    {
        for (EvalNode node : currentNode.getChildNodes())
        {
            set.add(node);
            recursiveGetChildNodes(set, node);
        }
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.assemble;

import com.espertech.esper.epl.join.rep.Node;
import com.espertech.esper.util.IndentWriter;

import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map;

/**
 * Represents a node in a tree responsible for assembling outer join query results.
 * <p>
 * The tree is double-linked, child nodes know each parent and parent know all child nodes.
 * <p>
 * Each specific subclass of this abstract assembly node is dedicated to assembling results for
 * a certain event stream.
 */
public abstract class BaseAssemblyNode implements ResultAssembler
{
    /**
     * Parent node.
     */
    protected ResultAssembler parentNode;

    /**
     * Child nodes.
     */
    protected final List<BaseAssemblyNode> childNodes;

    /**
     * Stream number.
     */
    protected final int streamNum;

    /**
     * Number of streams in statement.
     */
    protected final int numStreams;

    /**
     * Ctor.
     * @param streamNum - stream number of the event stream that this node assembles results for.
     * @param numStreams - number of streams
     */
    protected BaseAssemblyNode(int streamNum, int numStreams)
    {
        this.streamNum = streamNum;
        this.numStreams = numStreams;
        childNodes = new LinkedList<BaseAssemblyNode>();
    }

    /**
     * Provides results to assembly nodes for initialization.
     * @param result is a list of result nodes per stream
     */
    public abstract void init(List<Node>[] result);

    /**
     * Process results.
     * @param result is a list of result nodes per stream
     */
    public abstract void process(List<Node>[] result);

    /**
     * Output this node using writer, not outputting child nodes.
     * @param indentWriter to use for output
     */
    public abstract void print(IndentWriter indentWriter);

    /**
     * Set parent node.
     * @param resultAssembler is the parent node
     */
    public void setParentAssembler(ResultAssembler resultAssembler)
    {
        this.parentNode = resultAssembler;
    }

    /**
     * Add a child node.
     * @param childNode to add
     */
    public void addChild(BaseAssemblyNode childNode)
    {
        childNode.parentNode = this;
        childNodes.add(childNode);
    }

    /**
     * Returns the stream number.
     * @return stream number
     */
    protected int getStreamNum()
    {
        return streamNum;
    }

    /**
     * Returns child nodes.
     * @return child nodes
     */
    protected List<BaseAssemblyNode> getChildNodes()
    {
        return childNodes;
    }

    /**
     * Returns parent node.
     * @return parent node
     */
    protected ResultAssembler getParentAssembler()
    {
        return parentNode;
    }

    /**
     * Returns an array of stream numbers that lists all child node's stream numbers.
     * @return child node stream numbers
     */
    protected int[] getSubstreams()
    {
        List<Integer> substreams = new LinkedList<Integer>();
        recusiveAddSubstreams(substreams);

        // copy to array
        int[] substreamArr = new int[substreams.size()];
        int count = 0;
        for (Integer stream : substreams)
        {
            substreamArr[count++] = stream;
        }

        return substreamArr;
    }

    private void recusiveAddSubstreams(List<Integer> substreams)
    {
        substreams.add(streamNum);
        for (BaseAssemblyNode child : childNodes)
        {
            child.recusiveAddSubstreams(substreams);
        }
    }

    /**
     * Output this node and all descendent nodes using writer, outputting child nodes.
     * @param indentWriter to output to
     */
    public void printDescendends(IndentWriter indentWriter)
    {
        this.print(indentWriter);
        for (BaseAssemblyNode child : childNodes)
        {
            indentWriter.incrIndent();
            child.print(indentWriter);
            indentWriter.decrIndent();
        }
    }

    /**
     * Returns all descendent nodes to the top node in a list in which the utmost descendants are
     * listed first and the top node itself is listed last.
     * @param topNode is the root node of a tree structure
     * @return list of nodes with utmost descendants first ordered by level of depth in tree with top node last
     */
    public static List<BaseAssemblyNode> getDescendentNodesBottomUp(BaseAssemblyNode topNode)
    {
        List<BaseAssemblyNode> result = new LinkedList<BaseAssemblyNode>();

        // Map to hold per level of the node (1 to N depth) of node a list of nodes, if any
        // exist at that level
        TreeMap<Integer, List<BaseAssemblyNode>> nodesPerLevel = new TreeMap<Integer, List<BaseAssemblyNode>>();

        // Recursively enter all aggregate functions and their level into map
        recursiveAggregateEnter(topNode, nodesPerLevel, 1);

        // Done if none found
        if (nodesPerLevel.isEmpty())
        {
            throw new IllegalStateException("Empty collection for nodes per level");
        }

        // From the deepest (highest) level to the lowest, add aggregates to list
        int deepLevel = nodesPerLevel.lastKey();
        for (int i = deepLevel; i >= 1; i--)
        {
            List<BaseAssemblyNode> list = nodesPerLevel.get(i);
            if (list == null)
            {
                continue;
            }
            result.addAll(list);
        }

        return result;
    }

    private static void recursiveAggregateEnter(BaseAssemblyNode currentNode, Map<Integer, List<BaseAssemblyNode>> nodesPerLevel, int currentLevel)
    {
        // ask all child nodes to enter themselves
        for (BaseAssemblyNode node : currentNode.childNodes)
        {
            recursiveAggregateEnter(node, nodesPerLevel, currentLevel + 1);
        }

        // Add myself to list
        List<BaseAssemblyNode> aggregates = nodesPerLevel.get(currentLevel);
        if (aggregates == null)
        {
            aggregates = new LinkedList<BaseAssemblyNode>();
            nodesPerLevel.put(currentLevel, aggregates);
        }
        aggregates.add(currentNode);
    }
}

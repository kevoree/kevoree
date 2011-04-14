/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import com.espertech.esper.epl.expression.*;

/**
 * Analyzes a filter expression and builds a query graph model.
 * The 'equals' and 'and' expressions in the filter expression are extracted
 * and placed in the query graph model as navigable relationships (by key and index
 * properties) between streams.
 */
public class FilterExprAnalyzer
{
    /**
     * Analyzes filter expression to build query graph model.
     * @param topNode - filter top node
     * @param queryGraph - model containing relationships between streams, to be written to
     */
    public static void analyze(ExprNode topNode, QueryGraph queryGraph)
    {
        // Analyze relationships between streams. Relationships are properties in AND and EQUALS nodes of joins.
        if (topNode instanceof ExprEqualsNode)
        {
            ExprEqualsNode equalsNode = (ExprEqualsNode) topNode;
            if (!equalsNode.isNotEquals())
            {
                analyzeEqualsNode(equalsNode, queryGraph);
            }
        }
        else if (topNode instanceof ExprAndNode)
        {
            ExprAndNode andNode = (ExprAndNode) topNode;
            analyzeAndNode(andNode, queryGraph);
        }
    }

    /**
     * Analye EQUALS (=) node.
     * @param equalsNode - node to analyze
     * @param queryGraph - store relationships between stream properties
     */
    protected static void analyzeEqualsNode(ExprEqualsNode equalsNode, QueryGraph queryGraph)
    {
        if ( (!(equalsNode.getChildNodes().get(0) instanceof ExprIdentNode)) ||
             (!(equalsNode.getChildNodes().get(1) instanceof ExprIdentNode)) )
        {
            return;
        }

        ExprIdentNode identNodeLeft = (ExprIdentNode) equalsNode.getChildNodes().get(0);
        ExprIdentNode identNodeRight = (ExprIdentNode) equalsNode.getChildNodes().get(1);

        if (identNodeLeft.getStreamId() != identNodeRight.getStreamId())
        {
            queryGraph.add(identNodeLeft.getStreamId(), identNodeLeft.getResolvedPropertyName(),
                    identNodeRight.getStreamId(), identNodeRight.getResolvedPropertyName());            
        }
    }

    /**
     * Analyze the AND-node.
     * @param andNode - node to analyze
     * @param queryGraph - to store relationships between stream properties
     */
    protected static void analyzeAndNode(ExprAndNode andNode, QueryGraph queryGraph)
    {
        for (ExprNode childNode : andNode.getChildNodes())
        {
            if (childNode instanceof ExprEqualsNode)
            {
                ExprEqualsNode equalsNode = (ExprEqualsNode) childNode;
                if (!equalsNode.isNotEquals())
                {
                    analyzeEqualsNode(equalsNode, queryGraph);
                }
            }
        }
    }
}

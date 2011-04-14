/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.rowregex;

import com.espertech.esper.util.MetaDefItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Base node for 
 */
public abstract class RowRegexExprNode implements MetaDefItem, Serializable
{
    private static final Log log = LogFactory.getLog(RowRegexExprNode.class);
    private static final long serialVersionUID = 0L;

    private final ArrayList<RowRegexExprNode> childNodes;

    /**
     * Returns the expression node rendered as a string.
     * @return string rendering of expression
     */
    public abstract String toExpressionString();

    /**
     * Constructor creates a list of child nodes.
     */
    public RowRegexExprNode()
    {
        childNodes = new ArrayList<RowRegexExprNode>();
    }


    /**
     * Adds a child node.
     * @param childNode is the child evaluation tree node to add
     */
    public final void addChildNode(RowRegexExprNode childNode)
    {
        childNodes.add(childNode);
    }

    /**
     * Returns list of child nodes.
     * @return list of child nodes
     */
    public final ArrayList<RowRegexExprNode> getChildNodes()
    {
        return childNodes;
    }

    /**
     * Recursively print out all nodes.
     * @param prefix is printed out for naming the printed info
     */
    @SuppressWarnings({"StringContatenationInLoop"})
    public final void dumpDebug(String prefix)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".dumpDebug " + prefix + this.toString());
        }
        for (RowRegexExprNode node : childNodes)
        {
            node.dumpDebug(prefix + "  ");
        }
    }
}

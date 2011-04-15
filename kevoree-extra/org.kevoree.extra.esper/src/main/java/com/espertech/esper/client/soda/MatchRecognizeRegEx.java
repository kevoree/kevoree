/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Interface representing an expression for use in match-recognize.
 * <p>
 * Event row regular expressions are organized into a tree-like structure with nodes representing sub-expressions.
 */
public abstract class MatchRecognizeRegEx implements Serializable
{
    private String treeObjectName;
    private List<MatchRecognizeRegEx> children;

    /**
     * Returns id of expression assigned by tools.
     * @return id
     */
    public String getTreeObjectName()
    {
        return treeObjectName;
    }

    /**
     * Sets id of expression assigned by tools.
     * @param treeObjectName to set
     */
    public void setTreeObjectName(String treeObjectName)
    {
        this.treeObjectName = treeObjectName;
    }

    /**
     * Ctor.
     */
    protected MatchRecognizeRegEx() {
        this.children = new ArrayList<MatchRecognizeRegEx>();
    }

    /**
     * Returns child nodes.
     * @return child nodes
     */
    public List<MatchRecognizeRegEx> getChildren() {
        return children;
    }

    /**
     * Set child nodes.
     * @param children child nodes to set
     */
    public void setChildren(List<MatchRecognizeRegEx> children) {
        this.children = children;
    }

    /**
     * Write EPL.
     * @param writer to use
     */
    public abstract void writeEPL(StringWriter writer);
}
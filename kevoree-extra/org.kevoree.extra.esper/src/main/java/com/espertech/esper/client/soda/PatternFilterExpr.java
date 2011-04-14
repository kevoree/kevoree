/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * Filter for use in pattern expressions. 
 */
public class PatternFilterExpr extends PatternExprBase
{
    private String tagName;
    private Filter filter;
    private static final long serialVersionUID = -916214860560949884L;

    /**
     * Ctor.
     */
    public PatternFilterExpr() {
    }

    /**

     * Ctor.
     * @param filter specifies to events to filter out
     */
    public PatternFilterExpr(Filter filter)
    {
        this(filter, null);
    }

    /**
     * Ctor.
     * @param filter specifies to events to filter out
     * @param tagName specifies the name of the tag to assigned to matching events
     */
    public PatternFilterExpr(Filter filter, String tagName)
    {
        this.tagName = tagName;
        this.filter = filter;
    }

    /**
     * Returns the tag name.
     * @return tag name.
     */
    public String getTagName()
    {
        return tagName;
    }

    /**
     * Sets the tag name.
     * @param tagName tag name to set
     */
    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    /**
     * Returns the filter specification.
     * @return filter
     */
    public Filter getFilter()
    {
        return filter;
    }

    /**
     * Sets the filter specification.
     * @param filter to use
     */
    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }

    public PatternExprPrecedenceEnum getPrecedence() {
        return PatternExprPrecedenceEnum.ATOM;
    }

    public void toPrecedenceFreeEPL(StringWriter writer)
    {
        if (tagName != null)
        {
            writer.write(tagName);
            writer.write('=');
        }
        filter.toEPL(writer);
    }
}

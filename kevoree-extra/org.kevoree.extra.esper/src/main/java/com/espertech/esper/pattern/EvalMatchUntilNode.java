/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.epl.expression.ExprNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;

/**
 * This class represents a match-until observer in the evaluation tree representing any event expressions.
 */
public class EvalMatchUntilNode extends EvalNode
{
    private static final long serialVersionUID = -959026931248456356L;

    private ExprNode lowerBounds;
    private ExprNode upperBounds;
    private transient MatchedEventConvertor convertor;
    private transient PatternContext context;
    private String[] tagsArrayed;

    /**
     * Ctor.
     */
    protected EvalMatchUntilNode(ExprNode lowerBounds, ExprNode upperBounds)
            throws IllegalArgumentException
    {
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    /**
     * Returns an array of tags for events, which is all tags used within the repeat-operator.
     * @return array of tags
     */
    public String[] getTagsArrayed()
    {
        return tagsArrayed;
    }

    /**
     * Sets the convertor for matching events to events-per-stream.
     * @param convertor convertor
     */
    public void setConvertor(MatchedEventConvertor convertor) {
        this.convertor = convertor;
    }

    public ExprNode getLowerBounds() {
        return lowerBounds;
    }

    public ExprNode getUpperBounds() {
        return upperBounds;
    }

    public void setLowerBounds(ExprNode lowerBounds) {
        this.lowerBounds = lowerBounds;
    }

    public void setUpperBounds(ExprNode upperBounds) {
        this.upperBounds = upperBounds;
    }

    /**
     * Sets the tags used within the repeat operator.
     * @param tagsArrayedSet tags used within the repeat operator
     */
    public void setTagsArrayedSet(Set<String> tagsArrayedSet)
    {
        if (tagsArrayedSet != null)
        {
            tagsArrayed = tagsArrayedSet.toArray(new String[tagsArrayedSet.size()]);
        }
        else
        {
            tagsArrayed = new String[0];
        }
    }

    public EvalStateNode newState(Evaluator parentNode,
                                                 MatchedEventMap beginState,
                                                 PatternContext context,
                                                 EvalStateNodeNumber stateNodeId)
    {
        if (this.context == null) {
            this.context = context;
        }
        return new EvalMatchUntilStateNode(parentNode, this, beginState);
    }

    public PatternContext getContext() {
        return context;
    }

    public MatchedEventConvertor getConvertor() {
        return convertor;
    }

    public final String toString()
    {
        return ("EvalMatchUntilNode children=" + this.getChildNodes().size());
    }

    private static final Log log = LogFactory.getLog(EvalMatchUntilNode.class);
}

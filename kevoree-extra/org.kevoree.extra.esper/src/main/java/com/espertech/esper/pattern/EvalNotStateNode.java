/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.util.ExecutionPathDebugLog;

/**
 * This class contains the state of an 'not' operator in the evaluation state tree.
 * The not operator inverts the truth of the subexpression under it. It defaults to being true rather than
 * being false at startup. True at startup means it will generate an event on newState such that parent expressions
 * may turn true. It turns permenantly false when it receives an event from a subexpression and the subexpression
 * quitted. It indicates the false state via an evaluateFalse call on its parent evaluator.
 */
public final class EvalNotStateNode extends EvalStateNode implements Evaluator
{
    private final EvalNotNode evalNotNode;
    private final MatchedEventMap beginState;
    private EvalStateNode childNode;

    /**
     * Constructor.
     * @param parentNode is the parent evaluator to call to indicate truth value
     * @param beginState contains the events that make up prior matches
     * @param evalNotNode is the factory node associated to the state
     */
    public EvalNotStateNode(Evaluator parentNode,
                                  EvalNotNode evalNotNode,
                                  MatchedEventMap beginState)
    {
        super(parentNode, null);

        this.evalNotNode = evalNotNode;
        this.beginState = beginState.shallowCopy();
        this.childNode = evalNotNode.getChildNodes().get(0).newState(this, beginState, evalNotNode.getContext(), null);
    }

    @Override
    public EvalNode getFactoryNode() {
        return evalNotNode;
    }

    public final void start()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".start Starting single child node");
        }

        if (childNode == null)
        {
            throw new IllegalStateException("'Not' state node is inactive");
        }

        childNode.start();

        // The not node acts by inverting the truth
        // By default the child nodes are false. This not node acts inverts the truth and pretends the child is true,
        // raising an event up.
        this.getParentEvaluator().evaluateTrue(beginState, this, false);
    }

    public final void evaluateFalse(EvalStateNode fromNode)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateFalse");
        }
    }

    public final void evaluateTrue(MatchedEventMap matchEvent, EvalStateNode fromNode, boolean isQuitted)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateTrue fromNode=" + fromNode.hashCode()  + "  isQuitted=" + isQuitted);
        }

        // Only is the subexpression stopped listening can we tell the parent evaluator that this
        // turned permanently false.
        if (isQuitted)
        {
            childNode = null;
            this.getParentEvaluator().evaluateFalse(this);
        }
        else
        {
            // If the subexpression did not quit, we stay in the "true" state
        }
    }

    public final void quit()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".quit Quitting not-node single child, childNode=" + childNode);
        }

        if (childNode != null)
        {
            childNode.quit();
        }
    }

    public final Object accept(EvalStateNodeVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public final Object childrenAccept(EvalStateNodeVisitor visitor, Object data)
    {
        childNode.accept(visitor, data);

        return data;
    }

    public final String toString()
    {
        return "EvalNotStateNode child=" + childNode;
    }

    private static final Log log = LogFactory.getLog(EvalNotStateNode.class);
}

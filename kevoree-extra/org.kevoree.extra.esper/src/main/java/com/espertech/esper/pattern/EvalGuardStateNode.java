/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.pattern.guard.Guard;
import com.espertech.esper.pattern.guard.Quitable;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents the state of a "within" operator in the evaluation state tree.
 * The within operator applies to a subexpression and is thus expected to only
 * have one child node.
 */
public final class EvalGuardStateNode extends EvalStateNode implements Evaluator, Quitable
{
    private EvalGuardNode evalGuardNode;
    private EvalStateNode activeChildNode;
    private final Guard guard;

    /**
     * Constructor.
     * @param parentNode is the parent evaluator to call to indicate truth value
     * @param beginState contains the events that make up prior matches
     * @param evalGuardNode is the factory node associated to the state
     * @param stateObjectId is the state object's id value
     */
    public EvalGuardStateNode(Evaluator parentNode,
                               EvalGuardNode evalGuardNode,
                                 MatchedEventMap beginState,
                                 EvalStateNodeNumber stateObjectId)
    {
        super(parentNode, null);
        this.evalGuardNode = evalGuardNode;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".constructor");
        }

        guard = evalGuardNode.getGuardFactory().makeGuard(evalGuardNode.getContext(), beginState, this, stateObjectId, null);

        this.activeChildNode = evalGuardNode.getChildNodes().get(0).newState(this, beginState, evalGuardNode.getContext(), null);
    }

    @Override
    public EvalNode getFactoryNode() {
        return evalGuardNode;
    }

    @Override
    public PatternContext getContext() {
        return evalGuardNode.getContext();
    }

    public final void start()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".start Starting within timer and single child node");
        }

        if (activeChildNode == null)
        {
            throw new IllegalStateException("Invalid state, child state node is inactive");
        }

        // Start the single child state
        activeChildNode.start();

        // Start the guard
        guard.startGuard();
    }

    public final void evaluateTrue(MatchedEventMap matchEvent, EvalStateNode fromNode, boolean isQuitted)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateTrue fromNode=" + fromNode.hashCode());
        }

        boolean haveQuitted = activeChildNode == null;

        // If one of the children quits, remove the child
        if (isQuitted)
        {
            activeChildNode = null;

            // Stop guard, since associated subexpression is gone
            guard.stopGuard();
        }

        if (!(haveQuitted))
        {
            boolean guardPass = guard.inspect(matchEvent);
            if (guardPass)
            {
                this.getParentEvaluator().evaluateTrue(matchEvent, this, isQuitted);
            }
        }
    }

    public final void evaluateFalse(EvalStateNode fromNode)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateFalse Removing fromNode=" + fromNode.hashCode());
        }
    }

    public final void quit()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".quit Stopping all children");
        }

        if (activeChildNode != null)
        {
            activeChildNode.quit();
            guard.stopGuard();
        }

        activeChildNode = null;
    }

    public final Object accept(EvalStateNodeVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public final Object childrenAccept(EvalStateNodeVisitor visitor, Object data)
    {
        if (activeChildNode != null)
        {
            activeChildNode.accept(visitor, data);
        }
        return data;
    }

    public final String toString()
    {
        return "EvaluationWitinStateNode activeChildNode=" + activeChildNode +
                 " guard=" + guard;
    }

    public void guardQuit()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".quit Guard has quit, stopping child node, activeChildNode=" + activeChildNode);
        }

        // It is possible that the child node has already been quit such as when the parent wait time was shorter.
        // 1. parent node's guard indicates quit to all children
        // 2. this node's guards also indicates quit, however that already occured
        if (activeChildNode != null)
        {
            activeChildNode.quit();
        }
        activeChildNode = null;

        // Indicate to parent state that this is permanently false.
        this.getParentEvaluator().evaluateFalse(this);
    }

    private static final Log log = LogFactory.getLog(EvalGuardStateNode.class);
}

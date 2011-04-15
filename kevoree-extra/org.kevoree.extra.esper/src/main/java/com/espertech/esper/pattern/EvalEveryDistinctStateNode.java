/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Contains the state collected by an "every" operator. The state includes handles to any sub-listeners
 * started by the operator.
 */
public final class EvalEveryDistinctStateNode extends EvalStateNode implements Evaluator, EvalStateNodeNonQuitting
{
    private final EvalEveryDistinctNode everyDistinctNode;
    private final Map<EvalStateNode, Set<MultiKeyUntyped>> spawnedNodes;
    private final MatchedEventMap beginState;

    /**
     * Constructor.
     * @param parentNode is the parent evaluator to call to indicate truth value
     * @param beginState contains the events that make up prior matches
     * @param everyDistinctNode is the factory node associated to the state
     */
    public EvalEveryDistinctStateNode(Evaluator parentNode,
                                  EvalEveryDistinctNode everyDistinctNode,
                                  MatchedEventMap beginState)
    {
        super(parentNode, null);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".constructor");
        }

        this.everyDistinctNode = everyDistinctNode;
        this.spawnedNodes = new LinkedHashMap<EvalStateNode, Set<MultiKeyUntyped>>();
        this.beginState = beginState.shallowCopy();

        EvalStateNode child = getFactoryNode().getChildNodes().get(0).newState(this, beginState, everyDistinctNode.getContext(), null);
        spawnedNodes.put(child, new HashSet<MultiKeyUntyped>());
    }

    @Override
    public EvalNode getFactoryNode() {
        return everyDistinctNode;
    }

    public final void start()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".start Starting single child");
        }

        if (spawnedNodes.size() != 1)
        {
            throw new IllegalStateException("EVERY state node is expected to have single child state node");
        }

        // During the start of the child we need to use the temporary evaluator to catch any event created during a start.
        // Events created during the start would likely come from the "not" operator.
        // Quit the new child again if
        EvalEveryStateSpawnEvaluator spawnEvaluator = new EvalEveryStateSpawnEvaluator(everyDistinctNode.getContext().getStatementName());
        EvalStateNode child = spawnedNodes.keySet().iterator().next();
        child.setParentEvaluator(spawnEvaluator);
        child.start();

        // If the spawned expression turned true already, just quit it
        if (spawnEvaluator.isEvaluatedTrue())
        {
            child.quit();
        }
        else
        {
            child.setParentEvaluator(this);
        }
    }

    public final void evaluateFalse(EvalStateNode fromNode)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateFalse");
        }

        fromNode.quit();
        spawnedNodes.remove(fromNode);

        // Spawn all nodes below this EVERY node
        // During the start of a child we need to use the temporary evaluator to catch any event created during a start
        // Such events can be raised when the "not" operator is used.
        EvalNode child = getFactoryNode().getChildNodes().get(0);
        EvalEveryStateSpawnEvaluator spawnEvaluator = new EvalEveryStateSpawnEvaluator(everyDistinctNode.getContext().getStatementName());
        EvalStateNode spawned = child.newState(spawnEvaluator, beginState, everyDistinctNode.getContext(), null);
        spawned.start();

        // If the whole spawned expression already turned true, quit it again
        if (spawnEvaluator.isEvaluatedTrue())
        {
            spawned.quit();
        }
        else
        {
            spawnedNodes.put(spawned, new HashSet<MultiKeyUntyped>());
            spawned.setParentEvaluator(this);
        }
    }

    public final void evaluateTrue(MatchedEventMap matchEvent, EvalStateNode fromNode, boolean isQuitted)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateTrue fromNode=" + fromNode  + "  isQuitted=" + isQuitted);
        }

        // determine if this evaluation has been seen before from the same node
        MultiKeyUntyped matchEventKey = PatternExpressionUtil.getKeys(matchEvent, everyDistinctNode);
        boolean haveSeenThis = false;
        Set<MultiKeyUntyped> keysFromNode = spawnedNodes.get(fromNode);
        if (keysFromNode != null)
        {
            if (keysFromNode.contains(matchEventKey))
            {
                haveSeenThis = true;
            }
            else
            {
                keysFromNode.add(matchEventKey);
            }
        }        

        if (isQuitted)
        {
            spawnedNodes.remove(fromNode);
        }

        // See explanation in EvalFilterStateNode for the type check
        if (fromNode instanceof EvalFilterStateNode)
        {
            // We do not need to newState new listeners here, since the filter state node below this node did not quit
        }
        else
        {
            // Spawn all nodes below this EVERY node
            // During the start of a child we need to use the temporary evaluator to catch any event created during a start
            // Such events can be raised when the "not" operator is used.
            EvalNode child = getFactoryNode().getChildNodes().get(0);
            EvalEveryStateSpawnEvaluator spawnEvaluator = new EvalEveryStateSpawnEvaluator(everyDistinctNode.getContext().getStatementName());
            EvalStateNode spawned = child.newState(spawnEvaluator, beginState, everyDistinctNode.getContext(), null);
            spawned.start();

            // If the whole spawned expression already turned true, quit it again
            if (spawnEvaluator.isEvaluatedTrue())
            {
                spawned.quit();
            }
            else
            {
                Set<MultiKeyUntyped> keyset = new HashSet<MultiKeyUntyped>();
                if (keysFromNode != null)
                {
                    keyset.addAll(keysFromNode);
                }
                spawnedNodes.put(spawned, keyset);
                spawned.setParentEvaluator(this);
            }
        }

        if (!haveSeenThis)
        {
            this.getParentEvaluator().evaluateTrue(matchEvent, this, false);
        }
    }

    public final void quit()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".quit Quitting EVERY-node all children");
        }

        // Stop all child nodes
        for (EvalStateNode child : spawnedNodes.keySet())
        {
            child.quit();
        }
    }

    public final Object accept(EvalStateNodeVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public final Object childrenAccept(EvalStateNodeVisitor visitor, Object data)
    {
        for (EvalStateNode spawnedNode : spawnedNodes.keySet())
        {
            spawnedNode.accept(visitor, data);
        }

        return data;
    }

    public final String toString()
    {
        return "EvalEveryStateNode spawnedChildren=" + spawnedNodes.size();
    }

    private static final Log log = LogFactory.getLog(EvalEveryStateNode.class);
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;


import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.util.ExecutionPathDebugLog;

/**
 * This class represents the state of an "and" operator in the evaluation state tree.
 */
public final class EvalAndStateNode extends EvalStateNode implements Evaluator
{
    private final EvalAndNode evalAndNode;
    private final List<EvalStateNode> activeChildNodes;
    private Map<EvalStateNode, List<MatchedEventMap>> eventsPerChild;

    /**
     * Constructor.
     * @param parentNode is the parent evaluator to call to indicate truth value
     * @param beginState contains the events that make up prior matches
     * @param evalAndNode is the factory node associated to the state
     */
    public EvalAndStateNode(Evaluator parentNode,
                                  EvalAndNode evalAndNode,
                                  MatchedEventMap beginState)
    {
        super(parentNode, null);

        this.evalAndNode = evalAndNode;
        this.activeChildNodes = new LinkedList<EvalStateNode>();
        this.eventsPerChild = new HashMap<EvalStateNode, List<MatchedEventMap>>();

        // In an "and" expression we need to create a state for all child listeners
        for (EvalNode node : evalAndNode.getChildNodes())
        {
            EvalStateNode childState = node.newState(this, beginState, evalAndNode.getContext(), null);
            activeChildNodes.add(childState);
        }
    }

    @Override
    public EvalNode getFactoryNode() {
        return evalAndNode;
    }

    public final void start()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".start Starting and-expression all children, size=" + getFactoryNode().getChildNodes().size());
        }

        if (activeChildNodes.size() < 2)
        {
            throw new IllegalStateException("AND state node is inactive");
        }

        // Start all child nodes
        EvalStateNode[] activeChildNodesArray = activeChildNodes.toArray(new EvalStateNode[activeChildNodes.size()]);
        for (EvalStateNode child : activeChildNodesArray)
        {
            child.start();
        }
    }

    public final void evaluateTrue(MatchedEventMap matchEvent, EvalStateNode fromNode, boolean isQuitted)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateTrue fromNode=" + fromNode.hashCode());
        }

        // If one of the children quits, remove the child
        if (isQuitted)
        {
            activeChildNodes.remove(fromNode);
        }

        if (eventsPerChild == null) {
            return;
        }

        // Add the event received to the list of events per child
        List<MatchedEventMap> eventList = eventsPerChild.get(fromNode);
        if (eventList == null)
        {
            eventList = new LinkedList<MatchedEventMap>();
            eventsPerChild.put(fromNode, eventList);
        }
        eventList.add(matchEvent);

        // If all nodes have events received, the AND expression turns true
        if (eventsPerChild.size() < getFactoryNode().getChildNodes().size())
        {
            return;
        }

        // For each combination in eventsPerChild for all other state nodes generate an event to the parent
        List<MatchedEventMap> result = generateMatchEvents(matchEvent, fromNode, eventsPerChild);

        // Check if this is quitting
        boolean quitted = true;
        if (!activeChildNodes.isEmpty())
        {
            for (EvalStateNode stateNode : activeChildNodes)
            {
                if (!(stateNode instanceof EvalNotStateNode))
                {
                    quitted = false;
                }
            }
        }

        // So we are quitting if all non-not child nodes have quit, since the not-node wait for evaluate false
        if (quitted)
        {
            quit();
        }

        // Send results to parent
        for (MatchedEventMap event : result)
        {
            this.getParentEvaluator().evaluateTrue(event, this, quitted);
        }
    }

    public final void evaluateFalse(EvalStateNode fromNode)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateFalse Removing fromNode=" + fromNode.hashCode());
        }

        if (eventsPerChild != null) {
            eventsPerChild.remove(fromNode);
        }

        // The and node cannot turn true anymore, might as well quit all child nodes
        this.getParentEvaluator().evaluateFalse(this);
        quit();
    }

    /**
     * Generate a list of matching event combinations constisting of the events per child that are passed in.
     * @param matchEvent can be populated with prior events that must be passed on
     * @param fromNode is the EvalStateNode that will not take part in the combinations produced.
     * @param eventsPerChild is the list of events for each child node to the "And" node.
     * @return list of events populated with all possible combinations
     */
    protected static List<MatchedEventMap> generateMatchEvents(MatchedEventMap matchEvent,
                                                              EvalStateNode fromNode,
                                                              Map<EvalStateNode, List<MatchedEventMap>> eventsPerChild)
    {
        // Place event list for each child state node into an array, excluding the node where the event came from
        ArrayList<List<MatchedEventMap>> listArray = new ArrayList<List<MatchedEventMap>>();
        int index = 0;
        for (Map.Entry<EvalStateNode, List<MatchedEventMap>> entry : eventsPerChild.entrySet())
        {
            if (fromNode != entry.getKey())
            {
                listArray.add(index++, entry.getValue());
            }
        }

        // Recusively generate MatchedEventMap instances for all accumulated events
        List<MatchedEventMap> results = new LinkedList<MatchedEventMap>();
        generateMatchEvents(listArray, 0, results, matchEvent);

        return results;
    }

    /**
     * For each combination of MatchedEventMap instance in all collections, add an entry to the list.
     * Recursive method.
     * @param eventList is an array of lists containing MatchedEventMap instances to combine
     * @param index is the current index into the array
     * @param result is the resulting list of MatchedEventMap
     * @param matchEvent is the start MatchedEventMap to generate from
     */
    protected static void generateMatchEvents(ArrayList<List<MatchedEventMap>> eventList,
                                                   int index,
                                                   List<MatchedEventMap> result,
                                                   MatchedEventMap matchEvent)
    {
        List<MatchedEventMap> events = eventList.get(index);

        for (MatchedEventMap event : events)
        {
            MatchedEventMap current = matchEvent.shallowCopy();
            current.merge(event);

            // If this is the very last list in the array of lists, add accumulated MatchedEventMap events to result
            if ((index + 1) == eventList.size())
            {
                result.add(current);
            }
            else
            {
                // make a copy of the event collection and hand to next list of events
                generateMatchEvents(eventList, index + 1, result, current);
            }
        }
    }

    public final void quit()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".quit Stopping all children");
        }

        for (EvalStateNode child : activeChildNodes)
        {
            child.quit();
        }
        activeChildNodes.clear();
        eventsPerChild = null;
    }

    public final Object accept(EvalStateNodeVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public final Object childrenAccept(EvalStateNodeVisitor visitor, Object data)
    {
        for (EvalStateNode node : activeChildNodes)
        {
            node.accept(visitor, data);
        }
        return data;
    }

    public final String toString()
    {
        return "EvalAndStateNode nodes=" + activeChildNodes.size();
    }

    private static final Log log = LogFactory.getLog(EvalAndStateNode.class);
}

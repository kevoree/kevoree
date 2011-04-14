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
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class represents the state of a match-until node in the evaluation state tree.
 */
public final class EvalMatchUntilStateNode extends EvalStateNode implements Evaluator, EvalStateNodeNonQuitting
{
    private final HashMap<EvalStateNode, Integer> nodes;
    private final EvalMatchUntilNode evalMatchUntilNode;
    private final MatchedEventMap beginState;
    private final ArrayList<EventBean>[] matchedEventArrays;

    private EvalStateNode stateMatcher;
    private EvalStateNode stateUntil;
    private int numMatches;
    private Integer lowerbounds;
    private Integer upperbounds;

    /**
     * Constructor.
     * @param parentNode is the parent evaluator to call to indicate truth value
     * @param beginState contains the events that make up prior matches
     * @param evalMatchUntilNode is the factory node associated to the state
     */
    public EvalMatchUntilStateNode(Evaluator parentNode,
                                         EvalMatchUntilNode evalMatchUntilNode,
                                         MatchedEventMap beginState)
    {
        super(parentNode, null);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".constructor");
        }

        this.nodes = new HashMap<EvalStateNode, Integer>();
        this.beginState = beginState;
        this.matchedEventArrays = (ArrayList<EventBean>[]) new ArrayList[evalMatchUntilNode.getTagsArrayed().length];
        this.evalMatchUntilNode = evalMatchUntilNode;

        EvalNode childMatcher = evalMatchUntilNode.getChildNodes().get(0);
        stateMatcher = childMatcher.newState(this, beginState, evalMatchUntilNode.getContext(), null);

        if (evalMatchUntilNode.getChildNodes().size() > 1)
        {
            EvalNode childUntil = evalMatchUntilNode.getChildNodes().get(1);
            stateUntil = childUntil.newState(this, beginState, evalMatchUntilNode.getContext(), null);
        }
    }

    @Override
    public EvalNode getFactoryNode() {
        return evalMatchUntilNode;
    }

    public final void start()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".start Starting match-until expression - matcher");
        }

        // start until first, it controls the expression
        // if the same event fires both match and until, the match should not count
        if (stateUntil != null)
        {
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug(".start Starting match-until expression - until");
            }
            stateUntil.start();
        }

        EventBean[] eventsPerStream = evalMatchUntilNode.getConvertor().convert(beginState);
        if (evalMatchUntilNode.getLowerBounds() != null) {
            lowerbounds = (Integer) evalMatchUntilNode.getLowerBounds().getExprEvaluator().evaluate(eventsPerStream, true, evalMatchUntilNode.getContext());
        }
        if (evalMatchUntilNode.getUpperBounds() != null) {
            upperbounds = (Integer) evalMatchUntilNode.getUpperBounds().getExprEvaluator().evaluate(eventsPerStream, true, evalMatchUntilNode.getContext());
        }
        if (upperbounds != null && lowerbounds != null) {
            if (upperbounds < lowerbounds) {
                Integer lbounds =  lowerbounds;
                lowerbounds = upperbounds;
                upperbounds = lbounds;
            }
        }

        if (stateMatcher != null) {
            stateMatcher.start();
        }
    }

    public final void evaluateTrue(MatchedEventMap matchEvent, EvalStateNode fromNode, boolean isQuitted)
    {
        boolean isMatcher = false;
        if (fromNode == stateMatcher)
        {
            // Add the additional tagged events to the list for later posting
            isMatcher = true;
            numMatches++;
            String[] tags = evalMatchUntilNode.getTagsArrayed();
            for (int i = 0; i < tags.length; i++)
            {
                Object event = matchEvent.getMatchingEventAsObject(tags[i]);
                if (event != null)
                {
                    if (matchedEventArrays[i] == null)
                    {
                        matchedEventArrays[i] = new ArrayList<EventBean>();
                    }
                    if (event instanceof EventBean) {
                        matchedEventArrays[i].add((EventBean) event);
                    }
                    else {
                        EventBean[] arrayEvents = (EventBean[]) event;
                        matchedEventArrays[i].addAll(Arrays.asList(arrayEvents));
                    }

                }
            }
        }

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateTrue isMatcher=" + isMatcher + "  fromNode=" + fromNode.hashCode() + "  isQuitted=" + isQuitted);
        }

        if (isQuitted)
        {
            if (isMatcher)
            {
                stateMatcher = null;
            }
            else
            {
                stateUntil = null;
            }
        }

        // handle matcher evaluating true
        if (isMatcher)
        {
            if ((isTightlyBound()) && (numMatches == lowerbounds))
            {
                quit();
                MatchedEventMap consolidated = consolidate(matchEvent, matchedEventArrays, evalMatchUntilNode.getTagsArrayed());
                this.getParentEvaluator().evaluateTrue(consolidated, this, true);
            }
            else
            {
                // restart or keep started if not bounded, or not upper bounds, or upper bounds not reached
                boolean restart = (!isBounded()) ||
                                  (upperbounds == null) ||
                                  (upperbounds > numMatches);
                if (stateMatcher == null)
                {
                    if (restart)
                    {
                        EvalNode childMatcher = evalMatchUntilNode.getChildNodes().get(0);
                        stateMatcher = childMatcher.newState(this, beginState, evalMatchUntilNode.getContext(), null);
                        stateMatcher.start();
                    }
                }
                else
                {
                    if (!restart)
                    {
                        stateMatcher.quit();
                        stateMatcher = null;
                    }
                }
            }
        }
        else
        // handle until-node
        {
            quit();

            // consolidate multiple matched events into a single event
            MatchedEventMap consolidated = consolidate(matchEvent, matchedEventArrays, evalMatchUntilNode.getTagsArrayed());

            if ((lowerbounds != null) && (numMatches < lowerbounds))
            {
                this.getParentEvaluator().evaluateFalse(this);
            }
            else
            {
                this.getParentEvaluator().evaluateTrue(consolidated, this, true);
            }
        }
    }

    private static MatchedEventMap consolidate(MatchedEventMap beginState, ArrayList<EventBean>[] matchedEventList, String[] tagsArrayed)
    {
        if (tagsArrayed == null)
        {
            return beginState;
        }

        for (int i = 0; i < tagsArrayed.length; i++)
        {
            if (matchedEventList[i] == null)
            {
                continue;
            }
            EventBean[] eventsForTag = matchedEventList[i].toArray(new EventBean[matchedEventList[i].size()]);
            beginState.add(tagsArrayed[i], eventsForTag);
        }

        return beginState;
    }

    public final void evaluateFalse(EvalStateNode fromNode)
    {
        boolean isMatcher = false;
        if (fromNode == stateMatcher)
        {
            isMatcher = true;
        }

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".evaluateFalse Child node has indicated permanently false, isMatcher=" + isMatcher);
        }

        if (isMatcher)
        {
            stateMatcher.quit();
            stateMatcher = null;
        }
        else
        {
            stateUntil.quit();
            stateUntil = null;
            this.getParentEvaluator().evaluateFalse(this);
        }
    }

    public final void quit()
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".quit Stopping match-until children");
        }

        if (stateMatcher != null)
        {
            stateMatcher.quit();
            stateMatcher = null;
        }
        if (stateUntil != null)
        {
            stateUntil.quit();
            stateUntil = null;
        }
    }

    public final Object accept(EvalStateNodeVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public final Object childrenAccept(EvalStateNodeVisitor visitor, Object data)
    {
        for (EvalStateNode node : nodes.keySet())
        {
            node.accept(visitor, data);
        }
        return data;
    }

    public final String toString()
    {
        return "EvalMatchUntilStateNode nodes=" + nodes.size();
    }

    private boolean isTightlyBound() {
        return lowerbounds != null && upperbounds != null && upperbounds.equals(lowerbounds);
    }

    private boolean isBounded() {
        return lowerbounds != null || upperbounds != null;
    }

    private static final Log log = LogFactory.getLog(EvalMatchUntilStateNode.class);

}

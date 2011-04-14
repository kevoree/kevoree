package com.espertech.esper.rowregex;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Partition-by implementation for partition state.
 */
public class RegexPartitionStateRepoGroup implements RegexPartitionStateRepo
{
    /**
     * Empty state collection initial threshold.
     */
    public final static int INITIAL_COLLECTION_MIN = 100;

    private final RegexPartitionStateRandomAccessGetter getter;
    private final Map<MultiKeyUntyped, RegexPartitionState> states;
    private final boolean hasInterval;
    private final ExprEvaluator[] partitionExpressions;
    private final EventBean[] eventsPerStream = new EventBean[1];
    private final ExprEvaluatorContext exprEvaluatorContext;
    private int currentCollectionSize = INITIAL_COLLECTION_MIN;

    /**
     * Ctor.
     * @param getter for "prev" function access
     * @param partitionExpressions expressions for computing group key
     * @param hasInterval true for interval
     * @param exprEvaluatorContext context for evaluating expressions
     */
    public RegexPartitionStateRepoGroup(RegexPartitionStateRandomAccessGetter getter,
                                        ExprEvaluator[] partitionExpressions,
                                        boolean hasInterval,
                                        ExprEvaluatorContext exprEvaluatorContext)
    {
        this.getter = getter;
        this.partitionExpressions  = partitionExpressions;
        this.hasInterval = hasInterval;
        this.states = new HashMap<MultiKeyUntyped, RegexPartitionState>();
        this.exprEvaluatorContext = exprEvaluatorContext;
    }

    public RegexPartitionStateRepo copyForIterate()
    {
        RegexPartitionStateRepoGroup copy = new RegexPartitionStateRepoGroup(getter, partitionExpressions, hasInterval, exprEvaluatorContext);
        for (Map.Entry<MultiKeyUntyped, RegexPartitionState> entry : states.entrySet())
        {
            copy.states.put(entry.getKey(), new RegexPartitionState(entry.getValue().getRandomAccess(), entry.getKey(), hasInterval));
        }
        return copy;
    }

    public void removeOld(EventBean[] oldData, boolean isEmpty, boolean[] found)
    {
        if (isEmpty)
        {
            if (getter == null)
            {
                // no "prev" used, clear all state
                states.clear();
            }
            else
            {
                for (Map.Entry<MultiKeyUntyped, RegexPartitionState> entry : states.entrySet())
                {
                    entry.getValue().getCurrentStates().clear();
                }
            }

            // clear "prev" state
            if (getter != null)
            {
                // we will need to remove event-by-event
                for (int i = 0; i < oldData.length; i++)
                {
                    RegexPartitionState partitionState = getState(oldData[i], true);
                    if (partitionState == null)
                    {
                        continue;
                    }
                    partitionState.removeEventFromPrev(oldData);
                }
            }

            return;
        }

        // we will need to remove event-by-event
        for (int i = 0; i < oldData.length; i++)
        {
            RegexPartitionState partitionState = getState(oldData[i], true);
            if (partitionState == null)
            {
                continue;
            }

            if (found[i])
            {
                boolean cleared = partitionState.removeEventFromState(oldData[i]);
                if (cleared)
                {
                    if (getter == null)
                    {
                        states.remove(partitionState.getOptionalKeys());
                    }
                }
            }

            partitionState.removeEventFromPrev(oldData[i]);
        }
    }
   
    public RegexPartitionState getState(MultiKeyUntyped key)
    {
        return states.get(key);
    }

    public RegexPartitionState getState(EventBean event, boolean isCollect)
    {
        // collect unused states
        if ((isCollect) && (states.size() >= currentCollectionSize))
        {
            List<MultiKeyUntyped> removeList = new ArrayList<MultiKeyUntyped>();
            for (Map.Entry<MultiKeyUntyped, RegexPartitionState> entry : states.entrySet())
            {
                if ((entry.getValue().getCurrentStates().isEmpty()) &&
                    (entry.getValue().getRandomAccess() == null || entry.getValue().getRandomAccess().isEmpty()))
                {
                    removeList.add(entry.getKey());
                }
            }

            for (MultiKeyUntyped removeKey : removeList)
            {
                states.remove(removeKey);
            }

            if (removeList.size() < (currentCollectionSize / 5))
            {
                currentCollectionSize *= 2;
            }
        }

        MultiKeyUntyped key = getKeys(event);
        
        RegexPartitionState state = states.get(key);
        if (state != null)
        {
            return state;
        }

        state = new RegexPartitionState(getter, new ArrayList<RegexNFAStateEntry>(), key, hasInterval);
        states.put(key, state);

        return state;
    }

    private MultiKeyUntyped getKeys(EventBean event)
    {
        Object[] keys = new Object[partitionExpressions.length];
        eventsPerStream[0] = event;
        int count = 0;
        for (ExprEvaluator node : partitionExpressions)
        {
            keys[count++] = node.evaluate(eventsPerStream, true, exprEvaluatorContext);
        }
        return new MultiKeyUntyped(keys);
    }
}
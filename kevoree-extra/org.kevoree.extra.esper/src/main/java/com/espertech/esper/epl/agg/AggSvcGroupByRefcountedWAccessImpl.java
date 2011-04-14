/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for handling aggregation with grouping by group-keys.
 */
public class AggSvcGroupByRefcountedWAccessImpl extends AggregationServiceBase
{
    private final AggregationAccessorSlotPair[] accessors;
    private final int[] streams;
    private final boolean isJoin;

    // maintain for each group a row of aggregator states that the expression node canb pull the data from via index
    private Map<MultiKeyUntyped, AggregationMethodPairRow> aggregatorsPerGroup;

    // maintain a current row for random access into the aggregator state table
    // (row=groups, columns=expression nodes that have aggregation functions)
    private AggregationMethod[] currentAggregatorMethods;
    private AggregationAccess[] currentAggregatorAccesses;

    private MethodResolutionService methodResolutionService;

    private List<MultiKeyUntyped> removedKeys;

    /**
     * Ctor.
     * @param evaluators - evaluate the sub-expression within the aggregate function (ie. sum(4*myNum))
     * @param prototypes - collect the aggregation state that evaluators evaluate to, act as prototypes for new aggregations
     * aggregation states for each group
     * @param methodResolutionService - factory for creating additional aggregation method instances per group key
     * @param accessors accessor definitions
     * @param streams streams in join
     * @param isJoin true for join, false for single-stream
     */
    public AggSvcGroupByRefcountedWAccessImpl(ExprEvaluator evaluators[],
                                       AggregationMethod prototypes[],
                                       MethodResolutionService methodResolutionService,
                                       AggregationAccessorSlotPair[] accessors,
                                       int[] streams,
                                       boolean isJoin)
    {
        super(evaluators, prototypes);
        this.methodResolutionService = methodResolutionService;
        this.aggregatorsPerGroup = new HashMap<MultiKeyUntyped, AggregationMethodPairRow>();
        this.accessors = accessors;
        this.streams = streams;
        this.isJoin = isJoin;
        removedKeys = new ArrayList<MultiKeyUntyped>();
    }

    public void clearResults()
    {
        aggregatorsPerGroup.clear();
    }

    public void applyEnter(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (!removedKeys.isEmpty())     // we collect removed keys lazily on the next enter to reduce the chance of empty-group queries creating empty aggregators temporarily
        {
            for (MultiKeyUntyped removedKey : removedKeys)
            {
                aggregatorsPerGroup.remove(removedKey);
            }
            removedKeys.clear();
        }

        AggregationMethodPairRow row = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        AggregationMethod[] groupAggregators;
        AggregationAccess[] groupAccesses;
        if (row == null)
        {
            groupAggregators = methodResolutionService.newAggregators(aggregators, groupByKey);
            groupAccesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
            row = new AggregationMethodPairRow(methodResolutionService.getCurrentRowCount(groupAggregators, groupAccesses) + 1, groupAggregators, groupAccesses);
            aggregatorsPerGroup.put(groupByKey, row);
        }
        else
        {
            groupAggregators = row.getMethods();
            groupAccesses = row.getAccesses();
            row.increaseRefcount();
        }

        currentAggregatorMethods = groupAggregators;
        currentAggregatorAccesses = groupAccesses;

        // For this row, evaluate sub-expressions, enter result
        for (int j = 0; j < evaluators.length; j++)
        {
            Object columnResult = evaluators[j].evaluate(eventsPerStream, true, exprEvaluatorContext);
            groupAggregators[j].enter(columnResult);
        }
        for (AggregationAccess access : currentAggregatorAccesses) {
            access.applyEnter(eventsPerStream);
        }
    }

    public void applyLeave(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        AggregationMethodPairRow row = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        AggregationMethod[] groupAggregators;
        AggregationAccess[] groupAccesses;
        if (row != null)
        {
            groupAggregators = row.getMethods();
            groupAccesses = row.getAccesses();
        }
        else
        {
            groupAggregators = methodResolutionService.newAggregators(aggregators, groupByKey);
            groupAccesses = AggregationAccessUtil.getNewAccesses(isJoin,  streams, methodResolutionService, groupByKey);
            row = new AggregationMethodPairRow(methodResolutionService.getCurrentRowCount(groupAggregators, groupAccesses) + 1, groupAggregators, groupAccesses);
            aggregatorsPerGroup.put(groupByKey, row);
        }

        currentAggregatorMethods = groupAggregators;
        currentAggregatorAccesses = groupAccesses;

        // For this row, evaluate sub-expressions, enter result
        for (int j = 0; j < evaluators.length; j++)
        {
            Object columnResult = evaluators[j].evaluate(eventsPerStream, false, exprEvaluatorContext);
            groupAggregators[j].leave(columnResult);
        }
        for (AggregationAccess access : currentAggregatorAccesses) {
            access.applyLeave(eventsPerStream);
        }

        row.decreaseRefcount();
        if (row.getRefcount() <= 0)
        {
            removedKeys.add(groupByKey);
            methodResolutionService.removeAggregators(groupByKey);  // allow persistence to remove keys already
        }
    }

    public void setCurrentAccess(MultiKeyUntyped groupByKey)
    {
        AggregationMethodPairRow row = aggregatorsPerGroup.get(groupByKey);

        if (row != null)
        {
            currentAggregatorMethods = row.getMethods();
            currentAggregatorAccesses = row.getAccesses();
        }
        else
        {
            currentAggregatorMethods = null;
        }

        if (currentAggregatorMethods == null)
        {
            currentAggregatorMethods = methodResolutionService.newAggregators(aggregators, groupByKey);
            currentAggregatorAccesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
        }
    }

    public Object getValue(int column)
    {
        if (column < aggregators.length) {
            return currentAggregatorMethods[column].getValue();
        }
        else {
            AggregationAccessorSlotPair pair = accessors[column - aggregators.length];
            return pair.getAccessor().getValue(currentAggregatorAccesses[pair.getSlot()]);
        }
    }
}

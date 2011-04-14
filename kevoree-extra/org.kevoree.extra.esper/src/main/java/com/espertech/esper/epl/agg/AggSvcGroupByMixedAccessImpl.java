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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for handling aggregation with grouping by group-keys.
 */
public class AggSvcGroupByMixedAccessImpl extends AggregationServiceBase
{
    private final AggregationAccessorSlotPair[] accessors;
    private final int[] streams;
    private final boolean isJoin;

    // maintain for each group a row of aggregator states that the expression node canb pull the data from via index
    private Map<MultiKeyUntyped, AggregationRowPair> aggregatorsPerGroup;

    // maintain a current row for random access into the aggregator state table
    // (row=groups, columns=expression nodes that have aggregation functions)
    private AggregationRowPair currentAggregatorRow;

    private MethodResolutionService methodResolutionService;

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
    public AggSvcGroupByMixedAccessImpl(ExprEvaluator evaluators[],
                                        AggregationMethod prototypes[],
                                        MethodResolutionService methodResolutionService,
                                        AggregationAccessorSlotPair[] accessors,
                                        int[] streams,
                                        boolean isJoin)
    {
        super(evaluators, prototypes);
        this.accessors = accessors;
        this.streams = streams;
        this.isJoin = isJoin;
        this.methodResolutionService = methodResolutionService;
        this.aggregatorsPerGroup = new HashMap<MultiKeyUntyped, AggregationRowPair>();
    }

    public void clearResults()
    {
        aggregatorsPerGroup.clear();
    }

    public void applyEnter(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        AggregationRowPair groupAggregators = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        if (groupAggregators == null)
        {
            AggregationMethod[] methods = methodResolutionService.newAggregators(aggregators, groupByKey);
            AggregationAccess[] accesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
            groupAggregators = new AggregationRowPair(methods, accesses);
            aggregatorsPerGroup.put(groupByKey, groupAggregators);
        }
        currentAggregatorRow = groupAggregators;

        // For this row, evaluate sub-expressions, enter result
        AggregationMethod[] groupAggMethods = groupAggregators.getMethods();
        for (int j = 0; j < evaluators.length; j++)
        {
            Object columnResult = evaluators[j].evaluate(eventsPerStream, true, exprEvaluatorContext);
            groupAggMethods[j].enter(columnResult);
        }
        for (AggregationAccess access : currentAggregatorRow.getAccesses()) {
            access.applyEnter(eventsPerStream);
        }
    }

    public void applyLeave(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        AggregationRowPair groupAggregators = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        if (groupAggregators == null)
        {
            AggregationMethod[] methods = methodResolutionService.newAggregators(aggregators, groupByKey);
            AggregationAccess[] accesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
            groupAggregators = new AggregationRowPair(methods, accesses);
            aggregatorsPerGroup.put(groupByKey, groupAggregators);
        }
        currentAggregatorRow = groupAggregators;

        // For this row, evaluate sub-expressions, enter result
        AggregationMethod[] groupAggMethods = groupAggregators.getMethods();
        for (int j = 0; j < evaluators.length; j++)
        {
            Object columnResult = evaluators[j].evaluate(eventsPerStream, false, exprEvaluatorContext);
            groupAggMethods[j].leave(columnResult);
        }
        for (AggregationAccess access : currentAggregatorRow.getAccesses()) {
            access.applyLeave(eventsPerStream);
        }
    }

    public void setCurrentAccess(MultiKeyUntyped groupByKey)
    {
        currentAggregatorRow = aggregatorsPerGroup.get(groupByKey);

        if (currentAggregatorRow == null)
        {
            AggregationMethod[] methods = methodResolutionService.newAggregators(aggregators, groupByKey);
            AggregationAccess[] accesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
            currentAggregatorRow = new AggregationRowPair(methods, accesses);
            aggregatorsPerGroup.put(groupByKey, currentAggregatorRow);
        }
    }

    public Object getValue(int column)
    {
        if (column < aggregators.length) {
            return currentAggregatorRow.getMethods()[column].getValue();
        }
        else {
            AggregationAccessorSlotPair pair = accessors[column - aggregators.length];
            return pair.getAccessor().getValue(currentAggregatorRow.getAccesses()[pair.getSlot()]);
        }
    }
}
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
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for handling aggregation with grouping by group-keys.
 */
public class AggSvcGroupByRefcountedNoAccessImpl extends AggregationServiceBase
{
    // maintain for each group a row of aggregator states that the expression node canb pull the data from via index
    private Map<MultiKeyUntyped, AggregationMethodRow> aggregatorsPerGroup;

    // maintain a current row for random access into the aggregator state table
    // (row=groups, columns=expression nodes that have aggregation functions)
    private AggregationMethod[] currentAggregatorRow;

    private MethodResolutionService methodResolutionService;

    private List<MultiKeyUntyped> removedKeys;

    /**
     * Ctor.
     * @param evaluators - evaluate the sub-expression within the aggregate function (ie. sum(4*myNum))
     * @param prototypes - collect the aggregation state that evaluators evaluate to, act as prototypes for new aggregations
     * aggregation states for each group
     * @param methodResolutionService - factory for creating additional aggregation method instances per group key
     */
    public AggSvcGroupByRefcountedNoAccessImpl(ExprEvaluator evaluators[],
                                       AggregationMethod prototypes[],
                                       MethodResolutionService methodResolutionService)
    {
        super(evaluators, prototypes);
        this.methodResolutionService = methodResolutionService;
        this.aggregatorsPerGroup = new HashMap<MultiKeyUntyped, AggregationMethodRow>();
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

        AggregationMethodRow row = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        AggregationMethod[] groupAggregators;
        if (row == null)
        {
            groupAggregators = methodResolutionService.newAggregators(aggregators, groupByKey);
            row = new AggregationMethodRow(methodResolutionService.getCurrentRowCount(groupAggregators, null) + 1, groupAggregators);
            aggregatorsPerGroup.put(groupByKey, row);
        }
        else
        {
            groupAggregators = row.getMethods();
            row.increaseRefcount();
        }

        currentAggregatorRow = groupAggregators;

        // For this row, evaluate sub-expressions, enter result
        for (int j = 0; j < evaluators.length; j++)
        {
            Object columnResult = evaluators[j].evaluate(eventsPerStream, true, exprEvaluatorContext);
            groupAggregators[j].enter(columnResult);
        }
    }

    public void applyLeave(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        AggregationMethodRow row = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        AggregationMethod[] groupAggregators;
        if (row != null)
        {
            groupAggregators = row.getMethods();
        }
        else
        {
            groupAggregators = methodResolutionService.newAggregators(aggregators, groupByKey);
            row = new AggregationMethodRow(methodResolutionService.getCurrentRowCount(groupAggregators, null) + 1, groupAggregators);
            aggregatorsPerGroup.put(groupByKey, row);
        }
        currentAggregatorRow = groupAggregators;

        // For this row, evaluate sub-expressions, enter result
        for (int j = 0; j < evaluators.length; j++)
        {
            Object columnResult = evaluators[j].evaluate(eventsPerStream, false, exprEvaluatorContext);
            groupAggregators[j].leave(columnResult);
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
        AggregationMethodRow row = aggregatorsPerGroup.get(groupByKey);

        if (row != null)
        {
            currentAggregatorRow = row.getMethods();
        }
        else
        {
            currentAggregatorRow = null;
        }

        if (currentAggregatorRow == null)
        {
            currentAggregatorRow = methodResolutionService.newAggregators(aggregators, groupByKey);
        }
    }

    public Object getValue(int column)
    {
        return currentAggregatorRow[column].getValue();
    }
}

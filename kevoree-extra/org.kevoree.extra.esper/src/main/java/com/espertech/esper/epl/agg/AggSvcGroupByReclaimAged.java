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
import com.espertech.esper.client.annotation.Hint;
import com.espertech.esper.client.annotation.HintEnum;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.variable.VariableChangeCallback;
import com.espertech.esper.epl.variable.VariableReader;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.type.DoubleValue;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.StatementStopCallback;
import com.espertech.esper.view.StatementStopService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Implementation for handling aggregation with grouping by group-keys.
 */
public class AggSvcGroupByReclaimAged extends AggregationServiceBase
{
    private static final Log log = LogFactory.getLog(AggSvcGroupByReclaimAged.class);

    private static final long DEFAULT_MAX_AGE_MSEC = 60000L;

    private final AggregationAccessorSlotPair[] accessors;
    private final int[] streams;
    private final boolean isJoin;

    // maintain for each group a row of aggregator states that the expression node canb pull the data from via index
    private Map<MultiKeyUntyped, AggregationMethodRowAged> aggregatorsPerGroup;

    // maintain a current row for random access into the aggregator state table
    // (row=groups, columns=expression nodes that have aggregation functions)
    private AggregationMethod[] currentAggregatorMethods;
    private AggregationAccess[] currentAggregatorAccesses;

    private MethodResolutionService methodResolutionService;

    private List<MultiKeyUntyped> removedKeys;
    private final EvaluationFunction evaluationFunctionMaxAge;
    private final EvaluationFunction evaluationFunctionFrequency;
    private Long nextSweepTime = null;
    private volatile long currentMaxAge = DEFAULT_MAX_AGE_MSEC;
    private volatile long currentReclaimFrequency = DEFAULT_MAX_AGE_MSEC;

    /**
     * Ctor.
     * @param evaluators - evaluate the sub-expression within the aggregate function (ie. sum(4*myNum))
     * @param prototypes - collect the aggregation state that evaluators evaluate to, act as prototypes for new aggregations
     * aggregation states for each group
     * @param methodResolutionService - factory for creating additional aggregation method instances per group key
     * @param reclaimGroupAged hint to reclaim
     * @param reclaimGroupFrequency hint to reclaim
     * @param variableService variables
     * @param statementStopService stop svc
     * @param accessors accessor definitions
     * @param streams streams in join
     * @param isJoin true for join, false for single-stream
     * @throws com.espertech.esper.epl.expression.ExprValidationException when validation fails
     */
    public AggSvcGroupByReclaimAged(ExprEvaluator evaluators[],
                                                AggregationMethod prototypes[],
                                                MethodResolutionService methodResolutionService,
                                                Hint reclaimGroupAged,
                                                Hint reclaimGroupFrequency,
                                                final VariableService variableService,
                                                StatementStopService statementStopService,
                                                AggregationAccessorSlotPair[] accessors,
                                                int[] streams,
                                                boolean isJoin)
            throws ExprValidationException
    {
        super(evaluators, prototypes);
        this.methodResolutionService = methodResolutionService;
        this.aggregatorsPerGroup = new HashMap<MultiKeyUntyped, AggregationMethodRowAged>();
        this.accessors = accessors;
        this.streams = streams;
        this.isJoin = isJoin;
        removedKeys = new ArrayList<MultiKeyUntyped>();

        String hintValueMaxAge = HintEnum.RECLAIM_GROUP_AGED.getHintAssignedValue(reclaimGroupAged);
        if (hintValueMaxAge == null)
        {
            throw new ExprValidationException("Required hint value for hint '" + HintEnum.RECLAIM_GROUP_AGED + "' has not been provided");
        }
        evaluationFunctionMaxAge = getEvaluationFunction(variableService, statementStopService, hintValueMaxAge);

        String hintValueFrequency = HintEnum.RECLAIM_GROUP_FREQ.getHintAssignedValue(reclaimGroupAged);
        if ((reclaimGroupFrequency == null) || (hintValueFrequency == null))
        {
            evaluationFunctionFrequency = evaluationFunctionMaxAge;
            currentReclaimFrequency = getReclaimFrequency(currentReclaimFrequency);
        }
        else
        {
            evaluationFunctionFrequency = getEvaluationFunction(variableService, statementStopService, hintValueFrequency);
        }
    }

    private EvaluationFunction getEvaluationFunction(final VariableService variableService, StatementStopService statementStopService, String hintValue)
            throws ExprValidationException
    {
        final VariableReader variableReader = variableService.getReader(hintValue);
        if (variableReader != null)
        {
            if (!JavaClassHelper.isNumeric(variableReader.getType()))
            {
                throw new ExprValidationException("Variable type of variable '" + variableReader.getVariableName() + "' is not numeric");
            }

            final VariableChangeCallback changeCallback = new VariableChangeCallback()
            {
                public void update(Object newValue, Object oldValue)
                {
                    AggSvcGroupByReclaimAged.this.nextSweepTime = null;
                }
            };
            variableService.registerCallback(variableReader.getVariableNumber(), changeCallback);
            statementStopService.addSubscriber(new StatementStopCallback()
            {
                public void statementStopped()
                {
                    variableService.unregisterCallback(variableReader.getVariableNumber(), changeCallback);
                }
            });

            return new EvaluationFunctionVariable(variableReader);
        }
        else
        {
            Double valueDouble;
            try
            {
                valueDouble = DoubleValue.parseString(hintValue);
            }
            catch (RuntimeException ex)
            {
                throw new ExprValidationException("Failed to parse hint parameter value '" + hintValue + "' as a double-typed seconds value or variable name");
            }
            if (valueDouble <= 0)
            {
                throw new ExprValidationException("Hint parameter value '" + hintValue + "' is an invalid value, expecting a double-typed seconds value or variable name");
            }
            return new EvaluationFunctionConstant(valueDouble);
        }
    }

    public void clearResults()
    {
        aggregatorsPerGroup.clear();
    }

    public void applyEnter(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        long currentTime = exprEvaluatorContext.getTimeProvider().getTime();
        if ((nextSweepTime == null) || (nextSweepTime <= currentTime))
        {
            currentMaxAge = getMaxAge(currentMaxAge);
            currentReclaimFrequency = getReclaimFrequency(currentReclaimFrequency);
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug("Reclaiming groups older then " + currentMaxAge + " msec and every " + currentReclaimFrequency + "msec in frequency");
            }
            nextSweepTime = currentTime + currentReclaimFrequency;
            sweep(currentTime, currentMaxAge);
        }

        if (!removedKeys.isEmpty())     // we collect removed keys lazily on the next enter to reduce the chance of empty-group queries creating empty aggregators temporarily
        {
            for (MultiKeyUntyped removedKey : removedKeys)
            {
                aggregatorsPerGroup.remove(removedKey);
            }
            removedKeys.clear();
        }

        AggregationMethodRowAged row = aggregatorsPerGroup.get(groupByKey);

        // The aggregators for this group do not exist, need to create them from the prototypes
        AggregationMethod[] groupAggregators;
        AggregationAccess[] groupAccesses;
        if (row == null)
        {
            groupAggregators = methodResolutionService.newAggregators(aggregators, groupByKey);
            groupAccesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
            row = new AggregationMethodRowAged(methodResolutionService.getCurrentRowCount(groupAggregators, groupAccesses) + 1, currentTime, groupAggregators, groupAccesses);
            aggregatorsPerGroup.put(groupByKey, row);
        }
        else
        {
            groupAggregators = row.getMethods();
            groupAccesses = row.getAccesses();
            row.increaseRefcount();
            row.setLastUpdateTime(currentTime);
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

    private void sweep(long currentTime, long currentMaxAge)
    {
        ArrayDeque<MultiKeyUntyped> removed = new ArrayDeque<MultiKeyUntyped>();
        for (Map.Entry<MultiKeyUntyped, AggregationMethodRowAged> entry : aggregatorsPerGroup.entrySet())
        {
            long age = currentTime - entry.getValue().getLastUpdateTime();
            if (age > currentMaxAge)
            {
                removed.add(entry.getKey());
            }
        }

        for (MultiKeyUntyped key : removed)
        {
            aggregatorsPerGroup.remove(key);
        }
    }

    private long getMaxAge(long currentMaxAge)
    {
        Double maxAge = evaluationFunctionMaxAge.getLongValue();
        if ((maxAge == null) || (maxAge <= 0))
        {
            return currentMaxAge;
        }
        return Math.round(maxAge * 1000d);
    }

    private long getReclaimFrequency(long currentReclaimFrequency)
    {
        Double frequency = evaluationFunctionFrequency.getLongValue();
        if ((frequency == null) || (frequency <= 0))
        {
            return currentReclaimFrequency;
        }
        return Math.round(frequency * 1000d);
    }

    public void applyLeave(EventBean[] eventsPerStream, MultiKeyUntyped groupByKey, ExprEvaluatorContext exprEvaluatorContext)
    {
        AggregationMethodRowAged row = aggregatorsPerGroup.get(groupByKey);
        long currentTime = exprEvaluatorContext.getTimeProvider().getTime();

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
            groupAccesses = AggregationAccessUtil.getNewAccesses(isJoin, streams, methodResolutionService, groupByKey);
            row = new AggregationMethodRowAged(methodResolutionService.getCurrentRowCount(groupAggregators, groupAccesses) + 1, currentTime, groupAggregators, groupAccesses);
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
        row.setLastUpdateTime(currentTime);
        if (row.getRefcount() <= 0)
        {
            removedKeys.add(groupByKey);
            methodResolutionService.removeAggregators(groupByKey);  // allow persistence to remove keys already
        }
    }

    public void setCurrentAccess(MultiKeyUntyped groupByKey)
    {
        AggregationMethodRowAged row = aggregatorsPerGroup.get(groupByKey);

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

    private static interface EvaluationFunction
    {
        public Double getLongValue();
    }

    private static class EvaluationFunctionConstant implements EvaluationFunction
    {
        private final double longValue;

        private EvaluationFunctionConstant(double longValue)
        {
            this.longValue = longValue;
        }

        public Double getLongValue()
        {
            return longValue;
        }
    }

    private static class EvaluationFunctionVariable implements EvaluationFunction
    {
        private VariableReader variableReader;

        private EvaluationFunctionVariable(VariableReader variableReader)
        {
            this.variableReader = variableReader;
        }

        public Double getLongValue()
        {
            Object val = variableReader.getValue();
            if ((val != null) && (val instanceof Number))
            {
                return ((Number) val).doubleValue();
            }
            log.warn("Variable '" + variableReader.getVariableName() + " returned a null value, using last valid value");
            return null;
        }
    }
}
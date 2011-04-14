/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.spec.OutputLimitLimitType;
import com.espertech.esper.epl.spec.OutputLimitSpec;
import com.espertech.esper.epl.view.OutputConditionPolled;
import com.espertech.esper.epl.view.OutputConditionPolledFactory;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Result-set processor for the aggregate-grouped case:
 * there is a group-by and one or more non-aggregation event properties in the select clause are not listed in the group by,
 * and there are aggregation functions.
 * <p>
 * This processor does perform grouping by computing MultiKey group-by keys for each row.
 * The processor generates one row for each event entering (new event) and one row for each event leaving (old event).
 * <p>
 * Aggregation state is a table of rows held by {@link AggregationService} where the row key is the group-by MultiKey.
 */
public class ResultSetProcessorAggregateGrouped implements ResultSetProcessor
{
    private static final Log log = LogFactory.getLog(ResultSetProcessorAggregateGrouped.class);

    private final SelectExprProcessor selectExprProcessor;
    private final OrderByProcessor orderByProcessor;
    private final AggregationService aggregationService;
    private final ExprEvaluator[] groupKeyNodes;
    private final ExprEvaluator optionalHavingNode;
    private final boolean isSorting;
    private final boolean isSelectRStream;
    private final boolean isUnidirectional;
    private final StatementContext statementContext;
    private final OutputLimitSpec outputLimitSpec;
    private final EventBean[] eventsPerStreamOneStream = new EventBean[1];

    // For output limiting, keep a representative of each group-by group
    private final Map<MultiKeyUntyped, EventBean[]> eventGroupReps = new HashMap<MultiKeyUntyped, EventBean[]>();
    private final Map<MultiKeyUntyped, EventBean[]> workCollection = new LinkedHashMap<MultiKeyUntyped, EventBean[]>();
    private final Map<MultiKeyUntyped, EventBean[]> workCollectionTwo = new LinkedHashMap<MultiKeyUntyped, EventBean[]>();

    // For sorting, keep the generating events for each outgoing event
    private final Map<MultiKeyUntyped, EventBean[]> newGenerators = new HashMap<MultiKeyUntyped, EventBean[]>();
	private final Map<MultiKeyUntyped, EventBean[]> oldGenerators = new HashMap<MultiKeyUntyped, EventBean[]>();

    private final Map<MultiKeyUntyped, OutputConditionPolled> outputState = new HashMap<MultiKeyUntyped, OutputConditionPolled>();

    /**
     * Ctor.
     * @param selectExprProcessor - for processing the select expression and generting the final output rows
     * @param orderByProcessor - for sorting outgoing events according to the order-by clause
     * @param aggregationService - handles aggregation
     * @param groupKeyNodes - list of group-by expression nodes needed for building the group-by keys
     * @param optionalHavingNode - expression node representing validated HAVING clause, or null if none given.
     * Aggregation functions in the having node must have been pointed to the AggregationService for evaluation.
     * @param isSelectRStream - true if remove stream events should be generated
     * @param isUnidirectional - true if unidirectional join
     */
    public ResultSetProcessorAggregateGrouped(SelectExprProcessor selectExprProcessor,
                                      		  OrderByProcessor orderByProcessor,
                                      		  AggregationService aggregationService,
                                      		  ExprEvaluator[] groupKeyNodes,
                                      		  ExprEvaluator optionalHavingNode,
                                              boolean isSelectRStream,
                                              boolean isUnidirectional,
                                              StatementContext statementContext,
                                              OutputLimitSpec outputLimitSpec)
    {
        this.selectExprProcessor = selectExprProcessor;
        this.orderByProcessor = orderByProcessor;
        this.aggregationService = aggregationService;
        this.groupKeyNodes = groupKeyNodes;
        this.optionalHavingNode = optionalHavingNode;
        this.isSorting = orderByProcessor != null;
        this.isSelectRStream = isSelectRStream;
        this.isUnidirectional = isUnidirectional;
        this.statementContext = statementContext;
        this.outputLimitSpec = outputLimitSpec;
    }

    public EventType getResultEventType()
    {
        return selectExprProcessor.getResultEventType();
    }

    public UniformPair<EventBean[]> processJoinResult(Set<MultiKey<EventBean>> newEvents, Set<MultiKey<EventBean>> oldEvents, boolean isSynthesize)
    {
        // Generate group-by keys for all events
        MultiKeyUntyped[] newDataGroupByKeys = generateGroupKeys(newEvents, true);
        MultiKeyUntyped[] oldDataGroupByKeys = generateGroupKeys(oldEvents, false);

        // generate old events
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".processJoinResults creating old output events");
        }

        if (isUnidirectional)
        {
            this.clear();
        }

        // update aggregates
        if (!newEvents.isEmpty())
        {
            // apply old data to aggregates
            int count = 0;
            for (MultiKey<EventBean> eventsPerStream : newEvents)
            {
                aggregationService.applyEnter(eventsPerStream.getArray(), newDataGroupByKeys[count], statementContext);
                count++;
            }
        }
        if (!oldEvents.isEmpty())
        {
            // apply old data to aggregates
            int count = 0;
            for (MultiKey<EventBean> eventsPerStream : oldEvents)
            {
                aggregationService.applyLeave(eventsPerStream.getArray(), oldDataGroupByKeys[count], statementContext);
                count++;
            }
        }

        EventBean[] selectOldEvents = null;
        if (isSelectRStream)
        {
            selectOldEvents = generateOutputEventsJoin(oldEvents, oldDataGroupByKeys, oldGenerators, false, isSynthesize);
        }
        EventBean[] selectNewEvents = generateOutputEventsJoin(newEvents, newDataGroupByKeys, newGenerators, true, isSynthesize);

        if ((selectNewEvents != null) || (selectOldEvents != null))
        {
            return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
        }
        return null;
    }

    public UniformPair<EventBean[]> processViewResult(EventBean[] newData, EventBean[] oldData, boolean isSynthesize)
    {
        // Generate group-by keys for all events
        MultiKeyUntyped[] newDataGroupByKeys = generateGroupKeys(newData, true);
        MultiKeyUntyped[] oldDataGroupByKeys = generateGroupKeys(oldData, false);

        // generate old events
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".processViewResults creating old output events");
        }

        // update aggregates
        EventBean[] eventsPerStream = new EventBean[1];
        if (newData != null)
        {
            // apply new data to aggregates
            for (int i = 0; i < newData.length; i++)
            {
                eventsPerStream[0] = newData[i];
                aggregationService.applyEnter(eventsPerStream, newDataGroupByKeys[i], statementContext);
            }
        }
        if (oldData != null)
        {
            // apply old data to aggregates
            for (int i = 0; i < oldData.length; i++)
            {
                eventsPerStream[0] = oldData[i];
                aggregationService.applyLeave(eventsPerStream, oldDataGroupByKeys[i], statementContext);
            }
        }

        EventBean[] selectOldEvents = null;
        if (isSelectRStream)
        {
            selectOldEvents = generateOutputEventsView(oldData, oldDataGroupByKeys, oldGenerators, false, isSynthesize);
        }
        EventBean[] selectNewEvents = generateOutputEventsView(newData, newDataGroupByKeys, newGenerators, true, isSynthesize);

        if ((selectNewEvents != null) || (selectOldEvents != null))
        {
            return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
        }
        return null;
    }

	private EventBean[] generateOutputEventsView(EventBean[] outputEvents, MultiKeyUntyped[] groupByKeys, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData, boolean isSynthesize)
    {
        if (outputEvents == null)
        {
            return null;
        }

        EventBean[] eventsPerStream = new EventBean[1];
        EventBean[] events = new EventBean[outputEvents.length];
        MultiKeyUntyped[] keys = new MultiKeyUntyped[outputEvents.length];
        EventBean[][] currentGenerators = null;
        if(isSorting)
        {
        	currentGenerators = new EventBean[outputEvents.length][];
        }

        int count = 0;
        for (int i = 0; i < outputEvents.length; i++)
        {
            aggregationService.setCurrentAccess(groupByKeys[count]);
            eventsPerStream[0] = outputEvents[count];

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            events[count] = selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            keys[count] = groupByKeys[count];
            if(isSorting)
            {
            	EventBean[] currentEventsPerStream = new EventBean[] { outputEvents[count] };
            	generators.put(keys[count], currentEventsPerStream);
            	currentGenerators[count] = currentEventsPerStream;
            }

            count++;
        }

        // Resize if some rows were filtered out
        if (count != events.length)
        {
            if (count == 0)
            {
                return null;
            }
            EventBean[] out = new EventBean[count];
            System.arraycopy(events, 0, out, 0, count);
            events = out;

            if(isSorting)
            {
            	MultiKeyUntyped[] outKeys = new MultiKeyUntyped[count];
            	System.arraycopy(keys, 0, outKeys, 0, count);
            	keys = outKeys;

            	EventBean[][] outGens = new EventBean[count][];
            	System.arraycopy(currentGenerators, 0, outGens, 0, count);
            	currentGenerators = outGens;
            }
        }

        if(isSorting)
        {
            events = orderByProcessor.sort(events, currentGenerators, keys, isNewData, statementContext);
        }

        return events;
    }

    private MultiKeyUntyped[] generateGroupKeys(Set<MultiKey<EventBean>> resultSet, boolean isNewData)
    {
        if (resultSet.isEmpty())
        {
            return null;
        }

        MultiKeyUntyped keys[] = new MultiKeyUntyped[resultSet.size()];

        int count = 0;
        for (MultiKey<EventBean> eventsPerStream : resultSet)
        {
            keys[count] = generateGroupKey(eventsPerStream.getArray(), isNewData);
            count++;
        }

        return keys;
    }

    private MultiKeyUntyped[] generateGroupKeys(EventBean[] events, boolean isNewData)
    {
        if (events == null)
        {
            return null;
        }

        EventBean[] eventsPerStream = new EventBean[1];
        MultiKeyUntyped keys[] = new MultiKeyUntyped[events.length];

        for (int i = 0; i < events.length; i++)
        {
            eventsPerStream[0] = events[i];
            keys[i] = generateGroupKey(eventsPerStream, isNewData);
        }

        return keys;
    }

    /**
     * Generates the group-by key for the row
     * @param eventsPerStream is the row of events
     * @param isNewData is true for new data
     * @return grouping keys
     */
    protected MultiKeyUntyped generateGroupKey(EventBean[] eventsPerStream, boolean isNewData)
    {
        Object[] keys = new Object[groupKeyNodes.length];

        int count = 0;
        for (ExprEvaluator exprNode : groupKeyNodes)
        {
            keys[count] = exprNode.evaluate(eventsPerStream, isNewData, statementContext);
            count++;
        }

        return new MultiKeyUntyped(keys);
    }

    private EventBean[] generateOutputEventsJoin(Set<MultiKey<EventBean>> resultSet, MultiKeyUntyped[] groupByKeys, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData, boolean isSynthesize)
    {
        if (resultSet.isEmpty())
        {
            return null;
        }

        EventBean[] events = new EventBean[resultSet.size()];
        MultiKeyUntyped[] keys = new MultiKeyUntyped[resultSet.size()];
        EventBean[][] currentGenerators = null;
        if(isSorting)
        {
        	currentGenerators = new EventBean[resultSet.size()][];
        }

        int count = 0;
        for (MultiKey<EventBean> row : resultSet)
        {
            EventBean[] eventsPerStream = row.getArray();

            if (isUnidirectional)
            {
                this.clear();
            }
            aggregationService.setCurrentAccess(groupByKeys[count]);

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            events[count] = selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize);
            keys[count] = groupByKeys[count];
            if(isSorting)
            {
            	generators.put(keys[count], eventsPerStream);
            	currentGenerators[count] = eventsPerStream;
            }

            count++;
        }

        // Resize if some rows were filtered out
        if (count != events.length)
        {
            if (count == 0)
            {
                return null;
            }
            EventBean[] out = new EventBean[count];
            System.arraycopy(events, 0, out, 0, count);
            events = out;

            if(isSorting)
            {
            	MultiKeyUntyped[] outKeys = new MultiKeyUntyped[count];
            	System.arraycopy(keys, 0, outKeys, 0, count);
            	keys = outKeys;

            	EventBean[][] outGens = new EventBean[count][];
            	System.arraycopy(currentGenerators, 0, outGens, 0, count);
            	currentGenerators = outGens;
            }
        }

        if(isSorting)
        {
            events = orderByProcessor.sort(events, currentGenerators, keys, isNewData, statementContext);
        }
        return events;
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        if (orderByProcessor == null)
        {
            return new ResultSetAggregateGroupedIterator(parent.iterator(), this, aggregationService, statementContext);
        }

        // Pull all parent events, generate order keys
        EventBean[] eventsPerStream = new EventBean[1];
        List<EventBean> outgoingEvents = new ArrayList<EventBean>();
        List<MultiKeyUntyped> orderKeys = new ArrayList<MultiKeyUntyped>();

        for (EventBean candidate : parent) {
            eventsPerStream[0] = candidate;

            MultiKeyUntyped groupKey = generateGroupKey(eventsPerStream, true);
            aggregationService.setCurrentAccess(groupKey);

            Boolean pass = true;
            if (optionalHavingNode != null) {
                pass = (Boolean) optionalHavingNode.evaluate(eventsPerStream, true, statementContext);
            }
            if ((pass == null) || (!pass))
            {
                continue;
            }

            outgoingEvents.add(selectExprProcessor.process(eventsPerStream, true, true));

            MultiKeyUntyped orderKey = orderByProcessor.getSortKey(eventsPerStream, true, statementContext);
            orderKeys.add(orderKey);
        }

        // sort
        EventBean[] outgoingEventsArr = outgoingEvents.toArray(new EventBean[outgoingEvents.size()]);
        MultiKeyUntyped[] orderKeysArr = orderKeys.toArray(new MultiKeyUntyped[orderKeys.size()]);
        EventBean[] orderedEvents = orderByProcessor.sort(outgoingEventsArr, orderKeysArr, statementContext);

        return new ArrayEventIterator(orderedEvents);
    }

    /**
     * Returns the select expression processor
     * @return select processor.
     */
    public SelectExprProcessor getSelectExprProcessor()
    {
        return selectExprProcessor;
    }

    /**
     * Returns the having node.
     * @return having expression
     */
    public ExprEvaluator getOptionalHavingNode()
    {
        return optionalHavingNode;
    }

    public Iterator<EventBean> getIterator(Set<MultiKey<EventBean>> joinSet)
    {
        // Generate group-by keys for all events
        MultiKeyUntyped[] groupByKeys = generateGroupKeys(joinSet, true);
        EventBean[] result = generateOutputEventsJoin(joinSet, groupByKeys, newGenerators, true, true);
        return new ArrayEventIterator(result);
    }

    public void clear()
    {
        aggregationService.clearResults();
    }

    public UniformPair<EventBean[]> processOutputLimitedJoin(List<UniformPair<Set<MultiKey<EventBean>>>> joinEventsSet, boolean generateSynthetic, OutputLimitLimitType outputLimitLimitType)
    {
        if (outputLimitLimitType == OutputLimitLimitType.DEFAULT)
        {
            List<EventBean> newEvents = new LinkedList<EventBean>();
            List<EventBean> oldEvents = null;
            if (isSelectRStream)
            {
                 oldEvents = new LinkedList<EventBean>();
            }

            List<MultiKeyUntyped> newEventsSortKey = null;
            List<MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedList<MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedList<MultiKeyUntyped>();
                }
            }

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                if (isUnidirectional)
                {
                    this.clear();
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (MultiKey<EventBean> aNewData : newData)
                    {
                        aggregationService.applyEnter(aNewData.getArray(), newDataMultiKey[count], statementContext);
                        count++;
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    int count = 0;
                    for (MultiKey<EventBean> anOldData : oldData)
                    {
                        aggregationService.applyLeave(anOldData.getArray(), oldDataMultiKey[count], statementContext);
                        count++;
                    }
                }

                if (isSelectRStream)
                {
                    generateOutputBatchedJoin(oldData, oldDataMultiKey, false, generateSynthetic, oldEvents, oldEventsSortKey);
                }
                generateOutputBatchedJoin(newData, newDataMultiKey, true, generateSynthetic, newEvents, newEventsSortKey);
            }

            EventBean[] newEventsArr = (newEvents.isEmpty()) ? null : newEvents.toArray(new EventBean[newEvents.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (oldEvents.isEmpty()) ? null : oldEvents.toArray(new EventBean[oldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
        else if (outputLimitLimitType == OutputLimitLimitType.ALL)
        {
            List<EventBean> newEvents = new LinkedList<EventBean>();
            List<EventBean> oldEvents = null;
            if (isSelectRStream)
            {
                oldEvents = new LinkedList<EventBean>();
            }

            List<MultiKeyUntyped> newEventsSortKey = null;
            List<MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedList<MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedList<MultiKeyUntyped>();
                }
            }

            workCollection.clear();

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                if (isUnidirectional)
                {
                    this.clear();
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (MultiKey<EventBean> aNewData : newData)
                    {
                        MultiKeyUntyped mk = newDataMultiKey[count];
                        aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                        count++;

                        // keep the new event as a representative for the group
                        workCollection.put(mk, aNewData.getArray());
                        eventGroupReps.put(mk, aNewData.getArray());
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    int count = 0;
                    for (MultiKey<EventBean> anOldData : oldData)
                    {
                        aggregationService.applyLeave(anOldData.getArray(), oldDataMultiKey[count], statementContext);
                        count++;
                    }
                }

                if (isSelectRStream)
                {
                    generateOutputBatchedJoin(oldData, oldDataMultiKey, false, generateSynthetic, oldEvents, oldEventsSortKey);
                }
                generateOutputBatchedJoin(newData, newDataMultiKey, true, generateSynthetic, newEvents, newEventsSortKey);
            }

            // For any group representatives not in the work collection, generate a row
            for (Map.Entry<MultiKeyUntyped, EventBean[]> entry : eventGroupReps.entrySet())
            {
                if (!workCollection.containsKey(entry.getKey()))
                {
                    workCollectionTwo.put(entry.getKey(), entry.getValue());
                    generateOutputBatchedArr(workCollectionTwo, true, generateSynthetic, newEvents, newEventsSortKey);
                    workCollectionTwo.clear();
                }
            }

            EventBean[] newEventsArr = (newEvents.isEmpty()) ? null : newEvents.toArray(new EventBean[newEvents.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (oldEvents.isEmpty()) ? null : oldEvents.toArray(new EventBean[oldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
        else if (outputLimitLimitType == OutputLimitLimitType.FIRST) {
            List<EventBean> resultNewEvents = new ArrayList<EventBean>();
            List<MultiKeyUntyped> resultNewSortKeys = null;
            List<EventBean> resultOldEvents = null;
            List<MultiKeyUntyped> resultOldSortKeys = null;

            if (orderByProcessor != null) {
                resultNewSortKeys = new ArrayList<MultiKeyUntyped>();
            }
            if (isSelectRStream) {
                resultOldEvents = new ArrayList<EventBean>();
                resultOldSortKeys = new ArrayList<MultiKeyUntyped>();
            }

            workCollection.clear();

            if (this.optionalHavingNode == null) {
                for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
                {
                    Set<MultiKey<EventBean>> newData = pair.getFirst();
                    Set<MultiKey<EventBean>> oldData = pair.getSecond();

                    MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                    MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                    if (newData != null)
                    {
                        // apply new data to aggregates
                        int count = 0;
                        for (MultiKey<EventBean> aNewData : newData)
                        {
                            MultiKeyUntyped mk = newDataMultiKey[count];
                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(newDataMultiKey[count], outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(1, 0);
                            if (pass) {
                                workCollection.put(mk, aNewData.getArray());
                            }
                            aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                            count++;
                        }
                    }

                    if (oldData != null)
                    {
                        // apply new data to aggregates
                        int count = 0;
                        for (MultiKey<EventBean> aOldData : oldData)
                        {
                            MultiKeyUntyped mk = oldDataMultiKey[count];
                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(oldDataMultiKey[count], outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(0, 1);
                            if (pass) {
                                workCollection.put(mk, aOldData.getArray());
                            }
                            aggregationService.applyLeave(aOldData.getArray(), mk, statementContext);
                            count++;
                        }
                    }

                    if (false)  // there is no remove stream currently for output first
                    {
                        generateOutputBatchedArr(workCollection, false, generateSynthetic, resultOldEvents, resultOldSortKeys);
                    }
                    generateOutputBatchedArr(workCollection, false, generateSynthetic, resultNewEvents, resultNewSortKeys);
                }
            }
            else {// there is a having-clause, apply after aggregations
                for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
                {
                    Set<MultiKey<EventBean>> newData = pair.getFirst();
                    Set<MultiKey<EventBean>> oldData = pair.getSecond();

                    MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                    MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                    if (newData != null)
                    {
                        // apply new data to aggregates
                        int count = 0;
                        for (MultiKey<EventBean> aNewData : newData)
                        {
                            MultiKeyUntyped mk = newDataMultiKey[count];
                            aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                            count++;
                        }
                    }

                    if (oldData != null)
                    {
                        int count = 0;
                        for (MultiKey<EventBean> aOldData : oldData)
                        {
                            MultiKeyUntyped mk = oldDataMultiKey[count];
                            aggregationService.applyLeave(aOldData.getArray(), mk, statementContext);
                            count++;
                        }
                    }

                    if (newData != null)
                    {
                        // check having clause and first-condition
                        int count = 0;
                        for (MultiKey<EventBean> aNewData : newData)
                        {
                            MultiKeyUntyped mk = newDataMultiKey[count];
                            aggregationService.setCurrentAccess(mk);

                            // Filter the having clause
                            Boolean result = (Boolean) optionalHavingNode.evaluate(aNewData.getArray(), true, statementContext);
                            if ((result == null) || (!result))
                            {
                                count++;
                                continue;
                            }

                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(mk, outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(1, 0);
                            if (pass) {
                                workCollection.put(mk, aNewData.getArray());
                            }
                            count++;
                        }
                    }

                    if (oldData != null)
                    {
                        // apply new data to aggregates
                        int count = 0;
                        for (MultiKey<EventBean> aOldData : oldData)
                        {
                            MultiKeyUntyped mk = oldDataMultiKey[count];
                            aggregationService.setCurrentAccess(mk);

                            // Filter the having clause
                            Boolean result = (Boolean) optionalHavingNode.evaluate(aOldData.getArray(), true, statementContext);
                            if ((result == null) || (!result))
                            {
                                count++;
                                continue;
                            }

                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(mk, outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(0, 1);
                            if (pass) {
                                workCollection.put(mk, aOldData.getArray());
                            }
                        }
                    }

                    if (false)  // there is no remove stream currently for output first
                    {
                        generateOutputBatchedArr(workCollection, false, generateSynthetic, resultOldEvents, resultOldSortKeys);
                    }
                    generateOutputBatchedArr(workCollection, false, generateSynthetic, resultNewEvents, resultNewSortKeys);
                }
            }

            EventBean[] newEventsArr = null;
            EventBean[] oldEventsArr = null;
            if (!resultNewEvents.isEmpty()) {
                newEventsArr = resultNewEvents.toArray(new EventBean[resultNewEvents.size()]);
            }
            if ((resultOldEvents != null) && (!resultOldEvents.isEmpty())) {
                oldEventsArr = resultOldEvents.toArray(new EventBean[resultOldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (resultNewSortKeys.isEmpty()) ? null : resultNewSortKeys.toArray(new MultiKeyUntyped[resultNewSortKeys.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (resultOldSortKeys.isEmpty()) ? null : resultOldSortKeys.toArray(new MultiKeyUntyped[resultOldSortKeys.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
        else // (outputLimitLimitType == OutputLimitLimitType.LAST) Compute last per group
        {
            Map<MultiKeyUntyped, EventBean> lastPerGroupNew = new LinkedHashMap<MultiKeyUntyped, EventBean>();
            Map<MultiKeyUntyped, EventBean> lastPerGroupOld = null;
            if (isSelectRStream)
            {
                lastPerGroupOld = new LinkedHashMap<MultiKeyUntyped, EventBean>();
            }

            Map<MultiKeyUntyped, MultiKeyUntyped> newEventsSortKey = null; // group key to sort key
            Map<MultiKeyUntyped, MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedHashMap<MultiKeyUntyped, MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedHashMap<MultiKeyUntyped, MultiKeyUntyped>();
                }
            }

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                if (isUnidirectional)
                {
                    this.clear();
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (MultiKey<EventBean> aNewData : newData)
                    {
                        MultiKeyUntyped mk = newDataMultiKey[count];
                        aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                        count++;
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    int count = 0;
                    for (MultiKey<EventBean> anOldData : oldData)
                    {
                        workCollection.put(oldDataMultiKey[count], anOldData.getArray());
                        aggregationService.applyLeave(anOldData.getArray(), oldDataMultiKey[count], statementContext);
                        count++;
                    }
                }

                if (isSelectRStream)
                {
                    generateOutputBatchedJoin(oldData, oldDataMultiKey, false, generateSynthetic, lastPerGroupOld, oldEventsSortKey);
                }
                generateOutputBatchedJoin(newData, newDataMultiKey, false, generateSynthetic, lastPerGroupNew, newEventsSortKey);
            }

            EventBean[] newEventsArr = (lastPerGroupNew.isEmpty()) ? null : lastPerGroupNew.values().toArray(new EventBean[lastPerGroupNew.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (lastPerGroupOld.isEmpty()) ? null : lastPerGroupOld.values().toArray(new EventBean[lastPerGroupOld.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.values().toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.values().toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
    }

    public UniformPair<EventBean[]> processOutputLimitedView(List<UniformPair<EventBean[]>> viewEventsList, boolean generateSynthetic, OutputLimitLimitType outputLimitLimitType)
    {
        if (outputLimitLimitType == OutputLimitLimitType.DEFAULT)
        {
            List<EventBean> newEvents = new LinkedList<EventBean>();
            List<EventBean> oldEvents = null;
            if (isSelectRStream)
            {
                oldEvents = new LinkedList<EventBean>();
            }

            List<MultiKeyUntyped> newEventsSortKey = null;
            List<MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedList<MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedList<MultiKeyUntyped>();
                }
            }

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (EventBean aNewData : newData)
                    {
                        eventsPerStreamOneStream[0] = aNewData;
                        aggregationService.applyEnter(eventsPerStreamOneStream, newDataMultiKey[count], statementContext);
                        count++;
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    int count = 0;
                    for (EventBean anOldData : oldData)
                    {
                        eventsPerStreamOneStream[0] = anOldData;
                        aggregationService.applyLeave(eventsPerStreamOneStream, oldDataMultiKey[count], statementContext);
                        count++;
                    }
                }

                if (isSelectRStream)
                {
                    generateOutputBatchedView(oldData, oldDataMultiKey, false, generateSynthetic, oldEvents, oldEventsSortKey);
                }
                generateOutputBatchedView(newData, newDataMultiKey, true, generateSynthetic, newEvents, newEventsSortKey);
            }

            EventBean[] newEventsArr = (newEvents.isEmpty()) ? null : newEvents.toArray(new EventBean[newEvents.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (oldEvents.isEmpty()) ? null : oldEvents.toArray(new EventBean[oldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
        else if (outputLimitLimitType == OutputLimitLimitType.ALL)
        {
            List<EventBean> newEvents = new LinkedList<EventBean>();
            List<EventBean> oldEvents = null;
            if (isSelectRStream)
            {
                oldEvents = new LinkedList<EventBean>();
            }

            List<MultiKeyUntyped> newEventsSortKey = null;
            List<MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedList<MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedList<MultiKeyUntyped>();
                }
            }

            workCollection.clear();

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                EventBean[] eventsPerStream = new EventBean[1];
                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (EventBean aNewData : newData)
                    {
                        MultiKeyUntyped mk = newDataMultiKey[count];
                        eventsPerStream[0] = aNewData;
                        aggregationService.applyEnter(eventsPerStream, mk, statementContext);
                        count++;

                        // keep the new event as a representative for the group
                        workCollection.put(mk, eventsPerStream);
                        eventGroupReps.put(mk, new EventBean[] {aNewData});
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    int count = 0;
                    for (EventBean anOldData : oldData)
                    {
                        eventsPerStream[0] = anOldData;
                        aggregationService.applyLeave(eventsPerStream, oldDataMultiKey[count], statementContext);
                        count++;
                    }
                }

                if (isSelectRStream)
                {
                    generateOutputBatchedView(oldData, oldDataMultiKey, false, generateSynthetic, oldEvents, oldEventsSortKey);
                }
                generateOutputBatchedView(newData, newDataMultiKey, true, generateSynthetic, newEvents, newEventsSortKey);
            }

            // For any group representatives not in the work collection, generate a row
            for (Map.Entry<MultiKeyUntyped, EventBean[]> entry : eventGroupReps.entrySet())
            {
                if (!workCollection.containsKey(entry.getKey()))
                {
                    workCollectionTwo.put(entry.getKey(), entry.getValue());
                    generateOutputBatchedArr(workCollectionTwo, true, generateSynthetic, newEvents, newEventsSortKey);
                    workCollectionTwo.clear();
                }
            }

            EventBean[] newEventsArr = (newEvents.isEmpty()) ? null : newEvents.toArray(new EventBean[newEvents.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (oldEvents.isEmpty()) ? null : oldEvents.toArray(new EventBean[oldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
        else if (outputLimitLimitType == OutputLimitLimitType.FIRST)
        {
            List<EventBean> resultNewEvents = new ArrayList<EventBean>();
            List<MultiKeyUntyped> resultNewSortKeys = null;
            List<EventBean> resultOldEvents = null;
            List<MultiKeyUntyped> resultOldSortKeys = null;

            if (orderByProcessor != null) {
                resultNewSortKeys = new ArrayList<MultiKeyUntyped>();
            }
            if (isSelectRStream) {
                resultOldEvents = new ArrayList<EventBean>();
                resultOldSortKeys = new ArrayList<MultiKeyUntyped>();
            }

            workCollection.clear();
            if (this.optionalHavingNode == null) {
                for (UniformPair<EventBean[]> pair : viewEventsList)
                {
                    EventBean[] newData = pair.getFirst();
                    EventBean[] oldData = pair.getSecond();

                    MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                    MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                    if (newData != null)
                    {
                        // apply new data to aggregates
                        for (int i = 0; i < newData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = newData[i];
                            MultiKeyUntyped mk = newDataMultiKey[i];
                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(newDataMultiKey[i], outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(1, 0);
                            if (pass) {
                                workCollection.put(mk, new EventBean[]{newData[i]});
                            }
                            aggregationService.applyEnter(eventsPerStreamOneStream, mk, statementContext);
                        }
                    }

                    if (oldData != null)
                    {
                        // apply new data to aggregates
                        for (int i = 0; i < oldData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = oldData[i];
                            MultiKeyUntyped mk = oldDataMultiKey[i];
                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(oldDataMultiKey[i], outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(0, 1);
                            if (pass) {
                                workCollection.put(mk, new EventBean[]{oldData[i]});
                            }
                            aggregationService.applyLeave(eventsPerStreamOneStream, mk, statementContext);
                        }
                    }

                    if (false)  // there is no remove stream currently for output first
                    {
                        generateOutputBatchedArr(workCollection, false, generateSynthetic, resultOldEvents, resultOldSortKeys);
                    }
                    generateOutputBatchedArr(workCollection, false, generateSynthetic, resultNewEvents, resultNewSortKeys);
                }
            }
            else {  // has a having-clause
                for (UniformPair<EventBean[]> pair : viewEventsList)
                {
                    EventBean[] newData = pair.getFirst();
                    EventBean[] oldData = pair.getSecond();

                    MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                    MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                    if (newData != null)
                    {
                        // apply new data to aggregates
                        for (int i = 0; i < newData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = newData[i];
                            MultiKeyUntyped mk = newDataMultiKey[i];
                            aggregationService.applyEnter(eventsPerStreamOneStream, mk, statementContext);
                        }
                    }

                    if (oldData != null)
                    {
                        for (int i = 0; i < oldData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = oldData[i];
                            MultiKeyUntyped mk = oldDataMultiKey[i];
                            aggregationService.applyLeave(eventsPerStreamOneStream, mk, statementContext);
                        }
                    }

                    if (newData != null)
                    {
                        // check having clause and first-condition
                        for (int i = 0; i < newData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = newData[i];
                            MultiKeyUntyped mk = newDataMultiKey[i];
                            aggregationService.setCurrentAccess(mk);

                            // Filter the having clause
                            Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStreamOneStream, true, statementContext);
                            if ((result == null) || (!result))
                            {
                                continue;
                            }

                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(mk, outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(1, 0);
                            if (pass) {
                                workCollection.put(mk, new EventBean[]{newData[i]});
                            }
                        }
                    }

                    if (oldData != null)
                    {
                        // apply new data to aggregates
                        for (int i = 0; i < oldData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = oldData[i];
                            MultiKeyUntyped mk = oldDataMultiKey[i];
                            aggregationService.setCurrentAccess(mk);

                            // Filter the having clause
                            Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStreamOneStream, true, statementContext);
                            if ((result == null) || (!result))
                            {
                                continue;
                            }

                            OutputConditionPolled outputStateGroup = outputState.get(mk);
                            if (outputStateGroup == null) {
                                try {
                                    outputStateGroup = OutputConditionPolledFactory.createCondition(outputLimitSpec, statementContext);
                                }
                                catch (ExprValidationException e) {
                                    log.error("Error starting output limit for group for statement '" + statementContext.getStatementName() + "'");
                                }
                                outputState.put(oldDataMultiKey[i], outputStateGroup);
                            }
                            boolean pass = outputStateGroup.updateOutputCondition(0, 1);
                            if (pass) {
                                workCollection.put(mk, new EventBean[]{oldData[i]});
                            }
                        }
                    }

                    if (false)  // there is no remove stream currently for output first
                    {
                        generateOutputBatchedArr(workCollection, false, generateSynthetic, resultOldEvents, resultOldSortKeys);
                    }
                    generateOutputBatchedArr(workCollection, false, generateSynthetic, resultNewEvents, resultNewSortKeys);
                }
            }

            EventBean[] newEventsArr = null;
            EventBean[] oldEventsArr = null;
            if (!resultNewEvents.isEmpty()) {
                newEventsArr = resultNewEvents.toArray(new EventBean[resultNewEvents.size()]);
            }
            if ((resultOldEvents != null) && (!resultOldEvents.isEmpty())) {
                oldEventsArr = resultOldEvents.toArray(new EventBean[resultOldEvents.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (resultNewSortKeys.isEmpty()) ? null : resultNewSortKeys.toArray(new MultiKeyUntyped[resultNewSortKeys.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (resultOldSortKeys.isEmpty()) ? null : resultOldSortKeys.toArray(new MultiKeyUntyped[resultOldSortKeys.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
        else // (outputLimitLimitType == OutputLimitLimitType.LAST) Compute last per group
        {
            Map<MultiKeyUntyped, EventBean> lastPerGroupNew = new LinkedHashMap<MultiKeyUntyped, EventBean>();
            Map<MultiKeyUntyped, EventBean> lastPerGroupOld = null;
            if (isSelectRStream)
            {
                lastPerGroupOld = new LinkedHashMap<MultiKeyUntyped, EventBean>();
            }

            Map<MultiKeyUntyped, MultiKeyUntyped> newEventsSortKey = null; // group key to sort key
            Map<MultiKeyUntyped, MultiKeyUntyped> oldEventsSortKey = null;
            if (orderByProcessor != null)
            {
                newEventsSortKey = new LinkedHashMap<MultiKeyUntyped, MultiKeyUntyped>();
                if (isSelectRStream)
                {
                    oldEventsSortKey = new LinkedHashMap<MultiKeyUntyped, MultiKeyUntyped>();
                }
            }

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, false);

                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (EventBean aNewData : newData)
                    {
                        MultiKeyUntyped mk = newDataMultiKey[count];
                        eventsPerStreamOneStream[0] = aNewData;
                        aggregationService.applyEnter(eventsPerStreamOneStream, mk, statementContext);
                        count++;
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    int count = 0;
                    for (EventBean anOldData : oldData)
                    {
                        eventsPerStreamOneStream[0] = anOldData;
                        aggregationService.applyLeave(eventsPerStreamOneStream, oldDataMultiKey[count], statementContext);
                        count++;
                    }
                }

                if (isSelectRStream)
                {
                    generateOutputBatchedView(oldData, oldDataMultiKey, false, generateSynthetic, lastPerGroupOld, oldEventsSortKey);
                }
                generateOutputBatchedView(newData, newDataMultiKey, false, generateSynthetic, lastPerGroupNew, newEventsSortKey);
            }

            EventBean[] newEventsArr = (lastPerGroupNew.isEmpty()) ? null : lastPerGroupNew.values().toArray(new EventBean[lastPerGroupNew.size()]);
            EventBean[] oldEventsArr = null;
            if (isSelectRStream)
            {
                oldEventsArr = (lastPerGroupOld.isEmpty()) ? null : lastPerGroupOld.values().toArray(new EventBean[lastPerGroupOld.size()]);
            }

            if (orderByProcessor != null)
            {
                MultiKeyUntyped[] sortKeysNew = (newEventsSortKey.isEmpty()) ? null : newEventsSortKey.values().toArray(new MultiKeyUntyped[newEventsSortKey.size()]);
                newEventsArr = orderByProcessor.sort(newEventsArr, sortKeysNew, statementContext);
                if (isSelectRStream)
                {
                    MultiKeyUntyped[] sortKeysOld = (oldEventsSortKey.isEmpty()) ? null : oldEventsSortKey.values().toArray(new MultiKeyUntyped[oldEventsSortKey.size()]);
                    oldEventsArr = orderByProcessor.sort(oldEventsArr, sortKeysOld, statementContext);
                }
            }

            if ((newEventsArr == null) && (oldEventsArr == null))
            {
                return null;
            }
            return new UniformPair<EventBean[]>(newEventsArr, oldEventsArr);
        }
    }

    private void generateOutputBatchedArr(Map<MultiKeyUntyped, EventBean[]> keysAndEvents, boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents, List<MultiKeyUntyped> optSortKeys)
    {
        for (Map.Entry<MultiKeyUntyped, EventBean[]> entry : keysAndEvents.entrySet())
        {
            EventBean[] eventsPerStream = entry.getValue();

            // Set the current row of aggregation states
            aggregationService.setCurrentAccess(entry.getKey());

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            resultEvents.add(selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize));

            if(isSorting)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, statementContext));
            }
        }
    }

    private void generateOutputBatchedView(EventBean[] outputEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents, List<MultiKeyUntyped> optSortKeys)
    {
        if (outputEvents == null)
        {
            return;
        }

        EventBean[] eventsPerStream = new EventBean[1];

        int count = 0;
        for (int i = 0; i < outputEvents.length; i++)
        {
            aggregationService.setCurrentAccess(groupByKeys[count]);
            eventsPerStream[0] = outputEvents[count];

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            resultEvents.add(selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if(isSorting)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, statementContext));
            }

            count++;
        }
    }

    private void generateOutputBatchedJoin(Set<MultiKey<EventBean>> outputEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents, List<MultiKeyUntyped> optSortKeys)
    {
        if (outputEvents == null)
        {
            return;
        }

        EventBean[] eventsPerStream;

        int count = 0;
        for (MultiKey<EventBean> row : outputEvents)
        {
            aggregationService.setCurrentAccess(groupByKeys[count]);
            eventsPerStream = row.getArray();

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            resultEvents.add(selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if(isSorting)
            {
                optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, statementContext));
            }

            count++;
        }
    }

    private void generateOutputBatchedView(EventBean[] outputEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, boolean isSynthesize, Map<MultiKeyUntyped, EventBean> resultEvents, Map<MultiKeyUntyped, MultiKeyUntyped> optSortKeys)
    {
        if (outputEvents == null)
        {
            return;
        }

        EventBean[] eventsPerStream = new EventBean[1];

        int count = 0;
        for (int i = 0; i < outputEvents.length; i++)
        {
            MultiKeyUntyped groupKey = groupByKeys[count];
            aggregationService.setCurrentAccess(groupKey);
            eventsPerStream[0] = outputEvents[count];

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            resultEvents.put(groupKey, selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize));
            if(isSorting)
            {
                optSortKeys.put(groupKey, orderByProcessor.getSortKey(eventsPerStream, isNewData, statementContext));
            }

            count++;
        }
    }

    private void generateOutputBatchedJoin(Set<MultiKey<EventBean>> outputEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, boolean isSynthesize, Map<MultiKeyUntyped, EventBean> resultEvents, Map<MultiKeyUntyped, MultiKeyUntyped> optSortKeys)
    {
        if (outputEvents == null)
        {
            return;
        }

        int count = 0;
        for (MultiKey<EventBean> row : outputEvents)
        {
            MultiKeyUntyped groupKey = groupByKeys[count];
            aggregationService.setCurrentAccess(groupKey);

            // Filter the having clause
            if (optionalHavingNode != null)
            {
                Boolean result = (Boolean) optionalHavingNode.evaluate(row.getArray(), isNewData, statementContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            resultEvents.put(groupKey, selectExprProcessor.process(row.getArray(), isNewData, isSynthesize));
            if(isSorting)
            {
                optSortKeys.put(groupKey, orderByProcessor.getSortKey(row.getArray(), isNewData, statementContext));
            }

            count++;
        }
    }
}

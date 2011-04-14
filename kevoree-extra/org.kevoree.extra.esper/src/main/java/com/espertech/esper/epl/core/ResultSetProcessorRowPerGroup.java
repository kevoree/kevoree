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
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Result set processor for the fully-grouped case:
 * there is a group-by and all non-aggregation event properties in the select clause are listed in the group by,
 * and there are aggregation functions.
 * <p>
 * Produces one row for each group that changed (and not one row per event). Computes MultiKey group-by keys for
 * each event and uses a set of the group-by keys to generate the result rows, using the first (old or new, anyone) event
 * for each distinct group-by key.
 */
public class ResultSetProcessorRowPerGroup implements ResultSetProcessor
{
    private static final Log log = LogFactory.getLog(ResultSetProcessorRowPerGroup.class);

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

    // For output rate limiting, keep a representative event for each group for
    // representing each group in an output limit clause
    private final Map<MultiKeyUntyped, EventBean[]> groupRepsView = new LinkedHashMap<MultiKeyUntyped, EventBean[]>();

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
    public ResultSetProcessorRowPerGroup(SelectExprProcessor selectExprProcessor,
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
        // Generate group-by keys for all events, collect all keys in a set for later event generation
        Map<MultiKeyUntyped, EventBean[]> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean[]>();
        MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newEvents, keysAndEvents, true);
        MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldEvents, keysAndEvents, false);

        if (isUnidirectional)
        {
            this.clear();
        }

        // generate old events
        EventBean[] selectOldEvents = null;
        if (isSelectRStream)
        {
            selectOldEvents = generateOutputEventsJoin(keysAndEvents, oldGenerators, false, isSynthesize);
        }

        // update aggregates
        if (!newEvents.isEmpty())
        {
            // apply old data to aggregates
            int count = 0;
            for (MultiKey<EventBean> eventsPerStream : newEvents)
            {
                aggregationService.applyEnter(eventsPerStream.getArray(), newDataMultiKey[count], statementContext);
                count++;
            }
        }
        if (oldEvents != null && !oldEvents.isEmpty())
        {
            // apply old data to aggregates
            int count = 0;
            for (MultiKey<EventBean> eventsPerStream : oldEvents)
            {
                aggregationService.applyLeave(eventsPerStream.getArray(), oldDataMultiKey[count], statementContext);
                count++;
            }
        }

        // generate new events using select expressions
        EventBean[] selectNewEvents = generateOutputEventsJoin(keysAndEvents, newGenerators, true, isSynthesize);

        if ((selectNewEvents != null) || (selectOldEvents != null))
        {
            return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
        }
        return null;
    }

    public UniformPair<EventBean[]> processViewResult(EventBean[] newData, EventBean[] oldData, boolean isSynthesize)
    {
        // Generate group-by keys for all events, collect all keys in a set for later event generation
        Map<MultiKeyUntyped, EventBean> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean>();
        MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, keysAndEvents, true);
        MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, keysAndEvents, false);

        EventBean[] selectOldEvents = null;
        if (isSelectRStream)
        {
            selectOldEvents = generateOutputEventsView(keysAndEvents, oldGenerators, false, isSynthesize);
        }

        // update aggregates
        EventBean[] eventsPerStream = new EventBean[1];
        if (newData != null)
        {
            // apply new data to aggregates
            for (int i = 0; i < newData.length; i++)
            {
                eventsPerStream[0] = newData[i];
                aggregationService.applyEnter(eventsPerStream, newDataMultiKey[i], statementContext);
            }
        }
        if (oldData != null)
        {
            // apply old data to aggregates
            for (int i = 0; i < oldData.length; i++)
            {
                eventsPerStream[0] = oldData[i];
                aggregationService.applyLeave(eventsPerStream, oldDataMultiKey[i], statementContext);
            }
        }

        // generate new events using select expressions
        EventBean[] selectNewEvents = generateOutputEventsView(keysAndEvents, newGenerators, true, isSynthesize);

        if ((selectNewEvents != null) || (selectOldEvents != null))
        {
            return new UniformPair<EventBean[]>(selectNewEvents, selectOldEvents);
        }
        return null;
    }

    private EventBean[] generateOutputEventsView(Map<MultiKeyUntyped, EventBean> keysAndEvents, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData, boolean isSynthesize)
    {
        EventBean[] eventsPerStream = new EventBean[1];
        EventBean[] events = new EventBean[keysAndEvents.size()];
        MultiKeyUntyped[] keys = new MultiKeyUntyped[keysAndEvents.size()];
        EventBean[][] currentGenerators = null;
        if(isSorting)
        {
            currentGenerators = new EventBean[keysAndEvents.size()][];
        }

        int count = 0;
        for (Map.Entry<MultiKeyUntyped, EventBean> entry : keysAndEvents.entrySet())
        {
            // Set the current row of aggregation states
            aggregationService.setCurrentAccess(entry.getKey());

            eventsPerStream[0] = entry.getValue();

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
            keys[count] = entry.getKey();
            if(isSorting)
            {
                EventBean[] currentEventsPerStream = new EventBean[] { entry.getValue() };
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
            events =  orderByProcessor.sort(events, currentGenerators, keys, isNewData, statementContext);
        }

        return events;
    }

    private void generateOutputBatched(Map<MultiKeyUntyped, EventBean> keysAndEvents, boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents, List<MultiKeyUntyped> optSortKeys, StatementContext statementContext)
    {
        EventBean[] eventsPerStream = new EventBean[1];

        for (Map.Entry<MultiKeyUntyped, EventBean> entry : keysAndEvents.entrySet())
        {
            // Set the current row of aggregation states
            aggregationService.setCurrentAccess(entry.getKey());

            eventsPerStream[0] = entry.getValue();

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

    private void generateOutputBatchedArr(Map<MultiKeyUntyped, EventBean[]> keysAndEvents, boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents, List<MultiKeyUntyped> optSortKeys)
    {
        for (Map.Entry<MultiKeyUntyped, EventBean[]> entry : keysAndEvents.entrySet())
        {
            generateOutputBatched(entry.getKey(), entry.getValue(), isNewData, isSynthesize, resultEvents, optSortKeys);
        }
    }

    private void generateOutputBatched(MultiKeyUntyped mk, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize, List<EventBean> resultEvents, List<MultiKeyUntyped> optSortKeys)
    {
        // Set the current row of aggregation states
        aggregationService.setCurrentAccess(mk);

        // Filter the having clause
        if (optionalHavingNode != null)
        {
            Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStream, isNewData, statementContext);
            if ((result == null) || (!result))
            {
                return;
            }
        }

        resultEvents.add(selectExprProcessor.process(eventsPerStream, isNewData, isSynthesize));

        if(isSorting)
        {
            optSortKeys.add(orderByProcessor.getSortKey(eventsPerStream, isNewData, statementContext));
        }
    }

    private EventBean[] generateOutputEventsJoin(Map<MultiKeyUntyped, EventBean[]> keysAndEvents, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData, boolean isSynthesize)
    {
        EventBean[] events = new EventBean[keysAndEvents.size()];
        MultiKeyUntyped[] keys = new MultiKeyUntyped[keysAndEvents.size()];
        EventBean[][] currentGenerators = null;
        if(isSorting)
        {
            currentGenerators = new EventBean[keysAndEvents.size()][];
        }

        int count = 0;
        for (Map.Entry<MultiKeyUntyped, EventBean[]> entry : keysAndEvents.entrySet())
        {
            aggregationService.setCurrentAccess(entry.getKey());
            EventBean[] eventsPerStream = entry.getValue();

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
            keys[count] = entry.getKey();
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
            events =  orderByProcessor.sort(events, currentGenerators, keys, isNewData, statementContext);
        }

        return events;
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

    private MultiKeyUntyped[] generateGroupKeys(EventBean[] events, Map<MultiKeyUntyped, EventBean> eventPerKey, boolean isNewData)
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
            eventPerKey.put(keys[i], events[i]);
        }

        return keys;
    }

    private MultiKeyUntyped[] generateGroupKeys(Set<MultiKey<EventBean>> resultSet, Map<MultiKeyUntyped, EventBean[]> eventPerKey, boolean isNewData)
    {
        if (resultSet == null || resultSet.isEmpty())
        {
            return null;
        }

        MultiKeyUntyped keys[] = new MultiKeyUntyped[resultSet.size()];

        int count = 0;
        for (MultiKey<EventBean> eventsPerStream : resultSet)
        {
            keys[count] = generateGroupKey(eventsPerStream.getArray(), isNewData);
            eventPerKey.put(keys[count], eventsPerStream.getArray());

            count++;
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

        for (int i = 0; i < groupKeyNodes.length; i++)
        {
            keys[i] = groupKeyNodes[i].evaluate(eventsPerStream, isNewData, statementContext);
        }

        return new MultiKeyUntyped(keys);
    }

    /**
     * Returns the optional having expression.
     * @return having expression node
     */
    public ExprEvaluator getOptionalHavingNode()
    {
        return optionalHavingNode;
    }

    /**
     * Returns the select expression processor
     * @return select processor.
     */
    public SelectExprProcessor getSelectExprProcessor()
    {
        return selectExprProcessor;
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        if (orderByProcessor == null)
        {
            return new ResultSetRowPerGroupIterator(parent.iterator(), this, aggregationService, statementContext);
        }

        // Pull all parent events, generate order keys
        EventBean[] eventsPerStream = new EventBean[1];
        List<EventBean> outgoingEvents = new ArrayList<EventBean>();
        List<MultiKeyUntyped> orderKeys = new ArrayList<MultiKeyUntyped>();
        Set<MultiKeyUntyped> priorSeenGroups = new HashSet<MultiKeyUntyped>();

        for (EventBean candidate : parent)
        {
            eventsPerStream[0] = candidate;

            MultiKeyUntyped groupKey = generateGroupKey(eventsPerStream, true);
            aggregationService.setCurrentAccess(groupKey);

            Boolean pass = true;
            if (optionalHavingNode != null)
            {
                pass = (Boolean) optionalHavingNode.evaluate(eventsPerStream, true, statementContext);
            }
            if ((pass == null) || (!pass))
            {
                continue;
            }
            if (priorSeenGroups.contains(groupKey))
            {
                continue;
            }
            priorSeenGroups.add(groupKey);

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

    public Iterator<EventBean> getIterator(Set<MultiKey<EventBean>> joinSet)
    {
        Map<MultiKeyUntyped, EventBean[]> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean[]>();
        generateGroupKeys(joinSet, keysAndEvents, true);
        EventBean[] selectNewEvents = generateOutputEventsJoin(keysAndEvents, newGenerators, true, true);
        return new ArrayEventIterator(selectNewEvents);
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

            Map<MultiKeyUntyped, EventBean[]> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean[]>();

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if (isUnidirectional)
                {
                    this.clear();
                }

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, keysAndEvents, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, keysAndEvents, false);

                if (isSelectRStream)
                {
                    generateOutputBatchedArr(keysAndEvents, false, generateSynthetic, oldEvents, oldEventsSortKey);
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

                generateOutputBatchedArr(keysAndEvents, true, generateSynthetic, newEvents, newEventsSortKey);

                keysAndEvents.clear();
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

            if (isSelectRStream)
            {
                generateOutputBatchedArr(groupRepsView, false, generateSynthetic, oldEvents, oldEventsSortKey);
            }

            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if (isUnidirectional)
                {
                    this.clear();
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (MultiKey<EventBean> aNewData : newData)
                    {
                        MultiKeyUntyped mk = generateGroupKey(aNewData.getArray(), true);

                        // if this is a newly encountered group, generate the remove stream event
                        if (groupRepsView.put(mk, aNewData.getArray()) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, aNewData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }
                        aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (MultiKey<EventBean> anOldData : oldData)
                    {
                        MultiKeyUntyped mk = generateGroupKey(anOldData.getArray(), true);

                        if (groupRepsView.put(mk, anOldData.getArray()) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, anOldData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }

                        aggregationService.applyLeave(anOldData.getArray(), mk, statementContext);
                    }
                }
            }

            generateOutputBatchedArr(groupRepsView, true, generateSynthetic, newEvents, newEventsSortKey);

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

            groupRepsView.clear();
            if (this.optionalHavingNode == null) {
                for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
                {
                    Set<MultiKey<EventBean>> newData = pair.getFirst();
                    Set<MultiKey<EventBean>> oldData = pair.getSecond();

                    if (newData != null)
                    {
                        // apply new data to aggregates
                        for (MultiKey<EventBean> aNewData : newData)
                        {
                            MultiKeyUntyped mk = generateGroupKey(aNewData.getArray(), true);

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
                                // if this is a newly encountered group, generate the remove stream event
                                if (groupRepsView.put(mk, aNewData.getArray()) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, aNewData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }
                            aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                        }
                    }
                    if (oldData != null)
                    {
                        // apply old data to aggregates
                        for (MultiKey<EventBean> anOldData : oldData)
                        {
                            MultiKeyUntyped mk = generateGroupKey(anOldData.getArray(), true);

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
                                if (groupRepsView.put(mk, anOldData.getArray()) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, anOldData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }

                            aggregationService.applyLeave(anOldData.getArray(), mk, statementContext);
                        }
                    }
                }
            }
            else {
                groupRepsView.clear();
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

                    // evaluate having-clause
                    if (newData != null)
                    {
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
                                if (groupRepsView.put(mk, aNewData.getArray()) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, aNewData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }
                            count++;
                        }
                    }

                    // evaluate having-clause
                    if (oldData != null)
                    {
                        int count = 0;
                        for (MultiKey<EventBean> anOldData : oldData)
                        {
                            MultiKeyUntyped mk = oldDataMultiKey[count];
                            aggregationService.setCurrentAccess(mk);

                            // Filter the having clause
                            Boolean result = (Boolean) optionalHavingNode.evaluate(anOldData.getArray(), false, statementContext);
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
                                if (groupRepsView.put(mk, anOldData.getArray()) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, anOldData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }
                            count++;
                        }
                    }
                }
            }

            generateOutputBatchedArr(groupRepsView, true, generateSynthetic, newEvents, newEventsSortKey);

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
        else // (outputLimitLimitType == OutputLimitLimitType.LAST)
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

            groupRepsView.clear();
            for (UniformPair<Set<MultiKey<EventBean>>> pair : joinEventsSet)
            {
                Set<MultiKey<EventBean>> newData = pair.getFirst();
                Set<MultiKey<EventBean>> oldData = pair.getSecond();

                if (isUnidirectional)
                {
                    this.clear();
                }

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (MultiKey<EventBean> aNewData : newData)
                    {
                        MultiKeyUntyped mk = generateGroupKey(aNewData.getArray(), true);

                        // if this is a newly encountered group, generate the remove stream event
                        if (groupRepsView.put(mk, aNewData.getArray()) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, aNewData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }
                        aggregationService.applyEnter(aNewData.getArray(), mk, statementContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (MultiKey<EventBean> anOldData : oldData)
                    {
                        MultiKeyUntyped mk = generateGroupKey(anOldData.getArray(), true);

                        if (groupRepsView.put(mk, anOldData.getArray()) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, anOldData.getArray(), false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }

                        aggregationService.applyLeave(anOldData.getArray(), mk, statementContext);
                    }
                }
            }

            generateOutputBatchedArr(groupRepsView, true, generateSynthetic, newEvents, newEventsSortKey);

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

            Map<MultiKeyUntyped, EventBean> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean>();

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, keysAndEvents, true);
                MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, keysAndEvents, false);

                if (isSelectRStream)
                {
                    generateOutputBatched(keysAndEvents, false, generateSynthetic, oldEvents, oldEventsSortKey, statementContext);
                }

                EventBean[] eventsPerStream = new EventBean[1];
                if (newData != null)
                {
                    // apply new data to aggregates
                    int count = 0;
                    for (EventBean aNewData : newData)
                    {
                        eventsPerStream[0] = aNewData;
                        aggregationService.applyEnter(eventsPerStream, newDataMultiKey[count], statementContext);
                        count++;
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

                generateOutputBatched(keysAndEvents, true, generateSynthetic, newEvents, newEventsSortKey, statementContext);

                keysAndEvents.clear();
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
            EventBean[] eventsPerStream = new EventBean[1];

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

            if (isSelectRStream)
            {
                generateOutputBatchedArr(groupRepsView, false, generateSynthetic, oldEvents, oldEventsSortKey);
            }

            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (EventBean aNewData : newData)
                    {
                        eventsPerStream[0] = aNewData;
                        MultiKeyUntyped mk = generateGroupKey(eventsPerStream, true);

                        // if this is a newly encountered group, generate the remove stream event
                        if (groupRepsView.put(mk, new EventBean[] {aNewData}) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }
                        aggregationService.applyEnter(eventsPerStream, mk, statementContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (EventBean anOldData : oldData)
                    {
                        eventsPerStream[0] = anOldData;
                        MultiKeyUntyped mk = generateGroupKey(eventsPerStream, true);

                        if (groupRepsView.put(mk, new EventBean[] {anOldData}) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }

                        aggregationService.applyLeave(eventsPerStream, mk, statementContext);
                    }
                }
            }

            generateOutputBatchedArr(groupRepsView, true, generateSynthetic, newEvents, newEventsSortKey);

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

            if (this.optionalHavingNode == null) {

                groupRepsView.clear();
                for (UniformPair<EventBean[]> pair : viewEventsList)
                {
                    EventBean[] newData = pair.getFirst();
                    EventBean[] oldData = pair.getSecond();

                    if (newData != null)
                    {
                        // apply new data to aggregates
                        for (EventBean aNewData : newData)
                        {
                            EventBean[] eventsPerStream = new EventBean[] {aNewData};
                            MultiKeyUntyped mk = generateGroupKey(eventsPerStream, true);

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
                                // if this is a newly encountered group, generate the remove stream event
                                if (groupRepsView.put(mk, eventsPerStream) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }
                            aggregationService.applyEnter(eventsPerStream, mk, statementContext);
                        }
                    }
                    if (oldData != null)
                    {
                        // apply old data to aggregates
                        for (EventBean anOldData : oldData)
                        {
                            EventBean[] eventsPerStream = new EventBean[] {anOldData};
                            MultiKeyUntyped mk = generateGroupKey(eventsPerStream, true);

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
                                if (groupRepsView.put(mk, eventsPerStream) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }

                            aggregationService.applyLeave(eventsPerStream, mk, statementContext);
                        }
                    }
                }
            }
            else { // having clause present, having clause evaluates at the level of individual posts
                EventBean[] eventsPerStreamOneStream = new EventBean[1];
                groupRepsView.clear();
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
                            aggregationService.applyEnter(eventsPerStreamOneStream, newDataMultiKey[i], statementContext);
                        }
                    }
                    if (oldData != null)
                    {
                        // apply old data to aggregates
                        for (int i = 0; i < oldData.length; i++)
                        {
                            eventsPerStreamOneStream[0] = oldData[i];
                            aggregationService.applyLeave(eventsPerStreamOneStream, oldDataMultiKey[i], statementContext);
                        }
                    }

                    // evaluate having-clause
                    if (newData != null)
                    {
                        for (int i = 0; i < newData.length; i++)
                        {
                            MultiKeyUntyped mk = newDataMultiKey[i];
                            eventsPerStreamOneStream[0] = newData[i];
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
                            boolean pass = outputStateGroup.updateOutputCondition(0, 1);
                            if (pass) {
                                EventBean[] eventsPerStream = new EventBean[] {newData[i]};
                                if (groupRepsView.put(mk, eventsPerStream) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, eventsPerStream, true, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }
                        }
                    }

                    // evaluate having-clause
                    if (oldData != null)
                    {
                        for (int i = 0; i < oldData.length; i++)
                        {
                            MultiKeyUntyped mk = oldDataMultiKey[i];
                            eventsPerStreamOneStream[0] = oldData[i];
                            aggregationService.setCurrentAccess(mk);

                            // Filter the having clause
                            Boolean result = (Boolean) optionalHavingNode.evaluate(eventsPerStreamOneStream, false, statementContext);
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
                            boolean pass = outputStateGroup.updateOutputCondition(0, 1);
                            if (pass) {
                                EventBean[] eventsPerStream = new EventBean[] {oldData[i]};
                                if (groupRepsView.put(mk, eventsPerStream) == null)
                                {
                                    if (isSelectRStream)
                                    {
                                        generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            generateOutputBatchedArr(groupRepsView, true, generateSynthetic, newEvents, newEventsSortKey);

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
        else // (outputLimitLimitType == OutputLimitLimitType.LAST)
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

            groupRepsView.clear();
            for (UniformPair<EventBean[]> pair : viewEventsList)
            {
                EventBean[] newData = pair.getFirst();
                EventBean[] oldData = pair.getSecond();

                if (newData != null)
                {
                    // apply new data to aggregates
                    for (EventBean aNewData : newData)
                    {
                        EventBean[] eventsPerStream = new EventBean[] {aNewData};
                        MultiKeyUntyped mk = generateGroupKey(eventsPerStream, true);

                        // if this is a newly encountered group, generate the remove stream event
                        if (groupRepsView.put(mk, eventsPerStream) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }
                        aggregationService.applyEnter(eventsPerStream, mk, statementContext);
                    }
                }
                if (oldData != null)
                {
                    // apply old data to aggregates
                    for (EventBean anOldData : oldData)
                    {
                        EventBean[] eventsPerStream = new EventBean[] {anOldData};
                        MultiKeyUntyped mk = generateGroupKey(eventsPerStream, true);

                        if (groupRepsView.put(mk, eventsPerStream) == null)
                        {
                            if (isSelectRStream)
                            {
                                generateOutputBatched(mk, eventsPerStream, false, generateSynthetic, oldEvents, oldEventsSortKey);
                            }
                        }

                        aggregationService.applyLeave(eventsPerStream, mk, statementContext);
                    }
                }
            }

            generateOutputBatchedArr(groupRepsView, true, generateSynthetic, newEvents, newEventsSortKey);

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
}

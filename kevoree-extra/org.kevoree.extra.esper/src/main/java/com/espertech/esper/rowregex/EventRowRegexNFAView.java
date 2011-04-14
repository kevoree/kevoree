package com.espertech.esper.rowregex;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.agg.AggregationServiceMatchRecognize;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.epl.expression.ExprPreviousMatchRecognizeNode;
import com.espertech.esper.epl.spec.MatchRecognizeDefineItem;
import com.espertech.esper.epl.spec.MatchRecognizeMeasureItem;
import com.espertech.esper.epl.spec.MatchRecognizeSkipEnum;
import com.espertech.esper.epl.spec.MatchRecognizeSpec;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * View for match recognize support.
 */
public class EventRowRegexNFAView extends ViewSupport
{
    private static final Log log = LogFactory.getLog(EventRowRegexNFAView.class);
    private static final boolean IS_DEBUG = false;
    private static final Iterator<EventBean> NULL_ITERATOR = new SingleEventIterator(null);

    private final MatchRecognizeSpec matchRecognizeSpec;
    private final boolean isUnbound;
    private final boolean isIterateOnly;
    private final boolean isSelectAsksMultimatches;

    private final EventType compositeEventType;
    private final EventType rowEventType;
    private final StatementContext statementContext;
    private final AggregationServiceMatchRecognize aggregationService;

    // for interval-handling
    private final ScheduleSlot scheduleSlot;
    private final EPStatementHandleCallback handle;
    private final TreeMap<Long, Object> schedule;

    private final ExprEvaluator[] columnEvaluators;
    private final String[] columnNames;

    private final RegexNFAState[] startStates;
    private final RegexNFAState[] allStates;

    private final String[] variablesArray;
    private final LinkedHashMap<String, Pair<Integer, Boolean>> variableStreams;
    private final Map<Integer, String> streamsVariables;
    private final Set<String> variablesSingle;

    // state
    private RegexPartitionStateRepo regexPartitionStateRepo;
    private LinkedHashSet<EventBean> windowMatchedEventset; // this is NOT per partition - some optimizations are done for batch-processing (minus is out-of-sequence in partition) 
    private int eventSequenceNumber;

    /**
     * Ctor.
     * @param compositeEventType final event type
     * @param rowEventType event type for input rows
     * @param matchRecognizeSpec specification
     * @param variableStreams variables and their assigned stream number
     * @param streamsVariables stream number and the assigned variable
     * @param variablesSingle single variables
     * @param statementContext statement context
     * @param callbacksPerIndex  for handling the 'prev' function
     * @param aggregationService handles aggregations
     * @param isUnbound true if unbound stream
     * @param isIterateOnly true for iterate-only
     * @param isSelectAsksMultimatches if asking for multimatches
     */
    public EventRowRegexNFAView(EventType compositeEventType,
                                EventType rowEventType,
                                MatchRecognizeSpec matchRecognizeSpec,
                                LinkedHashMap<String, Pair<Integer, Boolean>> variableStreams,
                                Map<Integer, String> streamsVariables,
                                Set<String> variablesSingle,
                                StatementContext statementContext,
                                TreeMap<Integer, List<ExprPreviousMatchRecognizeNode>> callbacksPerIndex,
                                AggregationServiceMatchRecognize aggregationService,
                                boolean isUnbound,
                                boolean isIterateOnly,
                                boolean isSelectAsksMultimatches)
    {
        this.matchRecognizeSpec = matchRecognizeSpec;
        this.compositeEventType = compositeEventType;
        this.rowEventType = rowEventType;
        this.variableStreams = variableStreams;
        this.variablesArray = variableStreams.keySet().toArray(new String[variableStreams.keySet().size()]);
        this.streamsVariables = streamsVariables;
        this.variablesSingle = variablesSingle;
        this.aggregationService = aggregationService;
        this.isUnbound = isUnbound;
        this.isIterateOnly = isIterateOnly;
        this.statementContext = statementContext;
        this.isSelectAsksMultimatches = isSelectAsksMultimatches;

        if (matchRecognizeSpec.getInterval() != null)
        {
            scheduleSlot = statementContext.getScheduleBucket().allocateSlot();
            ScheduleHandleCallback callback = new ScheduleHandleCallback() {
                public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
                {
                    EventRowRegexNFAView.this.triggered();
                }
            };
            handle = new EPStatementHandleCallback(statementContext.getEpStatementHandle(), callback);
            schedule = new TreeMap<Long, Object>();
        }
        else
        {
            scheduleSlot = null;
            handle = null;
            schedule = null;
        }

        this.windowMatchedEventset = new LinkedHashSet<EventBean>();

        // handle "previous" function nodes (performance-optimized for direct index access)
        RegexPartitionStateRandomAccessGetter randomAccessByIndexGetter;
        if (!callbacksPerIndex.isEmpty())
        {
            // Build an array of indexes
            int[] randomAccessIndexesRequested = new int[callbacksPerIndex.size()];
            int count = 0;
            for (Map.Entry<Integer, List<ExprPreviousMatchRecognizeNode>> entry : callbacksPerIndex.entrySet())
            {
                randomAccessIndexesRequested[count] = entry.getKey();
                count++;
            }
            randomAccessByIndexGetter = new RegexPartitionStateRandomAccessGetter(randomAccessIndexesRequested, isUnbound);

            // Since an expression such as "prior(2, price), prior(8, price)" translates into {2, 8} the relative index is {0, 1}.
            // Map the expression-supplied index to a relative index
            count = 0;
            for (Map.Entry<Integer, List<ExprPreviousMatchRecognizeNode>> entry : callbacksPerIndex.entrySet())
            {
                for (ExprPreviousMatchRecognizeNode callback : entry.getValue())
                {
                    callback.setGetter(randomAccessByIndexGetter);
                    callback.setAssignedIndex(count);
                }
                count++;
            }
        }
        else
        {
            randomAccessByIndexGetter = null;
        }

        Map<String, ExprNode> variableDefinitions = new LinkedHashMap<String, ExprNode>();
        for (MatchRecognizeDefineItem defineItem : matchRecognizeSpec.getDefines())
        {
            variableDefinitions.put(defineItem.getIdentifier(), defineItem.getExpression());
        }

        // build states
        RegexNFAStrandResult strand = EventRowRegexHelper.recursiveBuildStartStates(matchRecognizeSpec.getPattern(), variableDefinitions, variableStreams);
        startStates = strand.getStartStates().toArray(new RegexNFAState[strand.getStartStates().size()]);
        allStates = strand.getAllStates().toArray(new RegexNFAState[strand.getAllStates().size()]);

        if (log.isDebugEnabled() || IS_DEBUG)
        {
            log.info("NFA tree:\n" + print(startStates));
        }

        // create evaluators
        columnNames = new String[matchRecognizeSpec.getMeasures().size()];
        columnEvaluators = new ExprEvaluator[matchRecognizeSpec.getMeasures().size()];
        int count = 0;
        for (MatchRecognizeMeasureItem measureItem : matchRecognizeSpec.getMeasures())
        {
            columnNames[count] = measureItem.getName();
            columnEvaluators[count] = measureItem.getExpr().getExprEvaluator();
            count++;
        }

        // create state repository
        if (this.matchRecognizeSpec.getPartitionByExpressions().isEmpty())
        {
            regexPartitionStateRepo = new RegexPartitionStateRepoNoGroup(randomAccessByIndexGetter, matchRecognizeSpec.getInterval() != null);
        }
        else
        {
            regexPartitionStateRepo = new RegexPartitionStateRepoGroup(randomAccessByIndexGetter, ExprNodeUtility.getEvaluators(matchRecognizeSpec.getPartitionByExpressions()), matchRecognizeSpec.getInterval() != null, statementContext);
        }
    }

    public void update(EventBean[] newData, EventBean[] oldData)
    {
        if (isIterateOnly)
        {
            if (oldData != null)
            {
                regexPartitionStateRepo.removeOld(oldData, false, new boolean[oldData.length]);
            }
            if (newData != null)
            {
                for (EventBean newEvent : newData)
                {
                    RegexPartitionState partitionState = regexPartitionStateRepo.getState(newEvent, true);
                    if ((partitionState != null) && (partitionState.getRandomAccess() != null))
                    {
                        partitionState.getRandomAccess().newEventPrepare(newEvent);
                    }
                }
            }            
            return;
        }

        if (oldData != null)
        {
            boolean isOutOfSequenceRemove = false;

            EventBean first = null;
            if (!windowMatchedEventset.isEmpty())
            {
                first = windowMatchedEventset.iterator().next();
            }

            // remove old data, if found in set
            boolean[] found = new boolean[oldData.length];
            int count = 0;

            // detect out-of-sequence removes
            for (EventBean oldEvent : oldData)
            {
                boolean removed = windowMatchedEventset.remove(oldEvent);
                if (removed)
                {
                    if ((oldEvent != first) && (first != null))
                    {
                        isOutOfSequenceRemove = true;
                    }
                    found[count++] = true;
                    if (!windowMatchedEventset.isEmpty())
                    {
                        first = windowMatchedEventset.iterator().next();
                    }
                }
            }

            // remove old events from repository - and let the repository know there are no interesting events left
            regexPartitionStateRepo.removeOld(oldData, windowMatchedEventset.isEmpty(), found);

            // reset, rebuilding state
            if (isOutOfSequenceRemove)
            {
                regexPartitionStateRepo = regexPartitionStateRepo.copyForIterate();
                windowMatchedEventset = new LinkedHashSet<EventBean>();
                Iterator<EventBean> parentEvents = this.getParent().iterator();
                EventRowRegexIteratorResult iteratorResult = processIterator(startStates, parentEvents, regexPartitionStateRepo);
                eventSequenceNumber = iteratorResult.getEventSequenceNum();
            }
        }

        if (newData == null)
        {
            return;
        }
        
        List<RegexNFAStateEntry> endStates = new ArrayList<RegexNFAStateEntry>();
        List<RegexNFAStateEntry> nextStates = new ArrayList<RegexNFAStateEntry>();

        for (EventBean newEvent : newData)
        {
            eventSequenceNumber++;

            // get state holder for this event
            RegexPartitionState partitionState = regexPartitionStateRepo.getState(newEvent, true);
            List<RegexNFAStateEntry> currentStates = partitionState.getCurrentStates();
            
            // add start states for each new event
            for (RegexNFAState startState : startStates)
            {
                long time = 0;
                if (matchRecognizeSpec.getInterval() != null)
                {
                    time = statementContext.getSchedulingService().getTime();
                }
                currentStates.add(new RegexNFAStateEntry(eventSequenceNumber, time, startState, new EventBean[variableStreams.size()], new int[allStates.length], null, partitionState.getOptionalKeys()));
            }

            if (partitionState.getRandomAccess() != null)
            {
                partitionState.getRandomAccess().newEventPrepare(newEvent);
            }

            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()) || (IS_DEBUG))
            {
                log.info("Evaluating event " + newEvent.getUnderlying() + "\n" +
                    "current : " + printStates(currentStates));
            }

            step(currentStates, newEvent, nextStates, endStates, !isUnbound, eventSequenceNumber, partitionState.getOptionalKeys());

            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()) || (IS_DEBUG))
            {
                log.info("Evaluated event " + newEvent.getUnderlying() + "\n" +
                    "next : " + printStates(nextStates) + "\n" +
                    "end : " + printStates(endStates));
            }

            partitionState.setCurrentStates(nextStates);
            nextStates = currentStates;
            nextStates.clear();
        }

        if (endStates.isEmpty())
        {
            return;
        }

        // perform inter-ranking and elimination of duplicate matches
        if (!matchRecognizeSpec.isAllMatches())
        {
            endStates = rankEndStatesMultiPartition(endStates);
        }

        // handle interval for the set of matches
        if (matchRecognizeSpec.getInterval() != null)
        {
            Iterator<RegexNFAStateEntry> it = endStates.iterator();
            for (;it.hasNext();)
            {
                RegexNFAStateEntry endState = it.next();
                RegexPartitionState partitionState = regexPartitionStateRepo.getState(endState.getPartitionKey());
                if (partitionState == null)
                {
                    log.warn("Null partition state encountered, skipping row");
                    continue;
                }

                long matchBeginTime = endState.getMatchBeginEventTime();
                long current = statementContext.getSchedulingService().getTime();
                long deltaFromStart = current - matchBeginTime;
                long deltaUntil = matchRecognizeSpec.getInterval().getMSec() - deltaFromStart;

                if (schedule.containsKey(matchBeginTime))
                {
                    scheduleCallback(deltaUntil, endState);
                    it.remove();
                }
                else
                {
                    if (deltaFromStart < deltaUntil)
                    {
                        scheduleCallback(deltaUntil, endState);
                        it.remove();
                    }
                }
            }
            if (endStates.isEmpty())
            {
                return;
            }
        }
        // handle skip for incremental mode
        else if (matchRecognizeSpec.getSkip().getSkip() == MatchRecognizeSkipEnum.PAST_LAST_ROW)
        {
            Iterator<RegexNFAStateEntry> endStateIter = endStates.iterator();
            for (;endStateIter.hasNext();)
            {
                RegexNFAStateEntry endState = endStateIter.next();
                RegexPartitionState partitionState = regexPartitionStateRepo.getState(endState.getPartitionKey());
                if (partitionState == null)
                {
                    log.warn("Null partition state encountered, skipping row");
                    continue;
                }

                Iterator<RegexNFAStateEntry> stateIter = partitionState.getCurrentStates().iterator();
                for (;stateIter.hasNext();)
                {
                    RegexNFAStateEntry currentState = stateIter.next();
                    if (currentState.getMatchBeginEventSeqNo() <= endState.getMatchEndEventSeqNo())
                    {
                        stateIter.remove();
                    }
                }
            }
        }
        else if (matchRecognizeSpec.getSkip().getSkip() == MatchRecognizeSkipEnum.TO_NEXT_ROW)
        {
            Iterator<RegexNFAStateEntry> endStateIter = endStates.iterator();
            for (;endStateIter.hasNext();)
            {
                RegexNFAStateEntry endState = endStateIter.next();
                RegexPartitionState partitionState = regexPartitionStateRepo.getState(endState.getPartitionKey());
                if (partitionState == null)
                {
                    log.warn("Null partition state encountered, skipping row");
                    continue;
                }

                Iterator<RegexNFAStateEntry> stateIter = partitionState.getCurrentStates().iterator();
                for (;stateIter.hasNext();)
                {
                    RegexNFAStateEntry currentState = stateIter.next();
                    if (currentState.getMatchBeginEventSeqNo() <= endState.getMatchBeginEventSeqNo())
                    {
                        stateIter.remove();
                    }
                }
            }
        }

        EventBean[] out = new EventBean[endStates.size()];
        int count = 0;
        for (RegexNFAStateEntry endState : endStates)
        {
            out[count++] = generateOutputRow(endState);
        }

        updateChildren(out, null);
    }

    private RegexNFAStateEntry rankEndStates(List<RegexNFAStateEntry> endStates) {

        RegexNFAStateEntry found = null;
        int min = Integer.MAX_VALUE;
        boolean multipleMinimums = false;
        for (RegexNFAStateEntry state : endStates)
        {
            if (state.getMatchBeginEventSeqNo() < min)
            {
                found = state;
                min = state.getMatchBeginEventSeqNo();
            }
            else if (state.getMatchBeginEventSeqNo() == min)
            {
                multipleMinimums = true;
            }
        }

        if (!multipleMinimums)
        {
            Collections.singletonList(found);
        }

        int[] best = null;
        found = null;
        for (RegexNFAStateEntry state : endStates)
        {
            if (state.getMatchBeginEventSeqNo() != min)
            {
                continue;
            }
            if (best == null)
            {
                best = state.getGreedycountPerState();
                found = state;
            }
            else
            {
                int[] current = state.getGreedycountPerState();
                if (compare(current, best))
                {
                    best = current;
                    found = state;
                }
            }
        }

        return found;
    }

    private boolean compare(int[] current, int[] best)
    {
        for (RegexNFAState state : allStates)
        {
            if (state.isGreedy() == null)
            {
                continue;
            }
            if (state.isGreedy())
            {
                if (current[state.getNodeNumFlat()] > best[state.getNodeNumFlat()])
                {
                    return true;
                }
            } else
            {
                if (current[state.getNodeNumFlat()] < best[state.getNodeNumFlat()])
                {
                    return true;
                }
            }
        }

        return false;
    }

 private EventRowRegexIteratorResult processIterator(RegexNFAState[] startStates,
                                                     Iterator<EventBean> events,
                                                     RegexPartitionStateRepo regexPartitionStateRepo)
    {
        List<RegexNFAStateEntry> endStates = new ArrayList<RegexNFAStateEntry>();
        List<RegexNFAStateEntry> nextStates = new ArrayList<RegexNFAStateEntry>();
        List<RegexNFAStateEntry> currentStates;
        int eventSequenceNumber = 0;

        EventBean event;
        for (;events.hasNext();)
        {
            event = events.next();
            eventSequenceNumber++;

            RegexPartitionState partitionState = regexPartitionStateRepo.getState(event, false);
            currentStates = partitionState.getCurrentStates();

            // add start states for each new event
            for (RegexNFAState startState : startStates)
            {
                long time = 0;
                if (matchRecognizeSpec.getInterval() != null)
                {
                    time = statementContext.getSchedulingService().getTime();
                }
                currentStates.add(new RegexNFAStateEntry(eventSequenceNumber, time, startState, new EventBean[variableStreams.size()], new int[allStates.length], null, partitionState.getOptionalKeys()));
            }

            if (partitionState.getRandomAccess() != null)
            {
                partitionState.getRandomAccess().existingEventPrepare(event);
            }

            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()) || (IS_DEBUG))
            {
                log.info("Evaluating event " + event.getUnderlying() + "\n" +
                    "current : " + printStates(currentStates));
            }

            step(currentStates, event, nextStates, endStates, false, eventSequenceNumber, partitionState.getOptionalKeys());

            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()) || (IS_DEBUG))
            {
                log.info("Evaluating event " + event.getUnderlying() + "\n" +
                    "next : " + printStates(nextStates) + "\n" +
                    "end : " + printStates(endStates));
            }

            partitionState.setCurrentStates(nextStates);
            nextStates = currentStates;
            nextStates.clear();
        }

        return new EventRowRegexIteratorResult(endStates, eventSequenceNumber);
    }

    public EventType getEventType() {
        return rowEventType;
    }

    public Iterator<EventBean> iterator() {
        if (isUnbound)
        {
            return NULL_ITERATOR;
        }

        Iterator<EventBean> it = parent.iterator();

        RegexPartitionStateRepo regexPartitionStateRepoNew = regexPartitionStateRepo.copyForIterate();

        EventRowRegexIteratorResult iteratorResult = processIterator(startStates, it, regexPartitionStateRepoNew);
        List<RegexNFAStateEntry> endStates = iteratorResult.getEndStates();
        if (endStates.isEmpty())
        {
            return NULL_ITERATOR;
        }
        else
        {
            endStates = rankEndStatesMultiPartition(endStates);
        }

        List<EventBean> output = new ArrayList<EventBean>();
        for (RegexNFAStateEntry endState : endStates)
        {
            output.add(generateOutputRow(endState));
        }
        return output.iterator();
    }

    private List<RegexNFAStateEntry> rankEndStatesMultiPartition(List<RegexNFAStateEntry> endStates) {
        if (endStates.isEmpty())
        {
            return endStates;
        }
        if (endStates.size() == 1)
        {
            return endStates;
        }

        // unpartitioned case -
        if (matchRecognizeSpec.getPartitionByExpressions().isEmpty())
        {
            return rankEndStatesWithinPartitionByStart(endStates);
        }

        // partitioned case - structure end states by partition
        Map<MultiKeyUntyped, Object> perPartition = new LinkedHashMap<MultiKeyUntyped, Object>();
        for (RegexNFAStateEntry endState : endStates)
        {
            Object value = perPartition.get(endState.getPartitionKey());
            if (value == null)
            {
                perPartition.put(endState.getPartitionKey(), endState);
            }
            else if (value instanceof List)
            {
                List<RegexNFAStateEntry> entries = (List<RegexNFAStateEntry>) value;
                entries.add(endState);
            }
            else
            {
                List<RegexNFAStateEntry> entries = new ArrayList<RegexNFAStateEntry>();
                entries.add((RegexNFAStateEntry) value);
                entries.add(endState);
                perPartition.put(endState.getPartitionKey(), entries);
            }
        }

        List<RegexNFAStateEntry> finalEndStates = new ArrayList<RegexNFAStateEntry>();
        for (Map.Entry<MultiKeyUntyped, Object> entry : perPartition.entrySet())
        {
            if (entry.getValue() instanceof RegexNFAStateEntry)
            {
                finalEndStates.add((RegexNFAStateEntry) entry.getValue());
            }
            else
            {
                List<RegexNFAStateEntry> entries = (List<RegexNFAStateEntry>) entry.getValue();
                finalEndStates.addAll(rankEndStatesWithinPartitionByStart(entries));
            }            
        }
        return finalEndStates;
    }

    private List<RegexNFAStateEntry> rankEndStatesWithinPartitionByStart(List<RegexNFAStateEntry> endStates) {
        if (endStates.isEmpty())
        {
            return endStates;
        }
        if (endStates.size() == 1)
        {
            return endStates;
        }

        TreeMap<Integer, Object> endStatesPerBeginEvent = new TreeMap<Integer, Object>();
        for (RegexNFAStateEntry entry : endStates)
        {
            Integer endNum = entry.getMatchBeginEventSeqNo();
            Object value = endStatesPerBeginEvent.get(endNum);
            if (value == null)
            {
                endStatesPerBeginEvent.put(endNum, entry);
            }
            else if (value instanceof List)
            {
                List<RegexNFAStateEntry> entries = (List<RegexNFAStateEntry>) value;
                entries.add(entry);
            }
            else
            {
                List<RegexNFAStateEntry> entries = new ArrayList<RegexNFAStateEntry>();
                entries.add((RegexNFAStateEntry) value);
                entries.add(entry);
                endStatesPerBeginEvent.put(endNum, entries);
            }
        }

        if (endStatesPerBeginEvent.size() == 1)
        {
            List<RegexNFAStateEntry> endStatesUnranked = (List<RegexNFAStateEntry>) endStatesPerBeginEvent.values().iterator().next();
            if (matchRecognizeSpec.isAllMatches())
            {
                return endStatesUnranked;
            }
            RegexNFAStateEntry chosen = rankEndStates(endStatesUnranked);
            return Collections.singletonList(chosen);
        }

        List<RegexNFAStateEntry> endStatesRanked = new ArrayList<RegexNFAStateEntry>();
        Set<Integer> keyset = endStatesPerBeginEvent.keySet();
        Integer[] keys = keyset.toArray(new Integer[keyset.size()]);
        for (Integer key : keys)
        {
            Object value = endStatesPerBeginEvent.remove(key);
            if (value == null)
            {
                continue;
            }

            RegexNFAStateEntry entryTaken;
            if (value instanceof List)
            {
                List<RegexNFAStateEntry> endStatesUnranked = (List<RegexNFAStateEntry>) value;
                if (endStatesUnranked.isEmpty())
                {
                    continue;
                }
                entryTaken = rankEndStates(endStatesUnranked);

                if (matchRecognizeSpec.isAllMatches())
                {
                    endStatesRanked.addAll(endStatesUnranked);  // we take all matches and don't rank except to determine skip-past
                }
                else
                {
                    endStatesRanked.add(entryTaken);
                }
            }
            else
            {
                entryTaken = (RegexNFAStateEntry) value;
                endStatesRanked.add(entryTaken);
            }
            // could be null as removals take place

            if (entryTaken != null)
            {
                if (matchRecognizeSpec.getSkip().getSkip() == MatchRecognizeSkipEnum.PAST_LAST_ROW)
                {
                    int skipPastRow = entryTaken.getMatchEndEventSeqNo();
                    removeSkippedEndStates(endStatesPerBeginEvent, skipPastRow);
                }
                else if (matchRecognizeSpec.getSkip().getSkip() == MatchRecognizeSkipEnum.TO_NEXT_ROW)
                {
                    int skipPastRow = entryTaken.getMatchBeginEventSeqNo();
                    removeSkippedEndStates(endStatesPerBeginEvent, skipPastRow);
                }
            }
        }

        return endStatesRanked;
    }

    private void removeSkippedEndStates(TreeMap<Integer, Object> endStatesPerEndEvent, int skipPastRow)
    {
        for (Map.Entry<Integer, Object> entry : endStatesPerEndEvent.entrySet())
        {
            Object value = entry.getValue();

            if (value instanceof List)
            {
                List<RegexNFAStateEntry> endStatesUnranked = (List<RegexNFAStateEntry>) value;
                Iterator<RegexNFAStateEntry> it = endStatesUnranked.iterator();
                for (;it.hasNext();)
                {
                    RegexNFAStateEntry endState = it.next();
                    if (endState.getMatchBeginEventSeqNo() <= skipPastRow)
                    {
                        it.remove();
                    }
                }
            }
            else
            {
                RegexNFAStateEntry endState = (RegexNFAStateEntry) value;
                if (endState.getMatchBeginEventSeqNo() <= skipPastRow)
                {
                    endStatesPerEndEvent.put(entry.getKey(), null);
                }
            }
        }
    }

    private void step(List<RegexNFAStateEntry> currentStates,
                      EventBean event,
                      List<RegexNFAStateEntry> nextStates,
                      List<RegexNFAStateEntry> endStates,
                      boolean isRetainEventSet,
                      int currentEventSequenceNumber,
                      MultiKeyUntyped partitionKey)
    {
        for (RegexNFAStateEntry currentState : currentStates)
        {
            EventBean[] eventsPerStream = currentState.getEventsPerStream();
            int currentStateStreamNum = currentState.getState().getStreamNum();
            eventsPerStream[currentStateStreamNum] = event;

            if (currentState.getState().matches(eventsPerStream, statementContext))
            {
                if (isRetainEventSet)
                {
                    this.windowMatchedEventset.add(event);
                }
                List<RegexNFAState> nextStatesFromHere = currentState.getState().getNextStates();

                // save state for each next state
                boolean copy = nextStatesFromHere.size() > 1;
                for (RegexNFAState next : nextStatesFromHere)
                {
                    EventBean[] eventsForState = eventsPerStream;
                    MultimatchState[] multimatches = currentState.getOptionalMultiMatches();
                    int[] greedyCounts = currentState.getGreedycountPerState();

                    if (copy)
                    {
                        eventsForState = new EventBean[eventsForState.length];
                        System.arraycopy(eventsPerStream, 0, eventsForState, 0, eventsForState.length);

                        int[] greedyCountsCopy = new int[greedyCounts.length];
                        System.arraycopy(greedyCounts, 0, greedyCountsCopy, 0, greedyCounts.length);
                        greedyCounts = greedyCountsCopy;

                        if (isSelectAsksMultimatches) {
                            multimatches = deepCopy(multimatches);
                        }
                    }

                    if ((isSelectAsksMultimatches) && (currentState.getState().isMultiple()))
                    {
                        multimatches = addTag(currentState.getState().getStreamNum(), event, multimatches);
                    }

                    if ((currentState.getState().isGreedy() != null) && (currentState.getState().isGreedy()))
                    {
                        greedyCounts[currentState.getState().getNodeNumFlat()]++;
                    }

                    RegexNFAStateEntry entry = new RegexNFAStateEntry(currentState.getMatchBeginEventSeqNo(), currentState.getMatchBeginEventTime(), next, eventsForState, greedyCounts, multimatches, partitionKey);
                    if (next instanceof RegexNFAStateEnd)
                    {
                        entry.setMatchEndEventSeqNo(currentEventSequenceNumber);
                        endStates.add(entry);
                    }
                    else
                    {
                        nextStates.add(entry);
                    }
                }
            }
        }
    }

    private MultimatchState[] deepCopy(MultimatchState[] multimatchStates) {
        if (multimatchStates == null)
        {
            return null;
        }

        MultimatchState[] copy = new MultimatchState[multimatchStates.length];
        for (int i = 0; i < copy.length; i++)
        {
            if (multimatchStates[i] != null)
            {
                copy[i] = new MultimatchState(multimatchStates[i]);
            }
        }

        return copy;
    }

    private MultimatchState[] addTag(int streamNum, EventBean event, MultimatchState[] multimatches)
    {
        if (multimatches == null)
        {
            multimatches = new MultimatchState[variablesArray.length];
        }

        MultimatchState state = multimatches[streamNum];
        if (state == null)
        {
            multimatches[streamNum] = new MultimatchState(event);
            return multimatches;
        }

        multimatches[streamNum].add(event);
        return multimatches;
    }

    private String printStates(List<RegexNFAStateEntry> states) {
        StringBuilder buf = new StringBuilder();
        String delimiter = "";
        for (RegexNFAStateEntry state : states)
        {
            buf.append(delimiter);
            buf.append(state.getState().getNodeNumNested());

            buf.append("{");
            EventBean[] eventsPerStream = state.getEventsPerStream();
            if (eventsPerStream == null)
            {
                buf.append("null");
            }
            else
            {
                String eventDelimiter = "";
                for (Map.Entry<Integer, String> streamVariable : streamsVariables.entrySet())
                {
                    buf.append(eventDelimiter);
                    buf.append(streamVariable.getValue());
                    buf.append('=');
                    if (variablesSingle.contains(streamVariable.getValue()))
                    {
                        if (eventsPerStream[streamVariable.getKey()] == null)
                        {
                            buf.append("null");
                        }
                        else
                        {
                            buf.append(eventsPerStream[streamVariable.getKey()].getUnderlying());
                        }
                    }
                    else
                    {
                        int streamNum = state.getState().getStreamNum();
                        if (state.getOptionalMultiMatches() == null)
                        {
                            buf.append("null-mm");
                        }
                        else if (state.getOptionalMultiMatches()[streamNum] == null)
                        {
                            buf.append("no-entry");
                        }
                        else
                        {
                            buf.append("{");
                            String arrayEventDelimiter = "";
                            EventBean[] multiMatch = state.getOptionalMultiMatches()[streamNum].getBuffer();
                            int count = state.getOptionalMultiMatches()[streamNum].getCount();
                            for (int i = 0; i < count; i++)
                            {
                                buf.append(arrayEventDelimiter);
                                buf.append(multiMatch[i].getUnderlying());
                                arrayEventDelimiter = ", ";
                            }
                            buf.append("}");
                        }
                    }
                    eventDelimiter = ", ";
                }
            }
            buf.append("}");
            
            delimiter = ", ";
        }
        return buf.toString();
    }

    private String print(RegexNFAState[] states) {
        StringWriter writer = new StringWriter();
        PrintWriter buf = new PrintWriter(writer);
        Stack<RegexNFAState> currentStack = new Stack<RegexNFAState>();
        print(Arrays.asList(states), buf, 0, currentStack);
        return writer.toString();
    }

    private void print(List<RegexNFAState> states, PrintWriter writer, int indent, Stack<RegexNFAState> currentStack) {

        for (RegexNFAState state : states)
        {
            indent(writer, indent);
            if (currentStack.contains(state))
            {
                writer.println("(self)");
            }
            else
            {
                writer.println(printState(state));

                currentStack.push(state);
                print(state.getNextStates(), writer, indent + 4, currentStack);
                currentStack.pop();
            }
        }
    }

    private String printState(RegexNFAState state)
    {
        if (state instanceof RegexNFAStateEnd)
        {
            return "#" + state.getNodeNumNested();
        }
        else
        {
            return "#" + state.getNodeNumNested() + " " + state.getVariableName() + " s" + state.getStreamNum() + " defined as " + state;
        }
    }

    private void indent(PrintWriter writer, int indent)
    {
        for (int i = 0; i < indent; i++)
        {
            writer.append(' ');
        }
    }
    
    private EventBean generateOutputRow(RegexNFAStateEntry entry)
    {
        // we first generate a raw row of <String, Object> for each variable name.
        Map<String, Object> rowDataRaw = new HashMap<String, Object>();
        for (Map.Entry<String, Pair<Integer, Boolean>> variableDef : variableStreams.entrySet())
        {
            if (!variableDef.getValue().getSecond())
            {
                rowDataRaw.put(variableDef.getKey(), entry.getEventsPerStream()[variableDef.getValue().getFirst()]);
            }
        }
        if (aggregationService != null)
        {
            aggregationService.clearResults();
        }
        if (entry.getOptionalMultiMatches() != null)
        {
            MultimatchState[] multimatchState = entry.getOptionalMultiMatches();
            for (int i = 0; i < multimatchState.length; i++)
            {
                if (multimatchState[i] == null)
                {
                    continue;
                }
                EventBean[] multimatchEvents = multimatchState[i].getEventArray();
                rowDataRaw.put(variablesArray[i], multimatchEvents);

                if (aggregationService != null)
                {
                    EventBean[] eventsPerStream = entry.getEventsPerStream();

                    for (EventBean multimatchEvent : multimatchEvents)
                    {
                        eventsPerStream[i] = multimatchEvent;
                        aggregationService.applyEnter(eventsPerStream, i, statementContext);
                    }
                }
            }
        }
        EventBean rowRaw = statementContext.getEventAdapterService().adaptorForTypedMap(rowDataRaw, compositeEventType);

        Map<String, Object> row = new HashMap<String, Object>();
        int columnNum = 0;
        for (ExprEvaluator expression : columnEvaluators)
        {
            Object result = expression.evaluate(new EventBean[] {rowRaw}, true, statementContext);
            row.put(columnNames[columnNum], result);
            columnNum++;
        }

        return statementContext.getEventAdapterService().adaptorForTypedMap(row, rowEventType);
    }

    private void scheduleCallback(long msecAfterCurrentTime, RegexNFAStateEntry endState)
    {
        long matchBeginTime = endState.getMatchBeginEventTime();
        if (schedule.isEmpty())
        {
            schedule.put(matchBeginTime, endState);
            statementContext.getSchedulingService().add(msecAfterCurrentTime, handle, scheduleSlot);
        }
        else
        {
            Object value = schedule.get(matchBeginTime);
            if (value == null)
            {
                long currentFirstKey = schedule.firstKey();
                if (currentFirstKey > matchBeginTime)
                {
                    statementContext.getSchedulingService().remove(handle, scheduleSlot);
                    statementContext.getSchedulingService().add(msecAfterCurrentTime, handle, scheduleSlot);
                }

                schedule.put(matchBeginTime, endState);
            }
            else if (value instanceof RegexNFAStateEntry)
            {
                RegexNFAStateEntry valueEntry = (RegexNFAStateEntry) value;
                List<RegexNFAStateEntry> list = new ArrayList<RegexNFAStateEntry>();
                list.add(valueEntry);
                list.add(endState);
                schedule.put(matchBeginTime, list);
            }
            else
            {
                List<RegexNFAStateEntry> list = (List<RegexNFAStateEntry>) value;
                list.add(endState);
            }
        }
    }

    private void triggered()
    {
        long currentTime = statementContext.getSchedulingService().getTime();
        if (schedule.isEmpty())
        {
            return;
        }

        List<RegexNFAStateEntry> indicatables = new ArrayList<RegexNFAStateEntry>();
        while (true)
        {
            long firstKey = schedule.firstKey();
            long cutOffTime = currentTime - this.matchRecognizeSpec.getInterval().getMSec();
            if (firstKey > cutOffTime)
            {
                break;
            }

            Object value = schedule.remove(firstKey);

            if (value instanceof RegexNFAStateEntry)
            {
                indicatables.add((RegexNFAStateEntry) value);
            }
            else
            {
                List<RegexNFAStateEntry> list = (List<RegexNFAStateEntry>) value;
                indicatables.addAll(list);
            }

            if (schedule.isEmpty())
            {
                break;
            }
        }

        // schedule next
        if (!schedule.isEmpty())
        {
            long msecAfterCurrentTime = schedule.firstKey() + this.matchRecognizeSpec.getInterval().getMSec() - statementContext.getSchedulingService().getTime();
            statementContext.getSchedulingService().add(msecAfterCurrentTime, handle, scheduleSlot);
        }

        if (!matchRecognizeSpec.isAllMatches())
        {
            indicatables = rankEndStatesMultiPartition(indicatables);
        }

        EventBean[] out = new EventBean[indicatables.size()];
        int count = 0;
        for (RegexNFAStateEntry endState : indicatables)
        {
            out[count++] = generateOutputRow(endState);
        }

        updateChildren(out, null);
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.std;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.StoppableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public final class GroupByViewReclaimAged extends ViewSupport implements CloneableView, GroupByView
{
    private final ExprNode[] criteriaExpressions;
    private final ExprEvaluator[] criteriaEvaluators;
    private final StatementContext statementContext;
    private final long reclaimMaxAge;
    private final long reclaimFrequency;

    private EventBean[] eventsPerStream = new EventBean[1];
    private String[] propertyNames;

    private final Map<MultiKey<Object>, GroupByViewAgedEntry> subViewsPerKey = new HashMap<MultiKey<Object>, GroupByViewAgedEntry>();
    private final HashMap<GroupByViewAgedEntry, Pair<List<EventBean>, List<EventBean>>> groupedEvents = new HashMap<GroupByViewAgedEntry, Pair<List<EventBean>, List<EventBean>>>();
    private Long nextSweepTime = null;

    /**
     * Constructor.
     * @param statementContext contains required view services
     * @param criteriaExpressions is the fields from which to pull the values to group by
     * @param reclaimMaxAge age after which to reclaim group
     * @param reclaimFrequency frequency in which to check for groups to reclaim
     */
    public GroupByViewReclaimAged(StatementContext statementContext,
                                  ExprNode[] criteriaExpressions,
                                  ExprEvaluator[] criteriaEvaluators,
                                  double reclaimMaxAge, double reclaimFrequency)
    {
        this.statementContext = statementContext;
        this.criteriaExpressions = criteriaExpressions;
        this.criteriaEvaluators = criteriaEvaluators;
        this.reclaimMaxAge = (long) (reclaimMaxAge * 1000d);
        this.reclaimFrequency = (long) (reclaimFrequency * 1000d);

        propertyNames = new String[criteriaExpressions.length];
        for (int i = 0; i < criteriaExpressions.length; i++)
        {
            propertyNames[i] = criteriaExpressions[i].toExpressionString();
        }
    }

    public View cloneView(StatementContext statementContext)
    {
        return new GroupByViewReclaimAged(statementContext, criteriaExpressions, criteriaEvaluators, reclaimMaxAge, reclaimFrequency);
    }

    /**
     * Returns the field name that provides the key valie by which to group by.
     * @return field name providing group-by key.
     */
    public ExprNode[] getCriteriaExpressions()
    {
        return criteriaExpressions;
    }

    public final EventType getEventType()
    {
        // The schema is the parent view's schema
        return parent.getEventType();
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".update Updating view");
            dumpUpdateParams("GroupByView", newData, oldData);
        }

        long currentTime = statementContext.getTimeProvider().getTime();
        if ((nextSweepTime == null) || (nextSweepTime <= currentTime))
        {
            if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
            {
                log.debug("Reclaiming groups older then " + reclaimMaxAge + " msec and every " + reclaimFrequency + "msec in frequency");
            }
            nextSweepTime = currentTime + reclaimFrequency;
            sweep(currentTime);
        }

        // Algorithm for single new event
        if ((newData != null) && (oldData == null) && (newData.length == 1))
        {
            EventBean event = newData[0];
            EventBean[] newDataToPost = new EventBean[] {event};

            Object[] groupByValues = new Object[criteriaExpressions.length];
            eventsPerStream[0] = event;
            for (int i = 0; i < criteriaEvaluators.length; i++)
            {
                groupByValues[i] = criteriaEvaluators[i].evaluate(eventsPerStream, true, statementContext);
            }
            MultiKey<Object> groupByValuesKey = new MultiKey<Object>(groupByValues);

            // Get child views that belong to this group-by value combination
            GroupByViewAgedEntry subViews = this.subViewsPerKey.get(groupByValuesKey);

            // If this is a new group-by value, the list of subviews is null and we need to make clone sub-views
            if (subViews == null)
            {
                List<View> subviewsList = GroupByViewImpl.makeSubViews(this, propertyNames, groupByValuesKey.getArray(), statementContext);
                subViews = new GroupByViewAgedEntry(subviewsList, currentTime);
                subViewsPerKey.put(groupByValuesKey, subViews);
            }
            else {
                subViews.setLastUpdateTime(currentTime);
            }

            ViewSupport.updateChildren(subViews.getSubviews(), newDataToPost, null);
        }
        else
        {

            // Algorithm for dispatching multiple events
            if (newData != null)
            {
                for (EventBean newValue : newData)
                {
                    handleEvent(newValue, true);
                }
            }

            if (oldData != null)
            {
                for (EventBean oldValue : oldData)
                {
                    handleEvent(oldValue, false);
                }
            }

            // Update child views
            for (Map.Entry<GroupByViewAgedEntry, Pair<List<EventBean>, List<EventBean>>> entry : groupedEvents.entrySet())
            {
                EventBean[] newEvents = EventBeanUtility.toArray(entry.getValue().getFirst());
                EventBean[] oldEvents = EventBeanUtility.toArray(entry.getValue().getSecond());
                ViewSupport.updateChildren(entry.getKey().getSubviews(), newEvents, oldEvents);
            }

            groupedEvents.clear();
        }
    }

    private void handleEvent(EventBean event, boolean isNew)
    {
        // Get values for group-by, construct MultiKey
        Object[] groupByValues = new Object[criteriaExpressions.length];
        eventsPerStream[0] = event;
        for (int i = 0; i < criteriaEvaluators.length; i++)
        {
            groupByValues[i] = criteriaEvaluators[i].evaluate(eventsPerStream, true, statementContext);
        }
        MultiKey<Object> groupByValuesKey = new MultiKey<Object>(groupByValues);

        // Get child views that belong to this group-by value combination
        GroupByViewAgedEntry subViews = this.subViewsPerKey.get(groupByValuesKey);

        // If this is a new group-by value, the list of subviews is null and we need to make clone sub-views
        if (subViews == null)
        {
            List<View> subviewsList = GroupByViewImpl.makeSubViews(this, propertyNames, groupByValuesKey.getArray(), statementContext);
            long currentTime = statementContext.getTimeProvider().getTime();
            subViews = new GroupByViewAgedEntry(subviewsList, currentTime);
            subViewsPerKey.put(groupByValuesKey, subViews);
        }
        else {
            subViews.setLastUpdateTime(statementContext.getTimeProvider().getTime());
        }

        // Construct a pair of lists to hold the events for the grouped value if not already there
        Pair<List<EventBean>, List<EventBean>> pair = groupedEvents.get(subViews);
        if (pair == null)
        {
            LinkedList<EventBean> listNew = new LinkedList<EventBean>();
            LinkedList<EventBean> listOld = new LinkedList<EventBean>();
            pair = new Pair<List<EventBean>, List<EventBean>>(listNew, listOld);
            groupedEvents.put(subViews, pair);
        }

        // Add event to a child view event list for later child update that includes new and old events
        if (isNew)
        {
            pair.getFirst().add(event);
        }
        else
        {
            pair.getSecond().add(event);
        }
    }

    public final Iterator<EventBean> iterator()
    {
        throw new UnsupportedOperationException("Cannot iterate over group view, this operation is not supported");
    }

    public final String toString()
    {
        return this.getClass().getName() + " groupFieldNames=" + Arrays.toString(criteriaExpressions);
    }

    private void sweep(long currentTime)
    {
        ArrayDeque<MultiKey<Object>> removed = new ArrayDeque<MultiKey<Object>>();
        for (Map.Entry<MultiKey<Object>, GroupByViewAgedEntry> entry : subViewsPerKey.entrySet())
        {
            long age = currentTime - entry.getValue().getLastUpdateTime();
            if (age > reclaimMaxAge)
            {
                removed.add(entry.getKey());
            }
        }

        for (MultiKey<Object> key : removed)
        {
            GroupByViewAgedEntry entry = subViewsPerKey.remove(key);
            for (View view : entry.getSubviews()) {
                view.setParent(null);
                recursiveMergeViewRemove(view);
                if (view instanceof StoppableView) {
                    ((StoppableView) view).stop();
                }
            }
        }
    }

    private void recursiveMergeViewRemove(View view)
    {
        for (View child : view.getViews()) {
            if (child instanceof StoppableView) {
                ((StoppableView) child).stop();
            }
            if (child instanceof MergeView) {
                MergeView mergeView = (MergeView) child;
                mergeView.removeParentView(view);
            }
            else {
                recursiveMergeViewRemove(child);
            }
        }
    }

    private static final Log log = LogFactory.getLog(GroupByViewReclaimAged.class);
}
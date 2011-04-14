/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.std;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * The group view splits the data in a stream to multiple subviews, based on a key index.
 * The key is one or more fields in the stream. Any view that follows the GROUP view will be executed
 * separately on each subview, one per unique key.
 *
 * The view takes a single parameter which is the field name returning the key value to group.
 *
 * This view can, for example, be used to calculate the average price per symbol for a list of symbols.
 *
 * The view treats its child views and their child views as prototypes. It dynamically instantiates copies
 * of each child view and their child views, and the child view's child views as so on. When there are
 * no more child views or the special merge view is encountered, it ends. The view installs a special merge
 * view unto each leaf child view that merges the value key that was grouped by back into the stream
 * using the group-by field name.
 */
public final class GroupByViewImpl extends ViewSupport implements CloneableView, GroupByView
{
    private final ExprNode[] criteriaExpressions;
    private final ExprEvaluator[] criteriaEvaluators;
    private final StatementContext statementContext;
    private EventBean[] eventsPerStream = new EventBean[1];

    private String[] propertyNames;
    private final Map<MultiKey<Object>, List<View>> subViewsPerKey = new HashMap<MultiKey<Object>, List<View>>();

    private final HashMap<List<View>, Pair<List<EventBean>, List<EventBean>>> groupedEvents = new HashMap<List<View>, Pair<List<EventBean>, List<EventBean>>>();

    /**
     * Constructor.
     * @param criteriaExpressions is the fields from which to pull the values to group by
     * @param statementContext contains required view services
     */
    public GroupByViewImpl(StatementContext statementContext, ExprNode[] criteriaExpressions, ExprEvaluator[] criteriaEvaluators)
    {
        this.statementContext = statementContext;
        this.criteriaExpressions = criteriaExpressions;
        this.criteriaEvaluators = criteriaEvaluators;

        propertyNames = new String[criteriaExpressions.length];
        for (int i = 0; i < criteriaExpressions.length; i++)
        {
            propertyNames[i] = criteriaExpressions[i].toExpressionString();
        }
    }

    public View cloneView(StatementContext statementContext)
    {
        return new GroupByViewImpl(statementContext, criteriaExpressions, criteriaEvaluators);
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
            List<View> subViews = this.subViewsPerKey.get(groupByValuesKey);

            // If this is a new group-by value, the list of subviews is null and we need to make clone sub-views
            if (subViews == null)
            {
                subViews = makeSubViews(this, propertyNames, groupByValuesKey.getArray(), statementContext);
                subViewsPerKey.put(groupByValuesKey, subViews);
            }

            ViewSupport.updateChildren(subViews, newDataToPost, null);
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
            for (Map.Entry<List<View>, Pair<List<EventBean>, List<EventBean>>> entry : groupedEvents.entrySet())
            {
                EventBean[] newEvents = EventBeanUtility.toArray(entry.getValue().getFirst());
                EventBean[] oldEvents = EventBeanUtility.toArray(entry.getValue().getSecond());
                ViewSupport.updateChildren(entry.getKey(), newEvents, oldEvents);
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
        List<View> subViews = this.subViewsPerKey.get(groupByValuesKey);

        // If this is a new group-by value, the list of subviews is null and we need to make clone sub-views
        if (subViews == null)
        {
            subViews = makeSubViews(this, propertyNames, groupByValuesKey.getArray(), statementContext);
            subViewsPerKey.put(groupByValuesKey, subViews);
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

    /**
     * Instantiate subviews for the given group view and the given key value to group-by.
     * Makes shallow copies of each child view and its subviews up to the merge point.
     * Sets up merge data views for merging the group-by key value back in.
     * @param groupView is the parent view for which to copy subviews for
     * @param groupByValues is the key values to group-by
     * @param statementContext is the view services that sub-views may need
     * @param propertyNames names of expressions or properties
     * @return a list of views that are copies of the original list, with copied children, with
     * data merge views added to the copied child leaf views.
     */
    protected static List<View> makeSubViews(GroupByView groupView, String[] propertyNames, Object[] groupByValues,
                                             StatementContext statementContext)
    {
        if (!groupView.hasViews())
        {
            String message = "Unexpected empty list of child nodes for group view";
            log.fatal(".copySubViews " + message);
            throw new EPException(message);
        }

        LinkedList<View> subViewList = new LinkedList<View>();

        // For each child node
        for (View originalChildView : groupView.getViews())
        {
            if (originalChildView instanceof MergeView)
            {
                String message = "Unexpected merge view as child of group-by view";
                log.fatal(".copySubViews " + message);
                throw new EPException(message);
            }

            if (!(originalChildView instanceof CloneableView))
            {
                throw new EPException("Unexpected error copying subview " + originalChildView.getClass().getName());
            }
            CloneableView cloneableView = (CloneableView) originalChildView;

            // Copy child node
            View copyChildView = cloneableView.cloneView(statementContext);
            copyChildView.setParent(groupView);
            subViewList.add(copyChildView);

            // Make the sub views for child copying from the original to the child
            copySubViews(groupView.getCriteriaExpressions(), propertyNames, groupByValues, originalChildView, copyChildView,
                    statementContext);
        }

        return subViewList;
    }

    private static void copySubViews(ExprNode[] criteriaExpressions, String[] propertyNames, Object[] groupByValues, View originalView, View copyView,
                                     StatementContext statementContext)
    {
        for (View subView : originalView.getViews())
        {
            // Determine if view is our merge view
            if (subView instanceof MergeView)
            {
                MergeView mergeView = (MergeView) subView;
                if (ExprNodeUtility.deepEquals(mergeView.getGroupFieldNames(), criteriaExpressions))
                {
                    // We found our merge view - install a new data merge view on top of it
                    AddPropertyValueView mergeDataView = new AddPropertyValueView(statementContext, propertyNames, groupByValues, mergeView.getEventType());

                    // Add to the copied parent subview the view merge data view
                    copyView.addView(mergeDataView);

                    // Add to the new merge data view the actual single merge view instance that clients may attached to
                    mergeDataView.addView(mergeView);

                    // Add a parent view to the single merge view instance
                    mergeView.addParentView(mergeDataView);

                    continue;
                }
            }

            if (!(subView instanceof CloneableView))
            {
                throw new EPException("Unexpected error copying subview");
            }
            CloneableView cloneableView = (CloneableView) subView;
            View copiedChild = cloneableView.cloneView(statementContext);
            copyView.addView(copiedChild);

            // Make the sub views for child
            copySubViews(criteriaExpressions, propertyNames, groupByValues, subView, copiedChild, statementContext);
        }
    }

    private static final Log log = LogFactory.getLog(GroupByViewImpl.class);
}
/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.ext;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.util.MultiKeyCollatingComparator;
import com.espertech.esper.util.MultiKeyComparator;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.DataWindowView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Window sorting by values in the specified field extending a specified number of elements
 * from the lowest value up or the highest value down.
 * The view accepts 3 parameters. The first parameter is the field name to get the values to sort for,
 * the second parameter defines whether to sort ascending or descending, the third parameter
 * is the number of elements to keep in the sorted list.
 *
 * The type of the field to be sorted in the event must implement the Comparable interface.
 *
 * The natural order in which events arrived is used as the second sorting criteria. Thus should events arrive
 * with equal sort values the oldest event leaves the sort window first.
 *
 * Old values removed from a prior view are removed from the sort view.
 */
public final class SortWindowView extends ViewSupport implements DataWindowView, CloneableView
{
    private final SortWindowViewFactory sortWindowViewFactory;
    private final ExprEvaluator[] sortCriteriaEvaluators;
    private final ExprNode[] sortCriteriaExpressions;
    private final EventBean[] eventsPerStream = new EventBean[1];
    private final boolean[] isDescendingValues;
    private final int sortWindowSize;
    private final IStreamSortedRandomAccess optionalSortedRandomAccess;
    private final ExprEvaluatorContext exprEvaluatorContext;

    private TreeMap<MultiKeyUntyped, LinkedList<EventBean>> sortedEvents;
    private int eventCount;

    /**
     * Ctor.
     * @param sortCriteriaExpressions is the event property names to sort
     * @param descendingValues indicates whether to sort ascending or descending for each field
     * @param sortWindowSize is the window size
     * @param optionalSortedRandomAccess is the friend class handling the random access, if required by
     * expressions
     * @param sortWindowViewFactory for copying this view in a group-by
     * @param isSortUsingCollator for string value sorting using compare or Collator
     * @param exprEvaluatorContext context for expression evalauation
     */
    public SortWindowView(SortWindowViewFactory sortWindowViewFactory,
                          ExprNode[] sortCriteriaExpressions,
                          ExprEvaluator[] sortCriteriaEvaluators,
                          boolean[] descendingValues,
                          int sortWindowSize,
                          IStreamSortedRandomAccess optionalSortedRandomAccess,
                          boolean isSortUsingCollator,
                          ExprEvaluatorContext exprEvaluatorContext)
    {
        this.sortWindowViewFactory = sortWindowViewFactory;
        this.sortCriteriaExpressions = sortCriteriaExpressions;
        this.sortCriteriaEvaluators = sortCriteriaEvaluators;
        this.isDescendingValues = descendingValues;
        this.sortWindowSize = sortWindowSize;
        this.optionalSortedRandomAccess = optionalSortedRandomAccess;
        this.exprEvaluatorContext = exprEvaluatorContext;

        // determine string-type sorting
        boolean hasStringTypes = false;
        boolean stringTypes[] = new boolean[sortCriteriaExpressions.length];

        int count = 0;
        for(ExprEvaluator node : sortCriteriaEvaluators)
        {
            if (node.getType() == String.class)
            {
                hasStringTypes = true;
                stringTypes[count] = true;
            }
            count++;
        }

        Comparator<MultiKeyUntyped> comparator;
        if ((!hasStringTypes) || (!isSortUsingCollator))
        {
            comparator = new MultiKeyComparator(isDescendingValues);
        }
        else
        {
            comparator = new MultiKeyCollatingComparator(isDescendingValues, stringTypes);
        }
        sortedEvents = new TreeMap<MultiKeyUntyped, LinkedList<EventBean>>(comparator);
    }

    /**
     * Returns the field names supplying the values to sort by.
     * @return field names to sort by
     */
    protected final ExprNode[] getSortCriteriaExpressions()
    {
        return sortCriteriaExpressions;
    }

    /**
     * Returns the flags indicating whether to sort in descending order on each property.
     * @return the isDescending value for each sort property
     */
    protected final boolean[] getIsDescendingValues()
    {
    	return isDescendingValues;
    }

    /**
     * Returns the number of elements kept by the sort window.
     * @return size of window
     */
    protected final int getSortWindowSize()
    {
        return sortWindowSize;
    }

    /**
     * Returns the friend handling the random access, cal be null if not required.
     * @return random accessor to sort window contents
     */
    protected IStreamSortedRandomAccess getOptionalSortedRandomAccess()
    {
        return optionalSortedRandomAccess;
    }

    public View cloneView(StatementContext statementContext)
    {
        return sortWindowViewFactory.makeView(statementContext);
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
            dumpUpdateParams("SortWindowView", newData, oldData);
        }

        List<EventBean> removedEvents = new LinkedList<EventBean>();

        // Remove old data
        if (oldData != null)
        {
            for (int i = 0; i < oldData.length; i++)
            {
                MultiKeyUntyped sortValues = getSortValues(oldData[i]);
                boolean result = remove(sortValues, oldData[i]);
                if (result)
                {
                    eventCount--;
                    removedEvents.add(oldData[i]);
                }
            }
        }

        // Add new data
        if (newData != null)
        {
            for (int i = 0; i < newData.length; i++)
            {
                MultiKeyUntyped sortValues = getSortValues(newData[i]);
                add(sortValues, newData[i]);
                eventCount++;
            }
        }

        // Remove data that sorts to the bottom of the window
        if (eventCount > sortWindowSize)
        {
            int removeCount = eventCount - sortWindowSize;
            for (int i = 0; i < removeCount; i++)
            {
                // Remove the last element of the last key - sort order is key and then natural order of arrival
                MultiKeyUntyped lastKey = sortedEvents.lastKey();
                LinkedList<EventBean> events = sortedEvents.get(lastKey);
                EventBean event = events.removeLast();
                eventCount--;

                // Clear out entry if not used
                if (events.isEmpty())
                {
                    sortedEvents.remove(lastKey);
                }

                removedEvents.add(event);

                if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
                {
                    log.debug(".update Pushing out event event=" + event);
                }
            }
        }

        // If there are child views, fireStatementStopped update method
        if (optionalSortedRandomAccess != null)
        {
            optionalSortedRandomAccess.refresh(sortedEvents, eventCount, sortWindowSize);
        }
        if (this.hasViews())
        {
            EventBean[] expiredArr = null;
            if (!removedEvents.isEmpty())
            {
                expiredArr = removedEvents.toArray(new EventBean[removedEvents.size()]);
            }

            updateChildren(newData, expiredArr);
        }
    }

    public final Iterator<EventBean> iterator()
    {
        return new SortWindowIterator(sortedEvents);
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " sortFieldName=" + Arrays.toString(sortCriteriaExpressions) +
                " isDescending=" + Arrays.toString(isDescendingValues) +
                " sortWindowSize=" + sortWindowSize;
    }

    private void add(MultiKeyUntyped key, EventBean bean)
    {
        LinkedList<EventBean> listOfBeans = sortedEvents.get(key);
        if (listOfBeans != null)
        {
            listOfBeans.addFirst(bean); // Add to the front of the list as the second sort critertial is ascending arrival order
            return;
        }

        listOfBeans = new LinkedList<EventBean>();
        listOfBeans.add(bean);
        sortedEvents.put(key, listOfBeans);
    }

    private boolean remove(MultiKeyUntyped key, EventBean bean)
    {
        LinkedList<EventBean> listOfBeans = sortedEvents.get(key);
        if (listOfBeans == null)
        {
            return false;
        }

        boolean result = listOfBeans.remove(bean);
        if (listOfBeans.isEmpty())
        {
            sortedEvents.remove(key);
        }
        return result;
    }

    private MultiKeyUntyped getSortValues(EventBean event)
    {
        eventsPerStream[0] = event;
    	Object[] result = new Object[sortCriteriaExpressions.length];
    	int count = 0;
    	for(ExprEvaluator expr : sortCriteriaEvaluators)
    	{
            result[count++] = expr.evaluate(eventsPerStream, true, exprEvaluatorContext);
    	}
    	return new MultiKeyUntyped(result);
    }

    /**
     * True to indicate the sort window is empty, or false if not empty.
     * @return true if empty sort window
     */
    public boolean isEmpty()
    {
        if (sortedEvents == null)
        {
            return true;
        }
        return sortedEvents.isEmpty();
    }

    private static final Log log = LogFactory.getLog(SortWindowView.class);
}

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
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Window retaining timestamped events up to a given number of seconds such that
 * older events that arrive later are sorted into the window and released in timestamp order.
 * <p>
 * The insert stream consists of all arriving events. The remove stream consists of events in
 * order of timestamp value as supplied by each event.
 * <p>
 * Timestamp values on events should match engine time. The window compares engine time to timestamp value
 * and releases events when the event's timestamp is less then engine time minus interval size (the
 * event is older then the window tail).
 * <p>
 * The view accepts 2 parameters. The first parameter is the field name to get the event timestamp value from,
 * the second parameter defines the interval size.
 */
public final class TimeOrderView extends ViewSupport implements DataWindowView, CloneableView, StoppableView
{
    private final StatementContext statementContext;
    private final TimeOrderViewFactory timeOrderViewFactory;
    private final ExprNode timestampExpression;
    private final ExprEvaluator timestampEvaluator;
    private final long intervalSize;
    private final IStreamTimeOrderRandomAccess optionalSortedRandomAccess;
    private final ScheduleSlot scheduleSlot;
    private final EPStatementHandleCallback handle;

    private EventBean[] eventsPerStream = new EventBean[1];
    private TreeMap<Long, ArrayList<EventBean>> sortedEvents;
    private boolean isCallbackScheduled;
    private int eventCount;
    private Map<EventBean, ArrayList<EventBean>> reverseIndex;

    /**
     * Ctor.
     * @param optionalSortedRandomAccess is the friend class handling the random access, if required by
     * expressions
     * @param timeOrderViewFactory for copying this view in a group-by
     * @param statementContext the statement context
     * @param timestampExpr the property name of the event supplying timestamp values
     * @param intervalSize the interval time length
     * @param isRemoveStreamHandling if the view must handle the remove stream of parent views
     */
    public TimeOrderView( StatementContext statementContext,
                          TimeOrderViewFactory timeOrderViewFactory,
                          ExprNode timestampExpr,
                          ExprEvaluator timestampEvaluator,
                          long intervalSize,
                          IStreamTimeOrderRandomAccess optionalSortedRandomAccess,
                          boolean isRemoveStreamHandling)
    {
        this.statementContext = statementContext;
        this.timeOrderViewFactory = timeOrderViewFactory;
        this.timestampExpression = timestampExpr;
        this.timestampEvaluator = timestampEvaluator;
        this.intervalSize = intervalSize;
        this.optionalSortedRandomAccess = optionalSortedRandomAccess;
        this.scheduleSlot = statementContext.getScheduleBucket().allocateSlot();
        if (isRemoveStreamHandling)
        {
            reverseIndex = new HashMap<EventBean, ArrayList<EventBean>>();
        }

        sortedEvents = new TreeMap<Long, ArrayList<EventBean>>();

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                TimeOrderView.this.expire();
            }
        };
        handle = new EPStatementHandleCallback(statementContext.getEpStatementHandle(), callback);
    }

    /**
     * Returns the timestamp property name.
     * @return property name supplying timestamp values
     */
    public ExprNode getTimestampExpression()
    {
        return timestampExpression;
    }

    /**
     * Returns the time interval size.
     * @return interval size
     */
    public long getIntervalSize()
    {
        return intervalSize;
    }

    /**
     * Returns the friend handling the random access, cal be null if not required.
     * @return random accessor to sort window contents
     */
    protected IStreamTimeOrderRandomAccess getOptionalSortedRandomAccess()
    {
        return optionalSortedRandomAccess;
    }

    public View cloneView(StatementContext statementContext)
    {
        return timeOrderViewFactory.makeView(statementContext);
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
            dumpUpdateParams("TimeOrderView", newData, oldData);
        }

        EventBean[] postOldEventsArray = null;

        if ((newData != null) && (newData.length > 0))
        {

            // figure out the current tail time
            long engineTime = statementContext.getSchedulingService().getTime();
            long windowTailTime = engineTime - intervalSize + 1;
            long oldestEvent = Long.MAX_VALUE;
            if (!sortedEvents.isEmpty())
            {
                oldestEvent = sortedEvents.firstKey();
            }
            boolean addedOlderEvent = false;

            // add events or post events as remove stream if already older then tail time
            ArrayList<EventBean> postOldEvents = null;
            for (int i = 0; i < newData.length; i++)
            {
                // get timestamp of event
                EventBean newEvent = newData[i];
                eventsPerStream[0] = newEvent;
                Long timestamp = (Long) timestampEvaluator.evaluate(eventsPerStream, true, statementContext);

                // if the event timestamp indicates its older then the tail of the window, release it
                if (timestamp < windowTailTime)
                {
                    if (postOldEvents == null)
                    {
                        postOldEvents = new ArrayList<EventBean>();
                    }
                    postOldEvents.add(newEvent);
                }
                else
                {
                    if (timestamp < oldestEvent)
                    {
                        addedOlderEvent = true;
                        oldestEvent = timestamp;
                    }

                    // add to list
                    ArrayList<EventBean> listOfBeans = sortedEvents.get(timestamp);
                    if (listOfBeans != null)
                    {
                        listOfBeans.add(newEvent);
                    }
                    else
                    {
                        listOfBeans = new ArrayList<EventBean>();
                        listOfBeans.add(newEvent);
                        sortedEvents.put(timestamp, listOfBeans);
                    }

                    if (reverseIndex != null)
                    {
                        reverseIndex.put(newEvent, listOfBeans);
                    }
                    eventCount++;
                }
            }

            // If we do have data, check the callback
            if (!sortedEvents.isEmpty())
            {
                // If we haven't scheduled a callback yet, schedule it now
                if (!isCallbackScheduled)
                {
                    long callbackWait = oldestEvent - windowTailTime + 1;
                    statementContext.getSchedulingService().add(callbackWait, handle, scheduleSlot);
                    isCallbackScheduled = true;
                }
                else
                {
                    // We may need to reschedule, and older event may have been added
                    if (addedOlderEvent)
                    {
                        oldestEvent = sortedEvents.firstKey();
                        long callbackWait = oldestEvent - windowTailTime + 1;
                        statementContext.getSchedulingService().remove(handle, scheduleSlot);
                        statementContext.getSchedulingService().add(callbackWait, handle, scheduleSlot);
                        isCallbackScheduled = true;
                    }
                }
            }

            if (postOldEvents != null)
            {
                postOldEventsArray = postOldEvents.toArray(new EventBean[postOldEvents.size()]);
            }

            if (optionalSortedRandomAccess != null)
            {
                optionalSortedRandomAccess.refresh(sortedEvents, eventCount);
            }
        }

        if ((oldData != null) && (reverseIndex != null))
        {
            for (EventBean old : oldData)
            {
                ArrayList<EventBean> list = reverseIndex.remove(old);
                if (list != null)
                {
                    list.remove(old);
                }
            }

            if (postOldEventsArray == null)
            {
                postOldEventsArray = oldData;
            }
            else
            {
                Set<EventBean> oldDataSet = new HashSet<EventBean>();
                oldDataSet.addAll(Arrays.asList(postOldEventsArray));
                oldDataSet.addAll(Arrays.asList(oldData));
                postOldEventsArray = oldDataSet.toArray(new EventBean[oldDataSet.size()]);
            }
        }

        // update child views
        if (this.hasViews())
        {
            updateChildren(newData, postOldEventsArray);
        }
    }

    /**
     * True to indicate the sort window is empty, or false if not empty.
     * @return true if empty sort window
     */
    public boolean isEmpty()
    {
        return sortedEvents.isEmpty();
    }

    public final Iterator<EventBean> iterator()
    {
        return new TimeOrderViewIterator(sortedEvents);
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " timestampExpression=" + timestampExpression +
                " intervalSize=" + intervalSize;
    }

    /**
     * This method removes (expires) objects from the window and schedules a new callback for the
     * time when the next oldest message would expire from the window.
     */
    protected final void expire()
    {
        long expireBeforeTimestamp = statementContext.getSchedulingService().getTime() - intervalSize + 1;
        isCallbackScheduled = false;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".expire Expiring messages before " +
                    "msec=" + expireBeforeTimestamp +
                    "  date=" + statementContext.getSchedulingService().getTime());
        }

        ArrayList<EventBean> releaseEvents = null;
        Long oldestKey;
        while(true)
        {
            if (sortedEvents.isEmpty())
            {
                oldestKey = null;
                break;
            }

            oldestKey = sortedEvents.firstKey();
            if (oldestKey >= expireBeforeTimestamp)
            {
                break;
            }

            ArrayList<EventBean> expireEvents = sortedEvents.get(oldestKey);
            if (releaseEvents == null)
            {
                releaseEvents = expireEvents;
            }
            else
            {
                releaseEvents.addAll(expireEvents);
            }
            eventCount -= expireEvents.size();
            sortedEvents.remove(oldestKey);

            if (reverseIndex != null)
            {
                for (EventBean released : releaseEvents)
                {
                    reverseIndex.remove(released);
                }
            }
        }

        if (optionalSortedRandomAccess != null)
        {
            optionalSortedRandomAccess.refresh(sortedEvents, eventCount);
        }

        // If there are child views, do the update method
        if (this.hasViews())
        {
            if ((releaseEvents != null) && (!releaseEvents.isEmpty()))
            {
                EventBean[] oldEvents = releaseEvents.toArray(new EventBean[releaseEvents.size()]);
                updateChildren(null, oldEvents);
            }
        }

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".expire Expired messages....size=" + releaseEvents.size());
            for (Object object : releaseEvents)
            {
                log.debug(".expire object=" + object);
            }
        }

        // If we still have events in the window, schedule new callback
        if (oldestKey == null)
        {
            return;
        }

        // Next callback
        long callbackWait = oldestKey - expireBeforeTimestamp + 1;
        statementContext.getSchedulingService().add(callbackWait, handle, scheduleSlot);
        isCallbackScheduled = true;

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".expire Scheduled new callback for now plus msec=" + callbackWait);
        }
    }

    public void stop() {
    	if (handle != null) {
        	statementContext.getSchedulingService().remove(handle, scheduleSlot);
        }
    }

    private static final Log log = LogFactory.getLog(TimeOrderView.class);
}

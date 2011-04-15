package com.espertech.esper.view.internal;

import com.espertech.esper.collection.RefCountedSet;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.StoppableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.core.StatementContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A view that represents a union of multiple data windows.
 * <p>
 * The view is parameterized by two or more data windows. From an external viewpoint, the
 * view retains all events that is in any of the data windows (a union).
 */
public class UnionView extends ViewSupport implements LastPostObserver, CloneableView, StoppableView
{
    private static final Log log = LogFactory.getLog(UnionView.class);

    private final UnionViewFactory unionViewFactory;
    private final EventType eventType;
    private final View[] views;
    private final EventBean[][] oldEventsPerView;
    private final RefCountedSet<EventBean> unionWindow;
    private final List<EventBean> removalEvents = new ArrayList<EventBean>();

    private boolean isHasRemovestreamData;
    private boolean isRetainObserverEvents;
    private boolean isDiscardObserverEvents;

    /**
     * Ctor.
     * @param factory the view factory
     * @param eventType the parent event type
     * @param viewList the list of data window views
     */
    public UnionView(UnionViewFactory factory, EventType eventType, List<View> viewList)
    {
        this.unionViewFactory = factory;
        this.eventType = eventType;
        this.views = viewList.toArray(new View[viewList.size()]);
        this.unionWindow = new RefCountedSet<EventBean>();
        oldEventsPerView = new EventBean[viewList.size()][];
        
        for (int i = 0; i < viewList.size(); i++)
        {
            LastPostObserverView view = new LastPostObserverView(i);
            views[i].removeAllViews();
            views[i].addView(view);
            view.setObserver(this);
        }

        // recover
        for (int i = 0; i < views.length; i++)
        {
            Iterator<EventBean> viewSnapshot = views[i].iterator();
            for (;viewSnapshot.hasNext();)
            {
                EventBean event = viewSnapshot.next();
                unionWindow.add(event);
            }
        }
    }

    public View cloneView(StatementContext statementContext)
    {
        return unionViewFactory.makeView(statementContext);
    }

    public void update(EventBean[] newData, EventBean[] oldData)
    {
        // The assumption is that either remove stream (named window deletes) or insert stream (new events) are indicated
        if ((newData != null) && (oldData != null))
        {
            log.error("Union view received both insert and remove stream");
        }
        
        // add new event to union
        if (newData != null)
        {
            for (EventBean newEvent : newData)
            {
                unionWindow.add(newEvent, views.length);
            }

            // new events must go to all views
            // old events, such as when removing from a named window, get removed from all views
            isHasRemovestreamData = false;  // changed by observer logic to indicate new data
            isRetainObserverEvents = true;  // enable retain logic in observer
            try
            {
                for (View view : views)
                {
                    view.update(newData, oldData);
                }
            }
            finally
            {
                isRetainObserverEvents = false;                
            }

            // see if any child view has removed any events.
            // if there was an insert stream, handle pushed-out events
            if (isHasRemovestreamData)
            {
                List<EventBean> removedEvents = null;

                // process each buffer
                for (int i = 0; i < oldEventsPerView.length; i++)
                {
                    if (oldEventsPerView[i] == null)
                    {
                        continue;
                    }

                    EventBean[] viewOldData = oldEventsPerView[i];
                    oldEventsPerView[i]= null;  // clear entry

                    // remove events for union, if the last event was removed then add it
                    for (EventBean old : viewOldData)
                    {
                        boolean isNoMoreRef = unionWindow.remove(old);
                        if (isNoMoreRef)
                        {
                            if (removedEvents == null)
                            {
                                removalEvents.clear();
                                removedEvents = removalEvents;
                            }
                            removedEvents.add(old);
                        }
                    }
                }

                if (removedEvents != null)
                {
                    oldData = removedEvents.toArray(new EventBean[removedEvents.size()]);
                }
            }

            // indicate new and, possibly, old data
            updateChildren(newData, oldData);
        }

        // handle remove stream
        else if (oldData != null)
        {
            isDiscardObserverEvents = true;    // disable reaction logic in observer

            try
            {
                for (View view : views)
                {
                    view.update(null, oldData);
                }
            }
            finally
            {
                isDiscardObserverEvents = false;
            }

            // remove from union
            for (EventBean oldEvent : oldData)
            {
                unionWindow.removeAll(oldEvent);
            }

            updateChildren(null, oldData);
        }
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public Iterator<EventBean> iterator()
    {
        return unionWindow.keyIterator();
    }

    public void newData(int streamId, EventBean[] newEvents, EventBean[] oldEvents)
    {
        if ((oldEvents == null) || (isDiscardObserverEvents))
        {
            return;
        }

        if (isRetainObserverEvents)
        {
            oldEventsPerView[streamId] = oldEvents;
            isHasRemovestreamData = true;
            return;
        }

        // handle time-based removal
        List<EventBean> removedEvents = null;

        // remove events for union, if the last event was removed then add it
        for (EventBean old : oldEvents)
        {
            boolean isNoMoreRef = unionWindow.remove(old);
            if (isNoMoreRef)
            {
                if (removedEvents == null)
                {
                    removalEvents.clear();
                    removedEvents = removalEvents;
                }
                removedEvents.add(old);
            }
        }

        if (removedEvents != null)
        {
            EventBean[] removed = removedEvents.toArray(new EventBean[removedEvents.size()]);
            updateChildren(null, removed);
        }
    }

    @Override
    public void stop() {
        for (View view : views) {
            if (view instanceof StoppableView) {
                ((StoppableView) view).stop();
            }
        }
    }    
}

package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.event.EventBeanCopyMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Interface for a service that routes events within the engine for further processing.
 */
public class InternalEventRouterPreprocessor
{
    private static final Log log = LogFactory.getLog(InternalEventRouterPreprocessor.class);
    private static final Comparator<InternalEventRouterEntry> comparator = new Comparator<InternalEventRouterEntry>()
    {
        public int compare(InternalEventRouterEntry o1, InternalEventRouterEntry o2)
        {
            if (o1.getPriority() > o2.getPriority())
            {
                return 1;
            }
            else if (o1.getPriority() < o2.getPriority())
            {
                return -1;
            }
            else if (o1.isDrop())
            {
                return -1;
            }
            else if (o2.isDrop())
            {
                return -1;
            }
            return 0;
        }
    };

    private final EventBeanCopyMethod copyMethod;
    private final InternalEventRouterEntry[] entries;
    private final boolean empty;

    /**
     * Ctor.
     * @param copyMethod for copying the events to preprocess
     * @param entries descriptors for pre-processing to apply
     */
    public InternalEventRouterPreprocessor(EventBeanCopyMethod copyMethod, List<InternalEventRouterEntry> entries)
    {
        this.copyMethod = copyMethod;
        Collections.sort(entries, comparator);
        this.entries = entries.toArray(new InternalEventRouterEntry[entries.size()]);
        empty = this.entries.length == 0;
    }

    /**
     * Pre-proces the event.
     * @param event to pre-process
     * @param exprEvaluatorContext expression evaluation context
     * @return processed event
     */
    public EventBean process(EventBean event, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (empty)
        {
            return event;
        }

        EventBean oldEvent = event;
        boolean haveCloned = false;
        EventBean[] eventsPerStream = new EventBean[1];
        eventsPerStream[0] = event;
        InternalEventRouterEntry lastEntry = null;

        for (int i = 0; i < entries.length; i++)
        {
            InternalEventRouterEntry entry = entries[i];
            ExprEvaluator whereClause = entry.getOptionalWhereClause();
            if (whereClause != null)
            {
                Boolean result = (Boolean) whereClause.evaluate(eventsPerStream, true, exprEvaluatorContext);
                if ((result == null) || (!result))
                {
                    continue;
                }
            }

            if (entry.isDrop())
            {
                return null;
            }

            // before applying the changes, indicate to last-entries output view
            if (lastEntry != null)
            {
                InternalRoutePreprocessView view = lastEntry.getOutputView();
                if (view.isIndicate())
                {
                    EventBean copied = copyMethod.copy(event);
                    view.indicate(copied, oldEvent);
                    oldEvent = copied;
                }
                else
                {
                    if (entries[i].getOutputView().isIndicate())
                    {
                        oldEvent = copyMethod.copy(event);
                    }
                }
            }

            // copy event for the first update that applies
            if (!haveCloned)
            {
                EventBean copiedEvent = copyMethod.copy(event);
                if (copiedEvent == null)
                {
                    log.warn("Event of type " + event.getEventType().getName() + " could not be copied");
                    return null;
                }
                haveCloned = true;
                eventsPerStream[0] = copiedEvent;
                event = copiedEvent;
            }
            
            apply(event, eventsPerStream, entry, exprEvaluatorContext);
            lastEntry = entry;
        }
        
        if (lastEntry != null)
        {
            InternalRoutePreprocessView view = lastEntry.getOutputView();
            if (view.isIndicate())
            {
                view.indicate(event, oldEvent);
            }
        }

        return event;
    }

    private void apply(EventBean event, EventBean[] eventsPerStream, InternalEventRouterEntry entry, ExprEvaluatorContext exprEvaluatorContext)
    {
        // evaluate
        Object[] values = new Object[entry.getAssignments().length];
        for (int i = 0; i < entry.getAssignments().length; i++)
        {
            Object value = entry.getAssignments()[i].evaluate(eventsPerStream, true, exprEvaluatorContext);

            if ((value != null) && (entry.getWideners()[i] != null))
            {
                value = entry.getWideners()[i].widen(value);
            }

            values[i] = value;
        }

        // apply
        entry.getWriter().write(values, event);
    }
}

package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.event.EventBeanCopyMethod;
import com.espertech.esper.event.NaturalEventBean;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * View for use with pre-processing statement such as "update istream" for indicating previous and current event.
 */
public class InternalRoutePreprocessView extends ViewSupport
{
    private static final Log log = LogFactory.getLog(InternalRoutePreprocessView.class);
    private final EventType eventType;
    private final Iterator<EventBean> noiter = new SingleEventIterator(null);
    private final StatementResultService statementResultService;

    /**
     * Ctor.
     * @param eventType the type of event to indicator
     * @param statementResultService determines whether listeners or subscribers are attached.
     */
    public InternalRoutePreprocessView(EventType eventType, StatementResultService statementResultService)
    {
        this.eventType = eventType;
        this.statementResultService =  statementResultService;
    }

    public void update(EventBean[] newData, EventBean[] oldData)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".update Received update, " +
                    "  newData.length==" + ((newData == null) ? 0 : newData.length) +
                    "  oldData.length==" + ((oldData == null) ? 0 : oldData.length));
        }
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public Iterator<EventBean> iterator()
    {
        return noiter;
    }

    /**
     * Returns true if a subscriber or listener is attached.
     * @return indicator
     */
    public boolean isIndicate()
    {
        return (statementResultService.isMakeNatural() || statementResultService.isMakeSynthetic());
    }

    /**
     * Indicate an modifed event and its previous version.
     * @param newEvent modified event
     * @param oldEvent previous version event
     */
    public void indicate(EventBean newEvent, EventBean oldEvent)
    {
        try
        {
            if (statementResultService.isMakeNatural())
            {
                NaturalEventBean natural = new NaturalEventBean(eventType, new Object[] {newEvent.getUnderlying()}, newEvent);
                NaturalEventBean naturalOld = new NaturalEventBean(eventType, new Object[] {oldEvent.getUnderlying()}, oldEvent);
                this.updateChildren(new NaturalEventBean[]{natural}, new NaturalEventBean[]{naturalOld});
            }
            else
            {
                this.updateChildren(new EventBean[]{newEvent}, new EventBean[]{oldEvent});
            }
        }
        catch (RuntimeException ex)
        {
            log.error("Unexpected error updating child view: " + ex.getMessage());
        }
    }
}

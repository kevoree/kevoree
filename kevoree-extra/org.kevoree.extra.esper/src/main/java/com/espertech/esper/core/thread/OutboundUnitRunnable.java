package com.espertech.esper.core.thread;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.StatementResultServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Outbound unit.
 */
public class OutboundUnitRunnable implements Runnable
{
    private static final Log log = LogFactory.getLog(OutboundUnitRunnable.class);

    private final UniformPair<EventBean[]> events;
    private final StatementResultServiceImpl statementResultService;

    /**
     * Ctor.
     * @param events to dispatch
     * @param statementResultService handles result indicate
     */
    public OutboundUnitRunnable(UniformPair<EventBean[]> events, StatementResultServiceImpl statementResultService)
    {
        this.events = events;
        this.statementResultService = statementResultService;
    }

    public void run()
    {
        try
        {
            statementResultService.processDispatch(events);
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing dispatch: " + e.getMessage(), e);
        }
    }
}

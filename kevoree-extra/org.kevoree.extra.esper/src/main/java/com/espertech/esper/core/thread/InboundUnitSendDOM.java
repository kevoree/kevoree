package com.espertech.esper.core.thread;

import com.espertech.esper.core.EPServicesContext;
import com.espertech.esper.core.EPRuntimeImpl;
import com.espertech.esper.client.EventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Inbound unit for DOM events.
 */
public class InboundUnitSendDOM implements InboundUnitRunnable
{
    private static final Log log = LogFactory.getLog(InboundUnitSendDOM.class);

    private final org.w3c.dom.Node event;
    private final EPServicesContext services;
    private final EPRuntimeImpl runtime;

    /**
     * Ctor.
     * @param event document
     * @param services for wrapping event
     * @param runtime runtime to process
     */
    public InboundUnitSendDOM(org.w3c.dom.Node event, EPServicesContext services, EPRuntimeImpl runtime)
    {
        this.event = event;
        this.services = services;
        this.runtime = runtime;
    }

    public void run()
    {
        try
        {
            EventBean eventBean = services.getEventAdapterService().adapterForDOM(event);
            runtime.processEvent(eventBean);
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing DOM event: " + e.getMessage(), e);
        }
    }
}

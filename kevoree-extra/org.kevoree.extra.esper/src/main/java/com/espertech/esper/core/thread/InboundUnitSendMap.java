package com.espertech.esper.core.thread;

import com.espertech.esper.core.EPRuntimeImpl;
import com.espertech.esper.core.EPServicesContext;
import com.espertech.esper.client.EventBean;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Inbound work unit processing a map event.
 */
public class InboundUnitSendMap implements InboundUnitRunnable
{
    private static final Log log = LogFactory.getLog(InboundUnitSendMap.class);
    private final Map map;
    private final String eventTypeName;
    private final EPServicesContext services;
    private final EPRuntimeImpl runtime;

    /**
     * Ctor.
     * @param map to send
     * @param eventTypeName type name
     * @param services to wrap
     * @param runtime to process
     */
    public InboundUnitSendMap(Map map, String eventTypeName, EPServicesContext services, EPRuntimeImpl runtime)
    {
        this.eventTypeName = eventTypeName;
        this.map = map;
        this.services = services;
        this.runtime = runtime;
    }

    public void run()
    {
        try
        {
            EventBean eventBean = services.getEventAdapterService().adapterForMap(map, eventTypeName);
            runtime.processWrappedEvent(eventBean);
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing Map event: " + e.getMessage(), e);
        }
    }
}

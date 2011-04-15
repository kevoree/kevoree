package com.espertech.esper.core.thread;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.core.EPRuntimeImpl;
import com.espertech.esper.core.EPStatementHandleCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Route unit for single match.
 */
public class RouteUnitSingle implements RouteUnitRunnable
{
    private static final Log log = LogFactory.getLog(RouteUnitSingle.class);

    private final EPRuntimeImpl epRuntime;
    private EPStatementHandleCallback handleCallback;
    private final EventBean event;
    private final long filterVersion;

    /**
     * Ctor.
     * @param epRuntime runtime to process
     * @param handleCallback callback
     * @param event event
     * @param filterVersion version of filter
     */
    public RouteUnitSingle(EPRuntimeImpl epRuntime, EPStatementHandleCallback handleCallback, EventBean event, long filterVersion)
    {
        this.epRuntime = epRuntime;
        this.event = event;
        this.handleCallback = handleCallback;
        this.filterVersion = filterVersion;
    }

    public void run()
    {
        try
        {
            epRuntime.processStatementFilterSingle(handleCallback.getEpStatementHandle(), handleCallback, event, filterVersion);

            epRuntime.dispatch();

            epRuntime.processThreadWorkQueue();
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing route execution: " + e.getMessage(), e);
        }
    }

}

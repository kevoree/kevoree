package com.espertech.esper.core.thread;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.core.EPRuntimeImpl;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.filter.FilterHandleCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayDeque;

/**
 * Route execution work unit.
 */
public class RouteUnitMultiple implements RouteUnitRunnable
{
    private static final Log log = LogFactory.getLog(RouteUnitMultiple.class);

    private final EPRuntimeImpl epRuntime;
    private final EventBean event;
    private ArrayDeque<FilterHandleCallback> callbackList;
    private EPStatementHandle handle;
    private final long filterVersion;

    /**
     * Ctor.
     * @param epRuntime runtime to process
     * @param callbackList callback list
     * @param event event to pass
     * @param handle statement handle
     * @param filterVersion version of filter
     */
    public RouteUnitMultiple(EPRuntimeImpl epRuntime, ArrayDeque<FilterHandleCallback> callbackList, EventBean event, EPStatementHandle handle, long filterVersion)
    {
        this.epRuntime = epRuntime;
        this.callbackList = callbackList;
        this.event = event;
        this.handle = handle;
        this.filterVersion = filterVersion;
    }

    public void run()
    {
        try
        {
            epRuntime.processStatementFilterMultiple(handle, callbackList, event, filterVersion);

            epRuntime.dispatch();

            epRuntime.processThreadWorkQueue();
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing multiple route execution: " + e.getMessage(), e);
        }
    }

}

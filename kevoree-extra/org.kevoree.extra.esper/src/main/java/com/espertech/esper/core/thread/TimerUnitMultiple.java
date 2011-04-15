package com.espertech.esper.core.thread;

import com.espertech.esper.core.EPRuntimeImpl;
import com.espertech.esper.core.EPServicesContext;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Timer unit for multiple callbacks for a statement.
 */
public class TimerUnitMultiple implements TimerUnit
{
    private static final Log log = LogFactory.getLog(TimerUnitMultiple.class);

    private final EPServicesContext services;
    private final EPRuntimeImpl runtime;
    private final Object callbackObject;
    private final EPStatementHandle handle;
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param services engine services
     * @param runtime runtime to process
     * @param handle statement handle
     * @param callbackObject callback list
     * @param exprEvaluatorContext expression evaluation context
     */
    public TimerUnitMultiple(EPServicesContext services, EPRuntimeImpl runtime, EPStatementHandle handle, Object callbackObject, ExprEvaluatorContext exprEvaluatorContext)
    {
        this.services = services;
        this.handle = handle;
        this.runtime = runtime;
        this.callbackObject = callbackObject;
        this.exprEvaluatorContext = exprEvaluatorContext;
    }

    public void run()
    {
        try
        {
            EPRuntimeImpl.processStatementScheduleMultiple(handle, callbackObject, services, exprEvaluatorContext);

            // Let listeners know of results
            runtime.dispatch();

            // Work off the event queue if any events accumulated in there via a route()
            runtime.processThreadWorkQueue();
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing multiple timer execution: " + e.getMessage(), e);
        }
    }
}

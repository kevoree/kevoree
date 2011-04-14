package com.espertech.esper.core.thread;

import com.espertech.esper.core.EPRuntimeImpl;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.EPServicesContext;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Timer unit for a single callback for a statement.
 */
public class TimerUnitSingle implements TimerUnit
{
    private static final Log log = LogFactory.getLog(TimerUnitSingle.class);

    private final EPServicesContext services;
    private final EPRuntimeImpl runtime;
    private final EPStatementHandleCallback handleCallback;
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param services engine services
     * @param runtime runtime to process
     * @param handleCallback callback 
     * @param exprEvaluatorContext expression evaluation context
     */
    public TimerUnitSingle(EPServicesContext services, EPRuntimeImpl runtime, EPStatementHandleCallback handleCallback, ExprEvaluatorContext exprEvaluatorContext)
    {
        this.services = services;
        this.runtime = runtime;
        this.handleCallback = handleCallback;
        this.exprEvaluatorContext = exprEvaluatorContext;
    }

    public void run()
    {
        try
        {
            EPRuntimeImpl.processStatementScheduleSingle(handleCallback, services, exprEvaluatorContext);

            runtime.dispatch();

            runtime.processThreadWorkQueue();
        }
        catch (RuntimeException e)
        {
            log.error("Unexpected error processing timer execution: " + e.getMessage(), e);
        }
    }
}

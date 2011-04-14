/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.StreamTypeServiceImpl;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.schedule.*;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Output condition handling crontab-at schedule output.
 */
public final class OutputConditionCrontab implements OutputCondition
{
    private static final boolean DO_OUTPUT = true;
	private static final boolean FORCE_UPDATE = true;

    private final OutputCallback outputCallback;
    private final ScheduleSlot scheduleSlot;

    private Long currentReferencePoint;
    private StatementContext context;
    private boolean isCallbackScheduled;
    private ScheduleSpec scheduleSpec;

    /**
     * Constructor.
     * @param context is the view context for time scheduling
     * @param outputCallback is the callback to make once the condition is satisfied
     * @param scheduleSpecExpressionList list of schedule parameters
     * @throws ExprValidationException if the crontab expression failed to validate
     */
    public OutputConditionCrontab(List<ExprNode> scheduleSpecExpressionList,
                                   StatementContext context,
                                   OutputCallback outputCallback)
            throws ExprValidationException
    {
		if(outputCallback ==  null)
		{
			throw new NullPointerException("Output condition crontab requires a non-null callback");
		}
        if (context == null)
        {
            String message = "OutputConditionTime requires a non-null view context";
            throw new NullPointerException(message);
        }

        this.context = context;
        this.outputCallback = outputCallback;
        this.scheduleSlot = context.getScheduleBucket().allocateSlot();

        // Validate the expression
        ExprEvaluator[] expressions = new ExprEvaluator[scheduleSpecExpressionList.size()];
        int count = 0;
        for (ExprNode parameters : scheduleSpecExpressionList)
        {
            ExprNode node = parameters.getValidatedSubtree(new StreamTypeServiceImpl(context.getEngineURI(), false), context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);
            expressions[count++] = node.getExprEvaluator();
        }

        try
        {
            Object[] scheduleSpecParameterList = evaluate(expressions, context);
            scheduleSpec = ScheduleSpecUtil.computeValues(scheduleSpecParameterList);
        }
        catch (ScheduleParameterException e)
        {
            throw new IllegalArgumentException("Invalid schedule specification : " + e.getMessage(), e);
        }
    }

    public final void updateOutputCondition(int newEventsCount, int oldEventsCount)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
        	log.debug(".updateOutputCondition, " +
        			"  newEventsCount==" + newEventsCount +
        			"  oldEventsCount==" + oldEventsCount);
        }

        if (currentReferencePoint == null)
        {
        	currentReferencePoint = context.getSchedulingService().getTime();
        }

        // Schedule the next callback if there is none currently scheduled
        if (!isCallbackScheduled)
        {
        	scheduleCallback();
        }
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " spec=" + scheduleSpec;
    }

    private void scheduleCallback()
    {
    	isCallbackScheduled = true;
        long current = context.getSchedulingService().getTime();

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".scheduleCallback Scheduled new callback for " +
                    " now=" + current +
                    " currentReferencePoint=" + currentReferencePoint +
                    " spec=" + scheduleSpec);
        }

        ScheduleHandleCallback callback = new ScheduleHandleCallback() {
            public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
            {
                OutputConditionCrontab.this.isCallbackScheduled = false;
                OutputConditionCrontab.this.outputCallback.continueOutputProcessing(DO_OUTPUT, FORCE_UPDATE);
                scheduleCallback();
            }
        };
        EPStatementHandleCallback handle = new EPStatementHandleCallback(context.getEpStatementHandle(), callback);
        context.getSchedulingService().add(scheduleSpec, handle, scheduleSlot);
    }

    private static Object[] evaluate(ExprEvaluator[] parameters, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object[] results = new Object[parameters.length];
        int count = 0;
        for (ExprEvaluator expr : parameters)
        {
            try
            {
                results[count] = expr.evaluate(null, true, exprEvaluatorContext);
                count++;
            }
            catch (RuntimeException ex)
            {
                String message = "Failed expression evaluation in crontab timer-at for parameter " + count + ": " + ex.getMessage();
                log.error(message, ex);
                throw new IllegalArgumentException(message);
            }
        }
        return results;
    }

    private static final Log log = LogFactory.getLog(OutputConditionCrontab.class);
}

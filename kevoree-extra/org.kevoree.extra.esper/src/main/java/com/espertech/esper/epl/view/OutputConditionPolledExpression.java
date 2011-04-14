/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeIdentifierVisitor;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.spec.OnTriggerSetAssignment;
import com.espertech.esper.epl.variable.VariableReadWritePackage;
import com.espertech.esper.event.EventAdapterService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Output condition for output rate limiting that handles when-then expressions for controlling output.
 */
public class OutputConditionPolledExpression implements OutputConditionPolled
{
    private static final Log log = LogFactory.getLog(OutputConditionPolledExpression.class);
    private final ExprEvaluator whenExpressionNode;
    private final StatementContext context;
    private final VariableReadWritePackage variableReadWritePackage;

    private Map<String, Object> builtinProperties;
    private EventBean[] eventsPerStream;
    private EventType builtinPropertiesEventType;

    // ongoing builtin properties
    private int totalNewEventsCount;
    private int totalOldEventsCount;
    private Long lastOutputTimestamp;

    /**
     * Ctor.
     * @param whenExpressionNode the expression to evaluate, returning true when to output
     * @param assignments is the optional then-clause variable assignments, or null or empty if none
     * @param context statement context
     * @throws com.espertech.esper.epl.expression.ExprValidationException when validation fails
     */
    public OutputConditionPolledExpression(ExprNode whenExpressionNode, List<OnTriggerSetAssignment> assignments, final StatementContext context)
            throws ExprValidationException
    {
        this.whenExpressionNode = whenExpressionNode.getExprEvaluator();
        this.context = context;
        this.eventsPerStream = new EventBean[1];

        // determine if using properties
        boolean containsBuiltinProperties = false;
        if (containsBuiltinProperties(whenExpressionNode))
        {
            containsBuiltinProperties = true;
        }
        else
        {
            if (assignments != null)
            {
                for (OnTriggerSetAssignment assignment : assignments)
                {
                    if (containsBuiltinProperties(assignment.getExpression()))
                    {
                        containsBuiltinProperties = true;
                    }
                }
            }
        }

        if (containsBuiltinProperties)
        {
            builtinProperties = new HashMap<String, Object>();
            builtinPropertiesEventType = getBuiltInEventType(context.getEventAdapterService());
            lastOutputTimestamp = context.getSchedulingService().getTime();
        }

        if (assignments != null)
        {
            variableReadWritePackage = new VariableReadWritePackage(assignments, context.getVariableService(), context.getEventAdapterService());
        }
        else
        {
            variableReadWritePackage = null;
        }
    }

    public boolean updateOutputCondition(int newEventsCount, int oldEventsCount)
    {
        this.totalNewEventsCount += newEventsCount;
        this.totalOldEventsCount += oldEventsCount;

        boolean isOutput = evaluate();
        if (isOutput)
        {
            resetBuiltinProperties();

            // execute assignments
            if (variableReadWritePackage != null)
            {
                if (builtinProperties != null)
                {
                    builtinProperties.put("count_insert", totalNewEventsCount);
                    builtinProperties.put("count_remove", totalOldEventsCount);
                    builtinProperties.put("last_output_timestamp", lastOutputTimestamp);
                    eventsPerStream[0] = context.getEventAdapterService().adaptorForTypedMap(builtinProperties, builtinPropertiesEventType);
                }

                try {
                    variableReadWritePackage.writeVariables(context.getVariableService(), eventsPerStream, null, context);
                }
                finally {
                }
            }
        }
        return isOutput;
    }

    private boolean evaluate()
    {
        if (builtinProperties != null)
        {
            builtinProperties.put("count_insert", totalNewEventsCount);
            builtinProperties.put("count_remove", totalOldEventsCount);
            builtinProperties.put("last_output_timestamp", lastOutputTimestamp);
            eventsPerStream[0] = context.getEventAdapterService().adaptorForTypedMap(builtinProperties, builtinPropertiesEventType);
        }

        boolean result = false;
        Boolean output = (Boolean) whenExpressionNode.evaluate(eventsPerStream, true, context);
        if ((output != null) && (output))
        {
            result = true;
        }

        return result;
    }


    /**
     * Build the event type for built-in properties.
     * @param eventAdapterService event adapters
     * @return event type
     */
    public static EventType getBuiltInEventType(EventAdapterService eventAdapterService)
    {
        Map<String, Object> outputLimitProperties = new HashMap<String, Object>();
        outputLimitProperties.put("count_insert", Integer.class);
        outputLimitProperties.put("count_remove", Integer.class);
        outputLimitProperties.put("last_output_timestamp", Long.class);
        return eventAdapterService.createAnonymousMapType(outputLimitProperties);
    }

    private void resetBuiltinProperties()
    {
        if (builtinProperties  != null)
        {
            totalNewEventsCount = 0;
            totalOldEventsCount = 0;
            lastOutputTimestamp = context.getSchedulingService().getTime();
        }
    }

    private boolean containsBuiltinProperties(ExprNode expr)
    {
        ExprNodeIdentifierVisitor propertyVisitor = new ExprNodeIdentifierVisitor(false);
        expr.accept(propertyVisitor);
        return !propertyVisitor.getExprProperties().isEmpty();
    }
}

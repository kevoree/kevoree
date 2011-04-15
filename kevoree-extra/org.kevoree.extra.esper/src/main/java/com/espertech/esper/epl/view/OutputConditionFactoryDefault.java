/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.epl.spec.OutputLimitSpec;
import com.espertech.esper.epl.spec.OutputLimitLimitType;
import com.espertech.esper.epl.spec.OutputLimitRateType;
import com.espertech.esper.epl.variable.VariableReader;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.util.JavaClassHelper;

/**
 * Factory for output condition instances.
 */
public class OutputConditionFactoryDefault implements OutputConditionFactory
{
	private static final Log log = LogFactory.getLog(OutputConditionFactoryDefault.class);

    /**
     * Creates an output condition instance.
     * @param outputLimitSpec specifies what kind of condition to create
     * @param statementContext supplies the services required such as for scheduling callbacks
     * @param outputCallback is the method to invoke for output
     * @return instance for performing output
     */
	public OutputCondition createCondition(OutputLimitSpec outputLimitSpec,
										 	  	  StatementContext statementContext,
										 	      OutputCallback outputCallback,
                                                   boolean isGrouped)
            throws ExprValidationException
    {
		if(outputCallback ==  null)
		{
			throw new NullPointerException("Output condition by count requires a non-null callback");
		}

		if(outputLimitSpec == null)
		{
			return new OutputConditionNull(outputCallback);
		}

        // Check if a variable is present
        VariableReader reader = null;
        if (outputLimitSpec.getVariableName() != null)
        {
            reader = statementContext.getVariableService().getReader(outputLimitSpec.getVariableName());
            if (reader == null)
            {
                throw new IllegalArgumentException("Variable named '" + outputLimitSpec.getVariableName() + "' has not been declared");
            }
        }

        if(outputLimitSpec.getDisplayLimit() == OutputLimitLimitType.FIRST)
		{
            if (isGrouped) {
                return new OutputConditionNull(outputCallback);
            }
			return new OutputConditionFirst(outputLimitSpec, statementContext, outputCallback, isGrouped);
		}

        if(outputLimitSpec.getRateType() == OutputLimitRateType.CRONTAB)
        {
            return new OutputConditionCrontab(outputLimitSpec.getCrontabAtSchedule(), statementContext, outputCallback);
        }
        else if(outputLimitSpec.getRateType() == OutputLimitRateType.WHEN_EXPRESSION)
        {
            return new OutputConditionExpression(outputLimitSpec.getWhenExpressionNode(), outputLimitSpec.getThenExpressions(), statementContext, outputCallback);
        }
        else if(outputLimitSpec.getRateType() == OutputLimitRateType.EVENTS)
		{
            if (log.isDebugEnabled())
            {
			    log.debug(".createCondition creating OutputConditionCount with event rate " + outputLimitSpec);
            }

            if ((reader != null) && (!JavaClassHelper.isNumericNonFP(reader.getType())))
            {
                throw new IllegalArgumentException("Variable named '" + outputLimitSpec.getVariableName() + "' must be type integer, long or short");
            }

            int rate = -1;
            if (outputLimitSpec.getRate() != null)
            {
                rate = outputLimitSpec.getRate().intValue();
            }
            return new OutputConditionCount(rate, reader, outputCallback);
		}
		else
		{
            if (log.isDebugEnabled())
            {
                log.debug(".createCondition creating OutputConditionTime with interval length " + outputLimitSpec.getRate());
            }
            if ((reader != null) && (!JavaClassHelper.isNumeric(reader.getType())))
            {
                throw new IllegalArgumentException("Variable named '" + outputLimitSpec.getVariableName() + "' must be of numeric type");
            }

            return new OutputConditionTime(outputLimitSpec.getTimePeriodExpr(), statementContext, outputCallback);
		}
	}
}

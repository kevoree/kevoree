/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.epl.spec.OutputLimitSpec;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.core.StatementContext;

/**
 * Factory for output condition instances.
 */
public interface OutputConditionFactory
{
    /**
     * Creates an output condition instance.
     * @param outputLimitSpec specifies what kind of condition to create
     * @param statementContext supplies the services required such as for scheduling callbacks
     * @param outputCallback is the method to invoke for output
     * @return instance for performing output
     * @throws  ExprValidationException if validation of the output expressions fails
     */
	public OutputCondition createCondition(OutputLimitSpec outputLimitSpec,
										   StatementContext statementContext,
										   OutputCallback outputCallback,
                                           boolean isGrouped)
            throws ExprValidationException;
}

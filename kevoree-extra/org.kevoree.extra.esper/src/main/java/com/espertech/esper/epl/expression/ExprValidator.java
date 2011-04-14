/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;

/**
 * Validation interface for expression nodes.
 */
public interface ExprValidator
{
    /**
     * Validate node.
     * @param streamTypeService serves stream event type info
     * @param methodResolutionService - for resolving class names in library method invocations
     * @param viewResourceDelegate - delegates for view resources to expression nodes
     * @param timeProvider - provides engine current time
     * @param variableService - provides access to variable values
     * @param exprEvaluatorContext context for expression evalauation
     * @throws ExprValidationException thrown when validation failed
     */
    public void validate(StreamTypeService streamTypeService,
                         MethodResolutionService methodResolutionService,
                         ViewResourceDelegate viewResourceDelegate,
                         TimeProvider timeProvider,
                         VariableService variableService,
                         ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException;

    public ExprEvaluator getExprEvaluator();
}

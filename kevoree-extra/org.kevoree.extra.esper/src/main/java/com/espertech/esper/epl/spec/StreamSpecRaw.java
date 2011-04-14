/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.named.NamedWindowService;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.vaevent.ValueAddEventService;
import com.espertech.esper.pattern.PatternObjectResolutionService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.core.StatementContext;

import java.net.URI;
import java.util.Set;

/**
 * An uncompiled, unoptimize for of stream specification created by a parser.
 */
public interface StreamSpecRaw extends StreamSpec
{
    /**
     * Compiles a raw stream specification consisting event type information and filter expressions
     * to an validated, optimized form for use with filter service
     * @param eventTypeReferences event type names used by the statement
     * @param statementContext statement-level services
     * @param isInsertInto true for insert-into 
     * @return compiled stream
     * @throws ExprValidationException to indicate validation errors
     */
    public StreamSpecCompiled compile(StatementContext statementContext,
                                      Set<String> eventTypeReferences,
                                      boolean isInsertInto)
        throws ExprValidationException;

}

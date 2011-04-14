/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * A deletion strategy is for use with named window in on-delete statements and encapsulates
 * the strategy for resolving one or more events arriving in the on-clause of an on-delete statement
 * to one or more events to be deleted from the named window.
 */
public interface LookupStrategy
{
    /**
     * Determines the events to be deleted from a named window.
     * @param newData is the correlation events
     * @return the events to delete from the named window
     * @param exprEvaluatorContext expression evaluation context
     */
    public EventBean[] lookup(EventBean[] newData, ExprEvaluatorContext exprEvaluatorContext);
}

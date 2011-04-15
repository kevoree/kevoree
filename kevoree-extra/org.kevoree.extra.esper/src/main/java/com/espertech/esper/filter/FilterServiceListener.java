/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Collection;

/**
 * Listener to filter activity.
 */
public interface FilterServiceListener
{
    /**
     * Indicates an event being filtered.
     * @param event event
     * @param matches matches found
     * @param exprEvaluatorContext expression
     * @param statementId optional statement id if for a statement
     */
    public void filtering(EventBean event, Collection<FilterHandle> matches, ExprEvaluatorContext exprEvaluatorContext, String statementId);
}
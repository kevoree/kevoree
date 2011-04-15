/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.spec.UpdateDesc;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.lang.annotation.Annotation;

/**
 * Interface for a service that routes events within the engine for further processing.
 */
public interface InternalEventRouter
{
    /**
     * Add preprocessing.
     * @param eventType type to add for
     * @param desc update statement specification
     * @param annotations annotations
     * @param outputView output view
     * @throws ExprValidationException when expression validation fails
     */
    public void addPreprocessing(EventType eventType, UpdateDesc desc, Annotation[] annotations, InternalRoutePreprocessView outputView)
            throws ExprValidationException;

    /**
     * Remove preprocessing.
     * @param eventType type to remove for
     * @param desc update statement specification
     */
    public void removePreprocessing(EventType eventType, UpdateDesc desc);
    
    /**
     * Route the event such that the event is processed as required.
     * @param event to route
     * @param statementHandle provides statement resources
     * @param exprEvaluatorContext context for expression evalauation
     * @param routeDest routing destination
     */
    public void route(EventBean event, EPStatementHandle statementHandle, InternalEventRouteDest routeDest, ExprEvaluatorContext exprEvaluatorContext, boolean addToFront);

    public boolean isHasPreprocessing();

    public EventBean preprocess(EventBean event, ExprEvaluatorContext engineFilterAndDispatchTimeContext);
}

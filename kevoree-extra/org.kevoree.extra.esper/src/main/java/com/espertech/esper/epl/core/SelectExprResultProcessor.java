/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.StatementResultService;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.event.NaturalEventBean;

/**
 * A select expression processor that check what type of result (synthetic and natural) event is expected and
 * produces.
 */
public class SelectExprResultProcessor implements SelectExprProcessor
{
    private final StatementResultService statementResultService;
    private final SelectExprProcessor syntheticProcessor;
    private final BindProcessor bindProcessor;
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param statementResultService for awareness of listeners and subscribers handles output results
     * @param syntheticProcessor is the processor generating synthetic events according to the select clause
     * @param bindProcessor for generating natural object column results
     * @param exprEvaluatorContext context for expression evalauation
     * @throws ExprValidationException if the validation failed
     */
    public SelectExprResultProcessor(StatementResultService statementResultService,
                                     SelectExprProcessor syntheticProcessor,
                                     BindProcessor bindProcessor,
                                     ExprEvaluatorContext exprEvaluatorContext)
            throws ExprValidationException
    {
        this.statementResultService = statementResultService;
        this.syntheticProcessor = syntheticProcessor;
        this.bindProcessor = bindProcessor;
        this.exprEvaluatorContext = exprEvaluatorContext;
    }

    public EventType getResultEventType()
    {
        return syntheticProcessor.getResultEventType();
    }

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        if ((isSynthesize) && (!statementResultService.isMakeNatural()))
        {
            return syntheticProcessor.process(eventsPerStream, isNewData, isSynthesize);
        }

        EventBean syntheticEvent = null;
        EventType syntheticEventType = null;
        if (statementResultService.isMakeSynthetic() || isSynthesize)
        {
            syntheticEvent = syntheticProcessor.process(eventsPerStream, isNewData, isSynthesize);

            if (!statementResultService.isMakeNatural())
            {
                return syntheticEvent;
            }

            syntheticEventType = syntheticProcessor.getResultEventType();
        }

        if (!statementResultService.isMakeNatural())
        {
            return null; // neither synthetic nor natural required, be cheap and generate no output event
        }

        Object[] parameters = bindProcessor.process(eventsPerStream, isNewData, exprEvaluatorContext);
        return new NaturalEventBean(syntheticEventType, parameters, syntheticEvent);
    }
}

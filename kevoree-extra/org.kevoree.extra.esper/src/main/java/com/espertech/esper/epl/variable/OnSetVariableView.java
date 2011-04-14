/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.variable;

import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.core.StatementResultService;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.spec.OnTriggerSetAssignment;
import com.espertech.esper.epl.spec.OnTriggerSetDesc;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A view that handles the setting of variables upon receipt of a triggering event.
 * <p>
 * Variables are updated atomically and thus a separate commit actually updates the
 * new variable values, or a rollback if an exception occured during validation.
 */
public class OnSetVariableView extends ViewSupport
{
    private static final Log log = LogFactory.getLog(OnSetVariableView.class);
    private final OnTriggerSetDesc desc;
    private final EventAdapterService eventAdapterService;
    private final VariableService variableService;
    private final EventType eventType;
    private final VariableReadWritePackage variableReadWritePackage;
    private final EventBean[] eventsPerStream = new EventBean[1];
    private final StatementResultService statementResultService;
    private final ExprEvaluatorContext exprEvaluatorContext;

    /**
     * Ctor.
     * @param desc specification for the on-set statement
     * @param eventAdapterService for creating statements
     * @param variableService for setting variables
     * @param statementResultService for coordinating on whether insert and remove stream events should be posted
     * @param exprEvaluatorContext context for expression evalauation
     * @throws ExprValidationException if the assignment expressions are invalid
     */
    public OnSetVariableView(OnTriggerSetDesc desc, EventAdapterService eventAdapterService, VariableService variableService, StatementResultService statementResultService, ExprEvaluatorContext exprEvaluatorContext)
            throws ExprValidationException
    {
        this.desc = desc;
        this.eventAdapterService = eventAdapterService;
        this.variableService = variableService;
        this.statementResultService = statementResultService;
        this.exprEvaluatorContext = exprEvaluatorContext;

        variableReadWritePackage = new VariableReadWritePackage(desc.getAssignments(), variableService, eventAdapterService);
        eventType = eventAdapterService.createAnonymousMapType(variableReadWritePackage.getVariableTypes());
    }

    public void update(EventBean[] newData, EventBean[] oldData)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".update Received update, " +
                    "  newData.length==" + ((newData == null) ? 0 : newData.length) +
                    "  oldData.length==" + ((oldData == null) ? 0 : oldData.length));
        }

        if ((newData == null) || (newData.length == 0))
        {
            return;
        }

        Map<String, Object> values = null;
        boolean produceOutputEvents = (statementResultService.isMakeNatural() || statementResultService.isMakeSynthetic());

        if (produceOutputEvents)
        {
            values = new HashMap<String, Object>();
        }

        eventsPerStream[0] = newData[newData.length - 1];
        variableReadWritePackage.writeVariables(variableService, eventsPerStream, values, exprEvaluatorContext);
        
        if (values != null)
        {
            EventBean newDataOut[] = new EventBean[1];
            newDataOut[0] = eventAdapterService.adaptorForTypedMap(values, eventType);
            this.updateChildren(newDataOut, null);
        }
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public Iterator<EventBean> iterator()
    {
        Map<String, Object> values = variableReadWritePackage.iterate();
        EventBean event = eventAdapterService.adaptorForTypedMap(values, eventType);
        return new SingleEventIterator(event);
    }
}

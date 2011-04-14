/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.epl.spec.StatementSpecRaw;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Collection;
import java.util.Map;

/**
 * Represents an exists-subselect in an expression tree.
 */
public class ExprSubselectExistsNode extends ExprSubselectNode
{
    private static final Log log = LogFactory.getLog(ExprSubselectExistsNode.class);
    private static final long serialVersionUID = 7082390247880356269L;

    /**
     * Ctor.
     * @param statementSpec is the lookup statement spec from the parser, unvalidated
     */
    public ExprSubselectExistsNode(StatementSpecRaw statementSpec)
    {
        super(statementSpec);
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, Collection<EventBean> matchingEvents, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (matchingEvents == null)
        {
            return false;
        }
        if (matchingEvents.size() == 0)
        {
            return false;
        }

        if (filterExpr == null)
        {
            return true;
        }

        // Evaluate filter
        EventBean[] events = new EventBean[eventsPerStream.length + 1];
        System.arraycopy(eventsPerStream, 0, events, 1, eventsPerStream.length);

        for (EventBean subselectEvent : matchingEvents)
        {
            // Prepare filter expression event list
            events[0] = subselectEvent;

            Boolean pass = (Boolean) filterExpr.evaluate(events, true, exprEvaluatorContext);
            if ((pass != null) && (pass))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAllowMultiColumnSelect() {
        return false;
    }    
}

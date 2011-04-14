/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.spec.StatementSpecRaw;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Represents a subselect in an expression tree.
 */
public class ExprSubselectRowNode extends ExprSubselectNode
{
    private static final Log log = LogFactory.getLog(ExprSubselectRowNode.class);
    private static final long serialVersionUID = -7865711714805807559L;

    /**
     * Ctor.
     * @param statementSpec is the lookup statement spec from the parser, unvalidated
     */
    public ExprSubselectRowNode(StatementSpecRaw statementSpec)
    {
        super(statementSpec);
    }

    public Class getType()
    {
        if (selectClause == null)   // wildcards allowed
        {
            return rawEventType.getUnderlyingType();
        }
        if (selectClause.length == 1) {
            return selectClause[0].getExprEvaluator().getType();
        }
        return null;
    }

    public Map<String, Object> getEventType() throws ExprValidationException {
        if ((selectClause == null) || (selectClause.length < 2)) {
            return null;
        }

        Set<String> uniqueNames = new HashSet<String>();
        Map<String, Object> type = new LinkedHashMap<String, Object>();

        for (int i = 0; i < selectClause.length; i++) {
            String assignedName = this.selectAsNames[i];
            if (assignedName == null) {
                assignedName = selectClause[i].toExpressionString();
            }
            if (uniqueNames.add(assignedName)) {
                type.put(assignedName, selectClause[i].getExprEvaluator().getType());
            }
            else {
                throw new ExprValidationException("Column " + i + " in subquery does not have a unique column name assigned");
            }
        }
        return type;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, Collection<EventBean> matchingEvents, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (matchingEvents == null)
        {
            return null;
        }
        if (matchingEvents.size() == 0)
        {
            return null;
        }
        if ((filterExpr == null) && (matchingEvents.size() > 1))
        {
            log.warn("Subselect returned more then one row in subselect '" + toExpressionString() + "', returning null result");
            return null;
        }

        // Evaluate filter
        EventBean subSelectResult = null;
        EventBean[] events = new EventBean[eventsPerStream.length + 1];
        System.arraycopy(eventsPerStream, 0, events, 1, eventsPerStream.length);

        if (filterExpr != null)
        {
            for (EventBean subselectEvent : matchingEvents)
            {
                // Prepare filter expression event list
                events[0] = subselectEvent;

                Boolean pass = (Boolean) filterExpr.evaluate(events, true, exprEvaluatorContext);
                if ((pass != null) && (pass))
                {
                    if (subSelectResult != null)
                    {
                        log.warn("Subselect returned more then one row in subselect '" + toExpressionString() + "', returning null result");
                        return null;
                    }
                    subSelectResult = subselectEvent;
                }
            }

            if (subSelectResult == null)
            {
                return null;
            }
        }
        else
        {
            subSelectResult = matchingEvents.iterator().next();
        }

        events[0] = subSelectResult;
        Object result;

        if (selectClause != null)
        {
            if (selectClause.length == 1) {
                result = selectClauseEvaluator[0].evaluate(events, true, exprEvaluatorContext);
            }
            else {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < selectClauseEvaluator.length; i++) {
                    Object resultEntry = selectClauseEvaluator[i].evaluate(events, true, exprEvaluatorContext);
                    map.put(this.selectAsNames[i], resultEntry);
                }
                result = map;
            }
        }
        else
        {
            result = events[0].getUnderlying();
        }

        return result;
    }

    @Override
    public boolean isAllowMultiColumnSelect() {
        return true;
    }
}

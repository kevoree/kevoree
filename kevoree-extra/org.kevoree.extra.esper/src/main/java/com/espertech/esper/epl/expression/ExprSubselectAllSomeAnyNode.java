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
import com.espertech.esper.type.RelationalOpEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a subselect in an expression tree.
 */
public class ExprSubselectAllSomeAnyNode extends ExprSubselectNode
{
    private static final Log log = LogFactory.getLog(ExprSubselectInNode.class);
    private final boolean isNot;
    private final boolean isAll;
    private final RelationalOpEnum relationalOp;

    private transient SubselectEvalStrategy evalStrategy;
    private static final long serialVersionUID = -3884694910286266280L;

    /**
     * Ctor.
     * @param statementSpec is the lookup statement spec from the parser, unvalidated
     * @param not when NOT
     * @param all when ALL, false for ANY
     * @param relationalOpEnum operator
     */
    public ExprSubselectAllSomeAnyNode(StatementSpecRaw statementSpec, boolean not, boolean all, RelationalOpEnum relationalOpEnum)
    {
        super(statementSpec);
        isNot = not;
        isAll = all;
        this.relationalOp = relationalOpEnum;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    /**
     * Returns true for not.
     * @return not indicator
     */
    public boolean isNot()
    {
        return isNot;
    }

    /**
     * Returns true for all.
     * @return all indicator
     */
    public boolean isAll()
    {
        return isAll;
    }

    /**
     * Returns relational op.
     * @return op
     */
    public RelationalOpEnum getRelationalOp()
    {
        return relationalOp;
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        evalStrategy = SubselectEvalStrategyFactory.createStrategy(this, isNot, isAll, !isAll, relationalOp);
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, Collection<EventBean> matchingEvents, ExprEvaluatorContext exprEvaluatorContext)
    {
        return evalStrategy.evaluate(eventsPerStream, isNewData, matchingEvents, exprEvaluatorContext);
    }

    @Override
    public boolean isAllowMultiColumnSelect() {
        return false;
    }    
}
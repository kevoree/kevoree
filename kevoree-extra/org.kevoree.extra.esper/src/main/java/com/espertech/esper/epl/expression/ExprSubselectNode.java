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
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.lookup.TableLookupStrategy;
import com.espertech.esper.epl.spec.StatementSpecCompiled;
import com.espertech.esper.epl.spec.StatementSpecRaw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a subselect in an expression tree.
 */
public abstract class ExprSubselectNode extends ExprNode implements ExprEvaluator
{
    private static final Log log = LogFactory.getLog(ExprSubselectNode.class);

    /**
     * The validated select clause.
     */
    protected ExprNode[] selectClause;
    protected transient ExprEvaluator[] selectClauseEvaluator;

    protected String[] selectAsNames;

    /**
     * The validate filter expression.
     */
    protected transient ExprEvaluator filterExpr;

    /**
     * The event type generated for wildcard selects.
     */
    protected transient EventType rawEventType;

    private transient StreamTypeService filterSubqueryStreamTypes;
    private StatementSpecRaw statementSpecRaw;
    private transient StatementSpecCompiled statementSpecCompiled;
    private transient TableLookupStrategy strategy;
    private transient SubselectAggregationPreprocessor subselectAggregationPreprocessor;

    private static Set<EventBean> singleNullRowEventSet = new HashSet<EventBean>();
    static
    {
        singleNullRowEventSet.add(null);
    }

    /**
     * Evaluate the lookup expression returning an evaluation result object.
     * @param eventsPerStream is the events for each stream in a join
     * @param isNewData is true for new data, or false for old data
     * @param matchingEvents is filtered results from the table of stored lookup events
     * @param exprEvaluatorContext context for expression evalauation
     * @return evaluation result
     */
    public abstract Object evaluate(EventBean[] eventsPerStream, boolean isNewData, Collection<EventBean> matchingEvents, ExprEvaluatorContext exprEvaluatorContext);

    public abstract boolean isAllowMultiColumnSelect();

    /**
     * Ctor.
     * @param statementSpec is the lookup statement spec from the parser, unvalidated
     */
    public ExprSubselectNode(StatementSpecRaw statementSpec)
    {
        this.statementSpecRaw = statementSpec;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    /**
     * Supplies a compiled statement spec.
     * @param statementSpecCompiled compiled validated filters
     */
    public void setStatementSpecCompiled(StatementSpecCompiled statementSpecCompiled)
    {
        this.statementSpecCompiled = statementSpecCompiled;
    }

    /**
     * Returns the compiled statement spec.
     * @return compiled statement
     */
    public StatementSpecCompiled getStatementSpecCompiled()
    {
        return statementSpecCompiled;
    }

    /**
     * Sets the validate select clause
     * @param selectClause is the expression representing the select clause
     */
    public void setSelectClause(ExprNode[] selectClause)
    {
        this.selectClause = selectClause;
        this.selectClauseEvaluator = ExprNodeUtility.getEvaluators(selectClause);
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Collection<EventBean> matchingEvents = strategy.lookup(eventsPerStream);
        if (subselectAggregationPreprocessor != null) {
            subselectAggregationPreprocessor.evaluate(eventsPerStream, matchingEvents, exprEvaluatorContext);
            matchingEvents = singleNullRowEventSet;
        }
        return evaluate(eventsPerStream, isNewData, matchingEvents, exprEvaluatorContext);
    }

    /**
     * Returns the uncompiled statement spec.
     * @return statement spec uncompiled
     */
    public StatementSpecRaw getStatementSpecRaw()
    {
        return statementSpecRaw;
    }

    /**
     * Supplies the name of the select expression as-tag
     * @param selectAsNames is the as-name(s)
     */
    public void setSelectAsNames(String[] selectAsNames)
    {
        this.selectAsNames = selectAsNames;
    }

    /**
     * Sets the validated filter expression, or null if there is none.
     * @param filterExpr is the filter
     */
    public void setFilterExpr(ExprEvaluator filterExpr)
    {
        this.filterExpr = filterExpr;
    }

    public String toExpressionString()
    {
        if ((selectAsNames != null) && (selectAsNames[0] != null))
        {
            return selectAsNames[0];
        }
        if (selectClause == null)
        {
            return "*";
        }
        return selectClause[0].toExpressionString();
    }

    public boolean equalsNode(ExprNode node)
    {
        return false;   // 2 subselects are never equivalent
    }

    /**
     * Sets the strategy for boiling down the table of lookup events into a subset against which to run the filter.
     * @param strategy is the looking strategy (full table scan or indexed)
     */
    public void setStrategy(TableLookupStrategy strategy)
    {
        this.strategy = strategy;
    }

    /**
     * Sets the event type generated for wildcard selects.
     * @param rawEventType is the wildcard type (parent view)
     */
    public void setRawEventType(EventType rawEventType)
    {
        this.rawEventType = rawEventType;
    }

    /**
     * Returns the select clause or null if none.
     * @return clause
     */
    public ExprNode[] getSelectClause()
    {
        return selectClause;
    }

    /**
     * Returns filter expr or null if none.
     * @return filter
     */
    public ExprEvaluator getFilterExpr()
    {
        return filterExpr;
    }

    /**
     * Returns the event type.
     * @return type
     */
    public EventType getRawEventType()
    {
        return rawEventType;
    }

    /**
     * Return stream types.
     * @return types
     */
    public StreamTypeService getFilterSubqueryStreamTypes()
    {
        return filterSubqueryStreamTypes;
    }

    /**
     * Set stream types.
     * @param filterSubqueryStreamTypes types
     */
    public void setFilterSubqueryStreamTypes(StreamTypeService filterSubqueryStreamTypes)
    {
        this.filterSubqueryStreamTypes = filterSubqueryStreamTypes;
    }

    public void setSubselectAggregationPreprocessor(SubselectAggregationPreprocessor subselectAggregationPreprocessor) {
        this.subselectAggregationPreprocessor = subselectAggregationPreprocessor;
    }
}

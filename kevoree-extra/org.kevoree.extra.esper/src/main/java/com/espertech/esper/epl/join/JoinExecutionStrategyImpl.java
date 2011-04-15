/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Set;

/**
 * Join execution strategy based on a 3-step getSelectListEvents of composing a join set, filtering the join set and
 * indicating.
 */
public class JoinExecutionStrategyImpl implements JoinExecutionStrategy
{
    private final JoinSetComposer composer;
    private final JoinSetProcessor filter;
    private final JoinSetProcessor indicator;
    private final ExprEvaluatorContext staticExprEvaluatorContext;

    /**
     * Ctor.
     * @param composer - determines join tuple set
     * @param filter - for filtering among tuples
     * @param indicator - for presenting the info to a view
     * @param staticExprEvaluatorContext expression evaluation context for static evaluation (not for runtime eval)
     */
    public JoinExecutionStrategyImpl(JoinSetComposer composer, JoinSetProcessor filter, JoinSetProcessor indicator,
                                     ExprEvaluatorContext staticExprEvaluatorContext)
    {
        this.composer = composer;
        this.filter = filter;
        this.indicator = indicator;
        this.staticExprEvaluatorContext = staticExprEvaluatorContext;
    }

    public void join(EventBean[][] newDataPerStream, EventBean[][] oldDataPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        UniformPair<Set<MultiKey<EventBean>>> joinSet = composer.join(newDataPerStream, oldDataPerStream, exprEvaluatorContext);

        filter.process(joinSet.getFirst(), joinSet.getSecond(), exprEvaluatorContext);

        if ( (!joinSet.getFirst().isEmpty()) || (!joinSet.getSecond().isEmpty()) )
        {
            indicator.process(joinSet.getFirst(), joinSet.getSecond(), exprEvaluatorContext);
        }
    }

    public Set<MultiKey<EventBean>> staticJoin()
    {
        Set<MultiKey<EventBean>> joinSet = composer.staticJoin();
        filter.process(joinSet, null, staticExprEvaluatorContext);
        return joinSet;
    }
}

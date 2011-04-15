/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * This class represents an 'every-distinct' operator in the evaluation tree representing an event expression.
 */
public class EvalEveryDistinctNode extends EvalNode
{
    private List<ExprNode> expressions;
    protected transient ExprEvaluator[] expressionsArray;
    private transient MatchedEventConvertor convertor;
    private transient PatternContext context;
    private Long msecToExpire;
    private static final long serialVersionUID = 7455570958072753956L;

    /**
     * Ctor.
     * @param expressions distinct-value expressions
     */
    protected EvalEveryDistinctNode(List<ExprNode> expressions)
    {
        this.expressions = expressions;
    }

    public EvalStateNode newState(Evaluator parentNode,
                                        MatchedEventMap beginState,
                                        PatternContext context,
                                        EvalStateNodeNumber stateNodeId)
    {
        if (expressionsArray == null) {
            expressionsArray = ExprNodeUtility.getEvaluators(expressions);
            this.context = context;
        }
        if (msecToExpire == null) {
            return new EvalEveryDistinctStateNode(parentNode, this, beginState);
        }
        else {
            return new EvalEveryDistinctStateExpireKeyNode(parentNode, this, beginState);
        }
    }

    public ExprEvaluator[] getExpressionsArray() {
        return expressionsArray;
    }

    public MatchedEventConvertor getConvertor() {
        return convertor;
    }

    public Long getMsecToExpire() {
        return msecToExpire;
    }

    public PatternContext getContext() {
        return context;
    }

    public final String toString()
    {
        return "EvalEveryNode children=" + this.getChildNodes().size();
    }

    /**
     * Returns expressions for distinct-value.
     * @return expressions
     */
    public List<ExprNode> getExpressions()
    {
        return expressions;
    }

    /**
     * Sets the convertor for matching events to events-per-stream.
     * @param convertor convertor
     */
    public void setConvertor(MatchedEventConvertor convertor)
    {
        this.convertor = convertor;
    }

    /**
     * Sets expressions for distinct-value.
     * @param expressions to set
     */
    public void setExpressions(List<ExprNode> expressions, Long msecToExpire)
    {
        this.expressions = expressions;
        this.msecToExpire = msecToExpire;
    }

    private static final Log log = LogFactory.getLog(EvalEveryNode.class);
}

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * This class represents a followed-by operator in the evaluation tree representing any event expressions.
 */
public class EvalFollowedByNode extends EvalNode
{
    private static final long serialVersionUID = -3535280879288655577L;

    private transient PatternContext context;
    private List<ExprNode> optionalMaxExpressions;
    protected boolean hasMax;
    private Integer[] cachedMaxPerChild;
    private transient ExprEvaluator[] cachedMaxEvaluatorPerChild;

    protected EvalFollowedByNode(List<ExprNode> optionalMaxExpressions) {
        this.optionalMaxExpressions = optionalMaxExpressions;
    }

    public EvalStateNode newState(Evaluator parentNode,
                                                 MatchedEventMap beginState,
                                                 PatternContext context,
                                                 EvalStateNodeNumber stateNodeId)
    {
        if (this.context == null) {
            initOptionalMaxCache();
            this.context = context;
        }
        if (!hasMax) {
            return new EvalFollowedByStateNode(parentNode, this, beginState);
        }
        else {
            return new EvalFollowedByWithMaxStateNode(parentNode, this, beginState);
        }
    }

    protected void initOptionalMaxCache() {
        if (optionalMaxExpressions != null) {
            for (ExprNode maxExpr : optionalMaxExpressions) {
                if (maxExpr != null) {
                    hasMax = true;
                    break;
                }
            }
        }

        if (!hasMax) {
            return;
        }

        cachedMaxPerChild = new Integer[this.getChildNodes().size() - 1];
        cachedMaxEvaluatorPerChild = new ExprEvaluator[this.getChildNodes().size() - 1];

        for (int i = 0; i < getChildNodes().size() - 1; i++) {
            if (optionalMaxExpressions.size() <= i) {
                continue;
            }
            ExprNode optionalMaxExpression = optionalMaxExpressions.get(i);
            if (optionalMaxExpression == null) {
                continue;
            }
            if (optionalMaxExpression.isConstantResult()) {
                Number result = (Number) optionalMaxExpression.getExprEvaluator().evaluate(null, true, null);
                if (result != null) {
                    cachedMaxPerChild[i] = result.intValue();
                }
            }
            else {
                cachedMaxEvaluatorPerChild[i] = optionalMaxExpressions.get(i).getExprEvaluator();
            }
        }
    }

    public int getMax(int position) {
        Integer cached = cachedMaxPerChild[position];
        if (cached != null) {
            return cached;  // constant value cached
        }

        ExprEvaluator cachedExpr = cachedMaxEvaluatorPerChild[position];
        if (cachedExpr == null) {
            return -1;  // no limit defined for this sub-expression
        }

        Number result = (Number) cachedExpr.evaluate(null, true, null);
        if (result != null) {
            return result.intValue();
        }
        return -1;  // no limit
    }

    public PatternContext getContext() {
        return context;
    }

    public List<ExprNode> getOptionalMaxExpressions() {
        return optionalMaxExpressions;
    }

    public void setOptionalMaxExpressions(List<ExprNode> optionalMaxExpressions) {
        this.optionalMaxExpressions = optionalMaxExpressions;
    }

    public final String toString()
    {
        return ("EvalFollowedByNode children=" + this.getChildNodes().size());
    }

    private static final Log log = LogFactory.getLog(EvalFollowedByNode.class);
}

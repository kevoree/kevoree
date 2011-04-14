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
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;

import java.util.List;
import java.util.Map;

/**
 * Represents an Dot-operator expression, for use when "(expression).method(...).method(...)"
 */
public class ExprDotNode extends ExprNode implements ExprEvaluator, ExprNodeInnerNodeProvider
{
    private static final long serialVersionUID = 8105121208330622813L;

    private transient ExprDotEval[] eval;
    private transient ExprEvaluator childEvaluator;

    private final List<ExprChainedSpec> chainSpec;
    private final boolean isDuckTyping;

    public ExprDotNode(List<ExprChainedSpec> chainSpec, boolean isDuckTyping)
    {
        this.chainSpec = chainSpec;
        this.isDuckTyping = isDuckTyping;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        // validate all parameters
        ExprNodeUtility.validate(chainSpec, streamTypeService, methodResolutionService, viewResourceDelegate, timeProvider, variableService, exprEvaluatorContext);

        Class inputType = this.getChildNodes().get(0).getExprEvaluator().getType();

        // get no-duck evaluation
        // this is always attempted first to ensure we can also be fully-typed.
        eval = ExprDotNodeUtility.getChainEvaluators(inputType, chainSpec, methodResolutionService, isDuckTyping);

        if ((eval == null) && (isDuckTyping)) {

            eval = new ExprDotEval[chainSpec.size()];

            int count = 0;
            for (ExprChainedSpec chain : chainSpec) {

                ExprEvaluator[] paramEvals = new ExprEvaluator[chain.getParameters().size()];
                Class[] paramTypes = new Class[chain.getParameters().size()];
                for (int i = 0; i < chain.getParameters().size(); i++) {
                    paramEvals[i] = chain.getParameters().get(i).getExprEvaluator();
                    paramTypes[i] = paramEvals[i].getType();
                }

                eval[count] = new ExprDotMethodEvalDuck(methodResolutionService, chain.getName(), paramTypes, paramEvals);
                count++;
            }
        }

        childEvaluator = this.getChildNodes().get(0).getExprEvaluator();
    }

    @Override
    public void accept(ExprNodeVisitor visitor) {
        super.accept(visitor);
        ExprNode.acceptChain(visitor, chainSpec);
    }

    @Override
    public void accept(ExprNodeVisitorWithParent visitor) {
        super.accept(visitor);
        ExprNode.acceptChain(visitor, chainSpec);
    }

    @Override
    protected void acceptChildnodes(ExprNodeVisitorWithParent visitor, ExprNode parent) {
        super.acceptChildnodes(visitor, parent);
        ExprNode.acceptChain(visitor, chainSpec, this);
    }

    @Override
    protected void replaceUnlistedChildNode(ExprNode nodeToReplace, ExprNode newNode) {
        ExprNode.replaceChainChildNode(nodeToReplace, newNode, chainSpec);
    }

    public List<ExprChainedSpec> getChainSpec()
    {
        return chainSpec;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return eval[eval.length - 1].getResultType();
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object inner = childEvaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (inner == null) {
            return null;
        }

        for (ExprDotEval methodEval : eval) {
            inner = methodEval.evaluate(inner, eventsPerStream, isNewData, exprEvaluatorContext);
            if (inner == null) {
                break;
            }
        }
        return inner;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
		buffer.append('(');
        buffer.append(this.getChildNodes().get(0).toExpressionString());
		buffer.append(")");
        ExprNodeUtility.toExpressionString(chainSpec, buffer);
		return buffer.toString();
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprDotNode))
        {
            return false;
        }

        ExprDotNode other = (ExprDotNode) node;
        if (other.chainSpec.size() != this.chainSpec.size()) {
            return false;
        }
        for (int i = 0; i < chainSpec.size(); i++) {
            if (!(this.chainSpec.get(i).equals(other.chainSpec.get(i)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ExprNode> getAdditionalNodes() {
        return ExprNodeUtility.collectChainParameters(chainSpec);
    }
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.CoercionException;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.SimpleNumberCoercerFactory;
import com.espertech.esper.util.SimpleNumberCoercer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents the case-when-then-else control flow function is an expression tree.
 */
public class ExprCaseNode extends ExprNode implements ExprEvaluator
{
    private static final long serialVersionUID = 792538321520346459L;

    private final boolean isCase2;
    private Class resultType;
    private boolean isNumericResult;
    private boolean mustCoerce;

    private transient SimpleNumberCoercer coercer;
    private transient List<UniformPair<ExprEvaluator>> whenThenNodeList;
    private transient ExprEvaluator optionalCompareExprNode;
    private transient ExprEvaluator optionalElseExprNode;

    /**
     * Ctor.
     * @param isCase2 is an indicator of which Case statement we are working on.
     * <p> True indicates a 'Case2' statement with syntax "case a when a1 then b1 else b2".
     * <p> False indicates a 'Case1' statement with syntax "case when a=a1 then b1 else b2".
     */
    public ExprCaseNode(boolean isCase2)
    {
        this.isCase2 = isCase2;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    /**
     * Returns true if this is a switch-type case.
     * @return true for switch-type case, or false for when-then type
     */
    public boolean isCase2()
    {
        return isCase2;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService_, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        CaseAnalysis analysis = analyzeCase();

        whenThenNodeList = new ArrayList<UniformPair<ExprEvaluator>>();
        for (UniformPair<ExprNode> pair : analysis.getWhenThenNodeList())
        {
            if (!isCase2) {
                if (pair.getFirst().getExprEvaluator().getType() != Boolean.class)
                {
                    throw new ExprValidationException("Case node 'when' expressions must return a boolean value");
                }
            }
            whenThenNodeList.add(new UniformPair<ExprEvaluator>(pair.getFirst().getExprEvaluator(), pair.getSecond().getExprEvaluator()));
        }
        if (analysis.getOptionalCompareExprNode() != null) {
            optionalCompareExprNode = analysis.getOptionalCompareExprNode().getExprEvaluator();
        }
        if (analysis.getOptionalElseExprNode() != null) {
            optionalElseExprNode = analysis.getOptionalElseExprNode().getExprEvaluator();
        }

        if (isCase2)
        {
            validateCaseTwo();
        }

        // Determine type of each result (then-node and else node) child node expression
        List<Class> childTypes = new LinkedList<Class>();
        for (UniformPair<ExprEvaluator> pair : whenThenNodeList)
        {
            childTypes.add(pair.getSecond().getType());
        }
        if (optionalElseExprNode != null)
        {
            childTypes.add(optionalElseExprNode.getType());
        }

        // Determine common denominator type
        try {
            resultType = JavaClassHelper.getCommonCoercionType(childTypes.toArray(new Class[childTypes.size()]));
            if (JavaClassHelper.isNumeric(resultType))
            {
                isNumericResult = true;
            }
        }
        catch (CoercionException ex)
        {
            throw new ExprValidationException("Implicit conversion not allowed: " + ex.getMessage());
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return resultType;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (!isCase2)
        {
            return evaluateCaseSyntax1(eventsPerStream, isNewData, exprEvaluatorContext);
        }
        else
        {
            return evaluateCaseSyntax2(eventsPerStream, isNewData, exprEvaluatorContext);
        }
    }

    public boolean equalsNode(ExprNode node_)
    {
        if (!(node_ instanceof ExprCaseNode))
        {
            return false;
        }

        ExprCaseNode otherExprCaseNode = (ExprCaseNode) node_;
        return this.isCase2 == otherExprCaseNode.isCase2;
    }

    public String toExpressionString()
    {
        CaseAnalysis analysis;
        try
        {
             analysis = analyzeCase();
        }
        catch (ExprValidationException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("case");
        if (isCase2)
        {
            buffer.append(' ');
            buffer.append(analysis.getOptionalCompareExprNode().toExpressionString());
        }
        for (UniformPair<ExprNode> p : analysis.getWhenThenNodeList())
        {
            buffer.append(" when ");
            buffer.append(p.getFirst().toExpressionString());
            buffer.append(" then ");
            buffer.append(p.getSecond().toExpressionString());
        }
        if (analysis.getOptionalElseExprNode() != null)
        {
            buffer.append(" else ");
            buffer.append(analysis.getOptionalElseExprNode().toExpressionString());
        }
        buffer.append(" end");
        return buffer.toString();
    }

    private CaseAnalysis analyzeCaseOne() throws ExprValidationException
    {
        // Case 1 expression example:
        //      case when a=b then x [when c=d then y...] [else y]
        //
        ExprNode[] children = this.getChildNodes().toArray(new ExprNode[this.getChildNodes().size()]);
        if (children.length < 2)
        {
            throw new ExprValidationException("Case node must have at least 2 child nodes");
        }

        List<UniformPair<ExprNode>> whenThenNodeList = new LinkedList<UniformPair<ExprNode>>();
        int numWhenThen = children.length >> 1;
        for (int i = 0; i < numWhenThen; i++)
        {
            ExprNode whenExpr = children[(i << 1)];
            ExprNode thenExpr = children[(i << 1) + 1];
            whenThenNodeList.add(new UniformPair<ExprNode>(whenExpr, thenExpr));
        }
        ExprNode optionalElseExprNode = null;
        if (children.length % 2 != 0)
        {
            optionalElseExprNode = children[children.length - 1];
        }
        return new CaseAnalysis(whenThenNodeList, null, optionalElseExprNode);
    }

    private CaseAnalysis analyzeCaseTwo() throws ExprValidationException {
        // Case 2 expression example:
        //      case p when p1 then x [when p2 then y...] [else z]
        //
        ExprNode[] children = this.getChildNodes().toArray(new ExprNode[this.getChildNodes().size()]);
        if (children.length < 3)
        {
            throw new ExprValidationException("Case node must have at least 3 child nodes");
        }

        ExprNode optionalCompareExprNode = children[0];

        List<UniformPair<ExprNode>> whenThenNodeList = new LinkedList<UniformPair<ExprNode>>();
        int numWhenThen = (children.length - 1) / 2;
        for (int i = 0; i < numWhenThen; i++)
        {
            whenThenNodeList.add(new UniformPair<ExprNode>(children[i * 2 + 1], children[i * 2 + 2]));
        }
        ExprNode optionalElseExprNode = null;
        if (numWhenThen * 2 + 1 < children.length)
        {
            optionalElseExprNode = children[children.length - 1];
        }
        return new CaseAnalysis(whenThenNodeList, optionalCompareExprNode, optionalElseExprNode);
    }

    private void validateCaseTwo() throws ExprValidationException
    {
        // validate we can compare result types
        List<Class> comparedTypes = new LinkedList<Class>();
        comparedTypes.add(optionalCompareExprNode.getType());
        for (UniformPair<ExprEvaluator> pair : whenThenNodeList)
        {
            comparedTypes.add(pair.getFirst().getType());
        }

        // Determine common denominator type
        try {
            Class coercionType = JavaClassHelper.getCommonCoercionType(comparedTypes.toArray(new Class[comparedTypes.size()]));

            // Determine if we need to coerce numbers when one type doesn't match any other type
            if (JavaClassHelper.isNumeric(coercionType))
            {
                mustCoerce = false;
                for (Class comparedType : comparedTypes)
                {
                    if (comparedType != coercionType)
                    {
                        mustCoerce = true;
                    }
                }
                if (mustCoerce)
                {
                    coercer = SimpleNumberCoercerFactory.getCoercer(null, coercionType);
                }
            }
        }
        catch (CoercionException ex)
        {
            throw new ExprValidationException("Implicit conversion not allowed: " + ex.getMessage());
        }
    }

    private Object evaluateCaseSyntax1(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        // Case 1 expression example:
        //      case when a=b then x [when c=d then y...] [else y]

        Object caseResult = null;
        boolean matched = false;
        for (UniformPair<ExprEvaluator> p : whenThenNodeList)
        {
            Boolean whenResult = (Boolean) p.getFirst().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            // If the 'when'-expression returns true
            if ((whenResult != null) && (whenResult))
            {
                caseResult = p.getSecond().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                matched = true;
                break;
            }
        }

        if ((!matched) && (optionalElseExprNode != null))
        {
            caseResult = optionalElseExprNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        }

        if (caseResult == null)
        {
            return null;
        }

        if ((caseResult.getClass() != resultType) && (isNumericResult))
        {
            return JavaClassHelper.coerceBoxed( (Number) caseResult, resultType);
        }
        return caseResult;
    }

    private Object evaluateCaseSyntax2(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        // Case 2 expression example:
        //      case p when p1 then x [when p2 then y...] [else z]

        Object checkResult = optionalCompareExprNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        Object caseResult = null;
        boolean matched = false;
        for (UniformPair<ExprEvaluator> p : whenThenNodeList)
        {
            Object whenResult = p.getFirst().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            if (compare(checkResult, whenResult)) {
                caseResult = p.getSecond().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                matched = true;
                break;
            }
        }

        if ((!matched) && (optionalElseExprNode != null))
        {
            caseResult = optionalElseExprNode.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        }

        if (caseResult == null)
        {
            return null;
        }

        if ((caseResult.getClass() != resultType) && (isNumericResult))
        {
            return JavaClassHelper.coerceBoxed( (Number) caseResult, resultType);
        }
        return caseResult;
    }

    private boolean compare(Object leftResult, Object rightResult)
    {
        if (leftResult == null)
        {
            return (rightResult == null);
        }
        if (rightResult == null)
        {
            return false;
        }

        if (!mustCoerce)
        {
            return leftResult.equals(rightResult);
        }
        else
        {
            Number left = coercer.coerceBoxed((Number) leftResult);
            Number right = coercer.coerceBoxed((Number) rightResult);
            return left.equals(right);
        }
    }

    private CaseAnalysis analyzeCase() throws ExprValidationException {
        if (isCase2)
        {
            return analyzeCaseTwo();
        }
        else
        {
            return analyzeCaseOne();
        }
    }

    public static class CaseAnalysis {
        private List<UniformPair<ExprNode>> whenThenNodeList;
        private ExprNode optionalCompareExprNode;
        private ExprNode optionalElseExprNode;

        public CaseAnalysis(List<UniformPair<ExprNode>> whenThenNodeList, ExprNode optionalCompareExprNode, ExprNode optionalElseExprNode)
        {
            this.whenThenNodeList = whenThenNodeList;
            this.optionalCompareExprNode = optionalCompareExprNode;
            this.optionalElseExprNode = optionalElseExprNode;
        }

        public List<UniformPair<ExprNode>> getWhenThenNodeList()
        {
            return whenThenNodeList;
        }

        public ExprNode getOptionalCompareExprNode()
        {
            return optionalCompareExprNode;
        }

        public ExprNode getOptionalElseExprNode()
        {
            return optionalElseExprNode;
        }
    }
}



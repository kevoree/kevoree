/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.type.OuterJoinType;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Contains the ON-clause criteria in an outer join.
 */
public class OuterJoinDesc implements MetaDefItem, Serializable
{
    private OuterJoinType outerJoinType;
    private ExprIdentNode leftNode;
    private ExprIdentNode rightNode;
    private ExprIdentNode[] addLeftNode;
    private ExprIdentNode[] addRightNode;
    private static final long serialVersionUID = -2616847070429124382L;

    /**
     * Ctor.
     * @param outerJoinType - type of the outer join
     * @param leftNode - left hand identifier node
     * @param rightNode - right hand identifier node
     * @param addLeftNode - additional optional left hand identifier nodes for the on-clause in a logical-and
     * @param addRightNode - additional optional right hand identifier nodes for the on-clause in a logical-and
     */
    public OuterJoinDesc(OuterJoinType outerJoinType, ExprIdentNode leftNode, ExprIdentNode rightNode, ExprIdentNode[] addLeftNode, ExprIdentNode[] addRightNode)
    {
        this.outerJoinType = outerJoinType;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.addLeftNode = addLeftNode;
        this.addRightNode = addRightNode;
    }

    /**
     * Returns the type of outer join (left/right/full).
     * @return outer join type
     */
    public OuterJoinType getOuterJoinType()
    {
        return outerJoinType;
    }

    /**
     * Returns left hand identifier node.
     * @return left hand
     */
    public ExprIdentNode getLeftNode()
    {
        return leftNode;
    }

    /**
     * Returns right hand identifier node.
     * @return right hand
     */
    public ExprIdentNode getRightNode()
    {
        return rightNode;
    }

    /**
     * Returns additional properties in the on-clause, if any, that are connected via logical-and
     * @return additional properties
     */
    public ExprIdentNode[] getAdditionalLeftNodes()
    {
        return addLeftNode;
    }

    /**
     * Returns additional properties in the on-clause, if any, that are connected via logical-and
     * @return additional properties
     */
    public ExprIdentNode[] getAdditionalRightNodes()
    {
        return addRightNode;
    }

    /**
     * Make an expression node that represents the outer join criteria as specified in the on-clause.
     * @param exprEvaluatorContext context for expression evalauation
     * @return expression node for outer join criteria
     */
    public ExprNode makeExprNode(ExprEvaluatorContext exprEvaluatorContext)
    {
        ExprNode representativeNode = new ExprEqualsNode(false);
        representativeNode.addChildNode(leftNode);
        representativeNode.addChildNode(rightNode);

        if (addLeftNode == null) {
            topValidate(representativeNode, exprEvaluatorContext);
            return representativeNode;
        }

        ExprAndNode andNode = new ExprAndNode();
        topValidate(representativeNode, exprEvaluatorContext);
        andNode.addChildNode(representativeNode);
        representativeNode = andNode;

        for (int i = 0; i < addLeftNode.length; i++)
        {
            ExprEqualsNode eqNode = new ExprEqualsNode(false);
            eqNode.addChildNode(addLeftNode[i]);
            eqNode.addChildNode(addRightNode[i]);
            topValidate(eqNode, exprEvaluatorContext);
            andNode.addChildNode(eqNode);
        }

        topValidate(andNode, exprEvaluatorContext);
        return representativeNode;
    }

    private void topValidate(ExprNode exprNode, ExprEvaluatorContext exprEvaluatorContext) {
        try
        {
            exprNode.validate(null, null, null, null, null, exprEvaluatorContext);
        }
        catch (ExprValidationException e)
        {
            throw new IllegalStateException("Failed to make representative node for outer join criteria");
        }
    }
}

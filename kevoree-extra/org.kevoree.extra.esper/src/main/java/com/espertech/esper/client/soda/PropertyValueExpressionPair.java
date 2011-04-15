package com.espertech.esper.client.soda;

import java.io.Serializable;

/**
 * Pair of expressions with "equals" operator between.
 */
public class PropertyValueExpressionPair implements Serializable {
    private static final long serialVersionUID = 2207038136736490910L;

    private Expression left;
    private Expression right;

    /**
     * Ctor.
     */
    public PropertyValueExpressionPair() {
    }

    /**
     * Ctor.
     * @param left expression
     * @param right expression
     */
    public PropertyValueExpressionPair(PropertyValueExpression left, PropertyValueExpression right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns left expr.
     * @return left
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * Sets left expr.
     * @param left left
     */
    public void setLeft(Expression left) {
        this.left = left;
    }

    /**
     * Returns right side.
     * @return right side
     */
    public Expression getRight() {
        return right;
    }

    /**
     * Sets right side.
     * @param right to set
     */
    public void setRight(Expression right) {
        this.right = right;
    }
}

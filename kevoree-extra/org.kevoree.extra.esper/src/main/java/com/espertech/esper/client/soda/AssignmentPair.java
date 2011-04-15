package com.espertech.esper.client.soda;

import java.io.Serializable;

/**
 * An assignment to a variable or property name of an expression value.
 */
public class AssignmentPair implements Serializable {
    private static final long serialVersionUID = -2713092091207302856L;
    
    private String name;
    private Expression value;

    /**
     * Ctor.
     */
    public AssignmentPair() {
    }

    /**
     * Ctor.
     * @param name property or variable
     * @param value value to assign
     */
    public AssignmentPair(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns property or variable name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets property or variable name.
     * @param name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns expression to eval.
     * @return eval expression
     */
    public Expression getValue() {
        return value;
    }

    /**
     * Sets expression to eval.
     * @param value expression
     */
    public void setValue(Expression value) {
        this.value = value;
    }
}

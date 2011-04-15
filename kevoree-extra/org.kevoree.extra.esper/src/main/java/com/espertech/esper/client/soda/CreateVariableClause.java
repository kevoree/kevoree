/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.Serializable;
import java.io.StringWriter;

/**
 * Represents a create-variable syntax for creating a new variable.
 */
public class CreateVariableClause implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String variableType;
    private String variableName;
    private Expression optionalAssignment;

    /**
     * Ctor.
     */
    public CreateVariableClause() {
    }

    /**
     * Creates a create-variable syntax for declaring a variable.
     * @param variableType is the variable type name
     * @param variableName is the name of the variable
     * @return create-variable clause
     */
    public static CreateVariableClause create(String variableType, String variableName)
    {
        return new CreateVariableClause(variableType, variableName, null);
    }

    /**
     * Creates a create-variable syntax for declaring a variable.
     * @param variableType is the variable type name
     * @param variableName is the name of the variable
     * @param expression is the assignment expression supplying the initial value
     * @return create-variable clause
     */
    public static CreateVariableClause create(String variableType, String variableName, Expression expression)
    {
        return new CreateVariableClause(variableType, variableName, expression);
    }

    /**
     * Ctor.
     * @param variableType is the variable type name
     * @param variableName is the name of the variable
     * @param optionalAssignment is the optional assignment expression supplying the initial value, or null if the
     * initial value is null
     */
    public CreateVariableClause(String variableType, String variableName, Expression optionalAssignment)
    {
        this.variableType = variableType;
        this.variableName = variableName;
        this.optionalAssignment = optionalAssignment;
    }

    /**
     * Returns the variable type name.
     * @return type of the variable
     */
    public String getVariableType()
    {
        return variableType;
    }

    /**
     * Sets the variable type name.
     * @param variableType type of the variable
     */
    public void setVariableType(String variableType)
    {
        this.variableType = variableType;
    }

    /**
     * Returns the variable name.
     * @return name of the variable
     */
    public String getVariableName()
    {
        return variableName;
    }

    /**
     * Sets the variable name
     * @param variableName name of the variable
     */
    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    /**
     * Returns the optional assignment expression, or null to initialize to a null value
     * @return assignment expression, if present
     */
    public Expression getOptionalAssignment()
    {
        return optionalAssignment;
    }

    /**
     * Sets the optional assignment expression, or null to initialize to a null value
     * @param optionalAssignment assignment expression, if present
     */
    public void setOptionalAssignment(Expression optionalAssignment)
    {
        this.optionalAssignment = optionalAssignment;
    }

    /**
     * Render as EPL.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.append("create variable ");
        writer.append(variableType);
        writer.append(" ");
        writer.append(variableName);
        if (optionalAssignment != null)
        {
            writer.append(" = ");
            optionalAssignment.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        }
    }
}

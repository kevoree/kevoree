/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.util.MetaDefItem;
import com.espertech.esper.epl.expression.ExprNode;

import java.io.Serializable;

/**
 * Descriptor for create-variable statements.
 */
public class CreateVariableDesc implements MetaDefItem, Serializable
{
    private String variableType;
    private String variableName;
    private ExprNode assignment;
    private static final long serialVersionUID = -7864602464816397227L;

    /**
     * Ctor.
     * @param variableType type of the variable
     * @param variableName name of the variable
     * @param assignment expression assigning the initial value, or null if none
     */
    public CreateVariableDesc(String variableType, String variableName, ExprNode assignment)
    {
        this.variableType = variableType;
        this.variableName = variableName;
        this.assignment = assignment;
    }

    /**
     * Returns the variable type.
     * @return type of variable
     */
    public String getVariableType()
    {
        return variableType;
    }

    /**
     * Returns the variable name
     * @return name
     */
    public String getVariableName()
    {
        return variableName;
    }

    /**
     * Returns the assignment expression, or null if none
     * @return expression or null
     */
    public ExprNode getAssignment()
    {
        return assignment;
    }
}

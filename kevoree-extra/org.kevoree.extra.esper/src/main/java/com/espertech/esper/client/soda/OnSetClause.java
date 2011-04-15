/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A clause to assign new values to variables based on a triggering event arriving.
 */
public class OnSetClause extends OnClause
{
    private static final long serialVersionUID = 0L;

    private List<AssignmentPair> assignments;

    /**
     * Creates a new on-set clause for setting variables, and adds a variable to set.
     * @param variableName is the variable name
     * @param expression is the assignment expression providing the new variable value
     * @return on-set clause
     */
    public static OnSetClause create(String variableName, Expression expression)
    {
        OnSetClause clause = new OnSetClause();
        clause.addAssignment(variableName, expression);
        return clause;
    }

    /**
     * Ctor.
     */
    public OnSetClause()
    {
        assignments = new ArrayList<AssignmentPair>();
    }

    /**
     * Adds a variable to set to the clause.
     * @param variable to set
     * @param expression expression providing the new variable value
     * @return clause
     */
    public OnSetClause addAssignment(String variable, Expression expression)
    {
        assignments.add(new AssignmentPair(variable, expression));
        return this;
    }

    /**
     * Returns the list of variable assignments.
     * @return pair of variable name and expression
     */
    public List<AssignmentPair> getAssignments()
    {
        return assignments;
    }

    /**
     * Sets a list of variable assignments.
     * @param assignments list of pairs of variable name and expression
     */
    public void setAssignments(List<AssignmentPair> assignments)
    {
        this.assignments = assignments;
    }

    /**
     * Renders the clause in EPL.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.write(" set ");
        String delimiter = "";
        for (AssignmentPair pair : assignments)
        {
            writer.write(delimiter);
            writer.write(pair.getName());
            writer.write(" = ");
            pair.getValue().toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
            delimiter = ", ";
        }
    }
}

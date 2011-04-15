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
 * A clause to update a named window based on a triggering event arriving and correlated to the named window events to be updated.
 */
public class OnUpdateClause extends OnClause
{
    private static final long serialVersionUID = 0L;

    private String windowName;
    private String optionalAsName;
    private List<AssignmentPair> assignments;

    /**
     * Ctor.
     */
    public OnUpdateClause() {
        assignments = new ArrayList<AssignmentPair>();
    }

    /**
     * Creates an on-update clause.
     * @param windowName is the named window name
     * @param optionalAsName is the optional as-provided name
     * @return on-update clause without assignments
     */
    public static OnUpdateClause create(String windowName, String optionalAsName)
    {
        return new OnUpdateClause(windowName, optionalAsName);
    }

    /**
     * Ctor.
     * @param windowName is the named window name
     * @param optionalAsName is the as-provided name of the named window
     */
    public OnUpdateClause(String windowName, String optionalAsName)
    {
        this.windowName = windowName;
        this.optionalAsName = optionalAsName;
        assignments = new ArrayList<AssignmentPair>();
    }

    /**
     * Renders the clause in textual representation.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.write(windowName);
        if (optionalAsName != null)
        {
            writer.write(" as ");
            writer.write(optionalAsName);
        }

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

    /**
     * Returns the name of the named window to update.
     * @return named window name
     */
    public String getWindowName()
    {
        return windowName;
    }

    /**
     * Sets the name of the named window.
     * @param windowName window name
     */
    public void setWindowName(String windowName)
    {
        this.windowName = windowName;
    }

    /**
     * Returns the as-provided name for the named window.
     * @return name or null
     */
    public String getOptionalAsName()
    {
        return optionalAsName;
    }

    /**
     * Sets the as-provided for the named window.
     * @param optionalAsName name to set for window
     */
    public void setOptionalAsName(String optionalAsName)
    {
        this.optionalAsName = optionalAsName;
    }

    /**
     * Adds a variable to set to the clause.
     * @param variable to set
     * @param expression expression providing the new variable value
     * @return clause
     */
    public OnUpdateClause addAssignment(String variable, Expression expression)
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
}
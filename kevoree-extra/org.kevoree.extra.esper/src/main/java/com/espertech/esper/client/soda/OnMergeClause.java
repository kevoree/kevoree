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
 * A clause to insert, update or delete to/from a named window based on a triggering event arriving and correlated to the named window events to be updated.
 */
public class OnMergeClause extends OnClause
{
    private static final long serialVersionUID = 0L;

    private String windowName;
    private String optionalAsName;
    private List<OnMergeMatchedAction> actions;

    /**
     * Ctor.
     */
    public OnMergeClause() {
        actions = new ArrayList<OnMergeMatchedAction>();
    }

    /**
     * Creates an on-update clause.
     * @param windowName is the named window name
     * @param optionalAsName is the optional as-provided name
     * @return on-update clause without assignments
     */
    public static OnMergeClause create(String windowName, String optionalAsName)
    {
        return new OnMergeClause(windowName, optionalAsName);
    }

    /**
     * Ctor.
     * @param windowName is the named window name
     * @param optionalAsName is the as-provided name of the named window
     */
    public OnMergeClause(String windowName, String optionalAsName)
    {
        this.windowName = windowName;
        this.optionalAsName = optionalAsName;
        actions = new ArrayList<OnMergeMatchedAction>();
    }

    /**
     * Renders the clause in textual representation.
     * @param writer to output to
     * @param optionalWhereClause where clause if present, or null
     */
    public void toEPL(StringWriter writer, Expression optionalWhereClause)
    {
        writer.write(" merge ");
        writer.write(windowName);
        if (optionalAsName != null)
        {
            writer.write(" as ");
            writer.write(optionalAsName);
        }
        if (optionalWhereClause != null)
        {
            writer.write(" where ");
            optionalWhereClause.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
            writer.write(" ");
        }
        for (OnMergeMatchedAction action : actions) {
            action.toEPL(writer);
            writer.write(" ");
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
     * Add a new action to the list of actions.
     * @param action to add
     * @return clause
     */
    public OnMergeClause addAction(OnMergeMatchedAction action)
    {
        actions.add(action);
        return this;
    }

    /**
     * Returns all actions.
     * @return actions
     */
    public List<OnMergeMatchedAction> getActions() {
        return actions;
    }

    /**
     * Sets all actions.
     * @param actions to set
     */
    public void setActions(List<OnMergeMatchedAction> actions) {
        this.actions = actions;
    }
}
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
import java.util.Collections;
import java.util.List;

/**
 * For use with on-merge clauses, inserts into a named window if matching rows are not found.
 */
public class OnMergeMatchedInsertAction implements OnMergeMatchedAction
{
    private static final long serialVersionUID = 0L;

    private List<String> columnNames = Collections.emptyList();
    private List<SelectClauseElement> selectList = Collections.emptyList();
    private Expression optionalCondition;

    /**
     * Ctor.
     * @param columnNames insert-into column names, or empty list if none provided
     * @param selectList select expression list
     * @param optionalCondition optional condition or null
     */
    public OnMergeMatchedInsertAction(List<String> columnNames, List<SelectClauseElement> selectList, Expression optionalCondition) {
        this.columnNames = columnNames;
        this.selectList = selectList;
        this.optionalCondition = optionalCondition;
    }

    /**
     * Ctor.
     */
    public OnMergeMatchedInsertAction() {
    }

    /**
     * Returns the action condition, or null if undefined.
     * @return condition
     */
    public Expression getOptionalCondition() {
        return optionalCondition;
    }

    /**
     * Sets the action condition, or null if undefined.
     * @param optionalCondition to set, or null to remove the condition
     */
    public void setOptionalCondition(Expression optionalCondition) {
        this.optionalCondition = optionalCondition;
    }

    /**
     * Returns the insert-into column names, if provided.
     * @return column names
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Sets the insert-into column names, can be empty list.
     * @param columnNames column names to set
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Returns the select expressions.
     * @return expression list
     */
    public List<SelectClauseElement> getSelectList() {
        return selectList;
    }

    /**
     * Sets the select expressions.
     * @param selectList expression list
     */
    public void setSelectList(List<SelectClauseElement> selectList) {
        this.selectList = selectList;
    }

    @Override
    public void toEPL(StringWriter writer) {
        writer.write("when not matched");

        if (optionalCondition != null) {
            writer.write(" and ");
            optionalCondition.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        }
        writer.write(" then insert");

        if (columnNames.size() > 0)
        {
            writer.write("(");
            String delimiter = "";
            for (String name : columnNames)
            {
                writer.write(delimiter);
                writer.write(name);
                delimiter = ", ";
            }
            writer.write(")");
        }
        writer.write(" select ");
        String delimiter = "";
        for (SelectClauseElement element : selectList)
        {
            writer.write(delimiter);
            element.toEPLElement(writer);
            delimiter = ", ";
        }
    }
}
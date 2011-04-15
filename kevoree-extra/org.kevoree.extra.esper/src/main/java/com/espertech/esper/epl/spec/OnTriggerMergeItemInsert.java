/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.expression.ExprNode;

import java.util.List;

/**
 * Specification for the merge statement insert-part.
 */
public class OnTriggerMergeItemInsert extends OnTriggerMergeItem
{
    private static final long serialVersionUID = -657179063417985357L;
    
    private final List<String> columns;
    private final List<SelectClauseElementRaw> selectClause;
    private transient List<SelectClauseElementCompiled> selectClauseCompiled;

    public OnTriggerMergeItemInsert(ExprNode optionalMatchCond, List<String> columns, List<SelectClauseElementRaw> selectClause) {
        super(optionalMatchCond);
        this.columns = columns;
        this.selectClause = selectClause;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<SelectClauseElementRaw> getSelectClause() {
        return selectClause;
    }

    public void setSelectClauseCompiled(List<SelectClauseElementCompiled> selectClauseCompiled) {
        this.selectClauseCompiled = selectClauseCompiled;
    }

    public List<SelectClauseElementCompiled> getSelectClauseCompiled() {
        return selectClauseCompiled;
    }
}


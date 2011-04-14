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
import com.espertech.esper.epl.expression.ExprSubselectNode;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Specification object representing a complete EPL statement including all EPL constructs.
 */
public class StatementSpecCompiled
{
    private final OnTriggerDesc onTriggerDesc;
    private final CreateWindowDesc createWindowDesc;
    private final CreateIndexDesc createIndexDesc;
    private final CreateVariableDesc createVariableDesc;
    private final CreateSchemaDesc createSchemaDesc;
    private InsertIntoDesc insertIntoDesc;
    private SelectClauseStreamSelectorEnum selectStreamDirEnum;
    private SelectClauseSpecCompiled selectClauseSpec;
    private final List<StreamSpecCompiled> streamSpecs;
    private final List<OuterJoinDesc> outerJoinDescList;
    private ExprNode filterExprRootNode;
    private final List<ExprNode> groupByExpressions;
    private final ExprNode havingExprRootNode;
    private final OutputLimitSpec outputLimitSpec;
    private final List<OrderByItem> orderByList;
    private final List<ExprSubselectNode> subSelectExpressions;
    private final Set<String> variableReferences;
    private final RowLimitSpec rowLimitSpec;
    private final Set<String> eventTypeReferences;
    private final Annotation[] annotations;
    private final UpdateDesc updateSpec;
    private final MatchRecognizeSpec matchRecognizeSpec;
    private final ForClauseSpec forClauseSpec;
    private final Map<Integer, List<ExprNode>> sqlParameters;

    /**
     * Ctor.
     * @param insertIntoDesc insert into def
     * @param selectClauseStreamSelectorEnum stream selection
     * @param selectClauseSpec select clause
     * @param streamSpecs specs for streams
     * @param outerJoinDescList outer join def
     * @param filterExprRootNode where filter expr nodes
     * @param groupByExpressions group by expression
     * @param havingExprRootNode having expression
     * @param outputLimitSpec output limit
     * @param orderByList order by
     * @param subSelectExpressions list of subqueries
     * @param onTriggerDesc describes on-delete statements
     * @param createWindowDesc describes create-window statements
     * @param createVariableDesc describes create-variable statements
     * @param rowLimitSpec row limit specification, or null if none supplied
     * @param eventTypeReferences event type names statically determined
     * @param annotations statement annotations
     * @param updateSpec update specification if used
     * @param matchRecognizeSpec if provided
     * @param variableReferences variables referenced
     * @param createIndexDesc when an index get
     */
    public StatementSpecCompiled(OnTriggerDesc onTriggerDesc,
                                 CreateWindowDesc createWindowDesc,
                                 CreateIndexDesc createIndexDesc,
                                 CreateVariableDesc createVariableDesc,
                                 CreateSchemaDesc createSchemaDesc,
                                 InsertIntoDesc insertIntoDesc,
                                 SelectClauseStreamSelectorEnum selectClauseStreamSelectorEnum,
                                 SelectClauseSpecCompiled selectClauseSpec,
                                 List<StreamSpecCompiled> streamSpecs,
                                 List<OuterJoinDesc> outerJoinDescList,
                                 ExprNode filterExprRootNode,
                                 List<ExprNode> groupByExpressions,
                                 ExprNode havingExprRootNode,
                                 OutputLimitSpec outputLimitSpec,
                                 List<OrderByItem> orderByList,
                                 List<ExprSubselectNode> subSelectExpressions,
                                 Set<String> variableReferences,
                                 RowLimitSpec rowLimitSpec,
                                 Set<String> eventTypeReferences,
                                 Annotation[] annotations,
                                 UpdateDesc updateSpec,
                                 MatchRecognizeSpec matchRecognizeSpec,
                                 ForClauseSpec forClauseSpec,
                                 Map<Integer, List<ExprNode>> sqlParameters)
    {
        this.onTriggerDesc = onTriggerDesc;
        this.createWindowDesc = createWindowDesc;
        this.createIndexDesc = createIndexDesc;
        this.createVariableDesc = createVariableDesc;
        this.createSchemaDesc = createSchemaDesc;
        this.insertIntoDesc = insertIntoDesc;
        this.selectStreamDirEnum = selectClauseStreamSelectorEnum;
        this.selectClauseSpec = selectClauseSpec;
        this.streamSpecs = streamSpecs;
        this.outerJoinDescList = outerJoinDescList;
        this.filterExprRootNode = filterExprRootNode;
        this.groupByExpressions = groupByExpressions;
        this.havingExprRootNode = havingExprRootNode;
        this.outputLimitSpec = outputLimitSpec;
        this.orderByList = orderByList;
        this.subSelectExpressions = subSelectExpressions;
        this.variableReferences = variableReferences;
        this.rowLimitSpec = rowLimitSpec;
        this.eventTypeReferences = eventTypeReferences;
        this.annotations = annotations;
        this.updateSpec = updateSpec;
        this.matchRecognizeSpec = matchRecognizeSpec;
        this.forClauseSpec = forClauseSpec;
        this.sqlParameters = sqlParameters;
    }

    /**
     * Ctor.
     */
    public StatementSpecCompiled()
    {
        onTriggerDesc = null;
        createWindowDesc = null;
        createIndexDesc = null;
        createVariableDesc = null;
        createSchemaDesc = null;
        insertIntoDesc = null;
        selectStreamDirEnum = SelectClauseStreamSelectorEnum.RSTREAM_ISTREAM_BOTH;
        selectClauseSpec = new SelectClauseSpecCompiled(false);
        streamSpecs = new ArrayList<StreamSpecCompiled>();
        outerJoinDescList = new ArrayList<OuterJoinDesc>();
        filterExprRootNode = null;
        groupByExpressions = new ArrayList<ExprNode>();
        havingExprRootNode = null;
        outputLimitSpec = null;
        orderByList = new ArrayList<OrderByItem>();
        subSelectExpressions = new ArrayList<ExprSubselectNode>();
        variableReferences = new HashSet<String>();
        rowLimitSpec = null;
        eventTypeReferences = new HashSet<String>();
        annotations = new Annotation[0];
        updateSpec = null;
        matchRecognizeSpec = null;
        forClauseSpec = null;
        sqlParameters = null;
    }

    /**
     * Returns the specification for an create-window statement.
     * @return create-window spec, or null if not such a statement
     */
    public CreateWindowDesc getCreateWindowDesc()
    {
        return createWindowDesc;
    }

    /**
     * Returns the create-variable statement descriptor.
     * @return create-variable spec
     */
    public CreateVariableDesc getCreateVariableDesc()
    {
        return createVariableDesc;
    }

    /**
     * Returns the FROM-clause stream definitions.
     * @return list of stream specifications
     */
    public List<StreamSpecCompiled> getStreamSpecs()
    {
        return streamSpecs;
    }

    /**
     * Returns SELECT-clause list of expressions.
     * @return list of expressions and optional name
     */
    public SelectClauseSpecCompiled getSelectClauseSpec()
    {
        return selectClauseSpec;
    }

    /**
     * Returns the WHERE-clause root node of filter expression.
     * @return filter expression root node
     */
    public ExprNode getFilterRootNode()
    {
        return filterExprRootNode;
    }

    /**
     * Returns the LEFT/RIGHT/FULL OUTER JOIN-type and property name descriptor, if applicable. Returns null if regular join.
     * @return outer join type, stream names and property names
     */
    public List<OuterJoinDesc> getOuterJoinDescList()
    {
        return outerJoinDescList;
    }

    /**
     * Returns list of group-by expressions.
     * @return group-by expression nodes as specified in group-by clause
     */
    public List<ExprNode> getGroupByExpressions()
    {
        return groupByExpressions;
    }

    /**
     * Returns expression root node representing the having-clause, if present, or null if no having clause was supplied.
     * @return having-clause expression top node
     */
    public ExprNode getHavingExprRootNode()
    {
        return havingExprRootNode;
    }

    /**
     * Returns the output limit definition, if any.
     * @return output limit spec
     */
    public OutputLimitSpec getOutputLimitSpec()
    {
        return outputLimitSpec;
    }

    /**
     * Return a descriptor with the insert-into event name and optional list of columns.
     * @return insert into specification
     */
    public InsertIntoDesc getInsertIntoDesc()
    {
        return insertIntoDesc;
    }

    /**
     * Returns the list of order-by expression as specified in the ORDER BY clause.
     * @return Returns the orderByList.
     */
    public List<OrderByItem> getOrderByList() {
        return orderByList;
    }

    /**
     * Returns the stream selector (rstream/istream).
     * @return stream selector
     */
    public SelectClauseStreamSelectorEnum getSelectStreamSelectorEnum()
    {
        return selectStreamDirEnum;
    }

    /**
     * Set the where clause filter node.
     * @param optionalFilterNode is the where-clause filter node
     */
    public void setFilterExprRootNode(ExprNode optionalFilterNode)
    {
        filterExprRootNode = optionalFilterNode;
    }

    /**
     * Returns the list of lookup expression nodes.
     * @return lookup nodes
     */
    public List<ExprSubselectNode> getSubSelectExpressions()
    {
        return subSelectExpressions;
    }

    /**
     * Returns the specification for an on-delete or on-select statement.
     * @return on-trigger spec, or null if not such a statement
     */
    public OnTriggerDesc getOnTriggerDesc()
    {
        return onTriggerDesc;
    }

    /**
     * Returns true to indicate the statement has variables.
     * @return true for statements that use variables
     */
    public boolean isHasVariables()
    {
        return variableReferences != null && !variableReferences.isEmpty();
    }

    /**
     * Sets the stream selection.
     * @param selectStreamDirEnum stream selection
     */
    public void setSelectStreamDirEnum(SelectClauseStreamSelectorEnum selectStreamDirEnum) {
        this.selectStreamDirEnum = selectStreamDirEnum;
    }

    /**
     * Returns the row limit specification, or null if none supplied.
     * @return row limit spec if any
     */
    public RowLimitSpec getRowLimitSpec()
    {
        return rowLimitSpec;
    }

    /**
     * Returns the event type name in used by the statement.
     * @return set of event type name
     */
    public Set<String> getEventTypeReferences()
    {
        return eventTypeReferences;
    }

    /**
     * Returns annotations or empty array if none.
     * @return annotations
     */
    public Annotation[] getAnnotations()
    {
        return annotations;
    }

    /**
     * Sets the insert-into clause.
     * @param insertIntoDesc insert-into clause.
     */
    public void setInsertIntoDesc(InsertIntoDesc insertIntoDesc)
    {
        this.insertIntoDesc = insertIntoDesc;
    }

    /**
     * Sets the select clause.
     * @param selectClauseSpec select clause
     */
    public void setSelectClauseSpec(SelectClauseSpecCompiled selectClauseSpec)
    {
        this.selectClauseSpec = selectClauseSpec;
    }

    /**
     * Returns the update spec if update clause is used.
     * @return update desc
     */
    public UpdateDesc getUpdateSpec()
    {
        return updateSpec;
    }

    /**
     * Returns the match recognize spec, if used
     * @return match recognize spec
     */
    public MatchRecognizeSpec getMatchRecognizeSpec() {
        return matchRecognizeSpec;
    }

    /**
     * Return variables referenced.
     * @return variables
     */
    public Set<String> getVariableReferences() {
        return variableReferences;
    }

    /**
     * Returns create index
     * @return create index
     */
    public CreateIndexDesc getCreateIndexDesc()
    {
        return createIndexDesc;
    }

    public CreateSchemaDesc getCreateSchemaDesc()
    {
        return createSchemaDesc;
    }

    public ForClauseSpec getForClauseSpec()
    {
        return forClauseSpec;
    }

    public Map<Integer, List<ExprNode>> getSqlParameters()
    {
        return sqlParameters;
    }
}

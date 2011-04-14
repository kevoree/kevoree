/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.parse;

import com.espertech.esper.antlr.ASTUtil;
import com.espertech.esper.client.ConfigurationInformation;
import com.espertech.esper.client.EPException;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.EPAdministratorHelper;
import com.espertech.esper.epl.agg.AggregationAccessType;
import com.espertech.esper.epl.agg.AggregationSupport;
import com.espertech.esper.epl.core.EngineImportException;
import com.espertech.esper.epl.core.EngineImportService;
import com.espertech.esper.epl.core.EngineImportUndefinedException;
import com.espertech.esper.epl.core.StreamTypeServiceImpl;
import com.espertech.esper.epl.db.DatabasePollingViewableFactory;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.generated.EsperEPL2Ast;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.pattern.*;
import com.espertech.esper.pattern.guard.GuardEnum;
import com.espertech.esper.rowregex.*;
import com.espertech.esper.schedule.SchedulingService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.type.*;
import com.espertech.esper.type.StringValue;
import com.espertech.esper.util.PlaceholderParseException;
import com.espertech.esper.util.PlaceholderParser;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Called during the walks of a EPL expression AST tree as specified in the grammar file.
 * Constructs filter and view specifications etc.
 */
public class EPLTreeWalker extends EsperEPL2Ast
{
    // private holding areas for accumulated info
    private Map<Tree, ExprNode> astExprNodeMap = new HashMap<Tree, ExprNode>();
    private final Stack<Map<Tree, ExprNode>> astExprNodeMapStack;

    private final Map<Tree, EvalNode> astPatternNodeMap = new HashMap<Tree, EvalNode>();

    private final Map<Tree, RowRegexExprNode> astRowRegexNodeMap = new HashMap<Tree, RowRegexExprNode>();

    private FilterSpecRaw filterSpec;
    private final List<ViewSpec> viewSpecs = new LinkedList<ViewSpec>();

    // AST Walk result
    private List<ExprSubstitutionNode> substitutionParamNodes = new ArrayList<ExprSubstitutionNode>();
    private StatementSpecRaw statementSpec;
    private final Stack<StatementSpecRaw> statementSpecStack;

    private List<SelectClauseElementRaw> propertySelectRaw;
    private PropertyEvalSpec propertyEvalSpec;
    private List<OnTriggerMergeItem> mergeInstructions;

    private final EngineImportService engineImportService;
    private final VariableService variableService;
    private final TimeProvider timeProvider;
    private final ExprEvaluatorContext exprEvaluatorContext;
    private final SelectClauseStreamSelectorEnum defaultStreamSelector;
    private final String engineURI;
    private final ConfigurationInformation configurationInformation;
    private final SchedulingService schedulingService;
    private final PatternNodeFactory patternNodeFactory;

    /**
     * Ctor.
     * @param engineImportService is required to resolve lib-calls into static methods or configured aggregation functions
     * @param variableService for variable access
     * @param input is the tree nodes to walk
     * @param defaultStreamSelector - the configuration for which insert or remove streams (or both) to produce
     * @param engineURI engine URI
     * @param configurationInformation configuration info
     */
    public EPLTreeWalker(TreeNodeStream input,
                         EngineImportService engineImportService,
                         VariableService variableService,
                         SchedulingService schedulingService,
                         SelectClauseStreamSelectorEnum defaultStreamSelector,
                         String engineURI,
                         ConfigurationInformation configurationInformation,
                         PatternNodeFactory patternNodeFactory)
    {
        super(input);
        this.engineImportService = engineImportService;
        this.variableService = variableService;
        this.defaultStreamSelector = defaultStreamSelector;
        this.timeProvider = schedulingService;
        this.patternNodeFactory = patternNodeFactory;

        exprEvaluatorContext = new ExprEvaluatorContext()
        {
            public TimeProvider getTimeProvider()
            {
                return timeProvider;
            }
        };
        this.engineURI = engineURI;
        this.configurationInformation = configurationInformation;
        this.schedulingService = schedulingService;

        if (defaultStreamSelector == null)
        {
            throw new IllegalArgumentException("Default stream selector is null");
        }

        statementSpec = new StatementSpecRaw(defaultStreamSelector);
        statementSpecStack = new Stack<StatementSpecRaw>();
        astExprNodeMapStack = new Stack<Map<Tree, ExprNode>>();
    }

    /**
     * Pushes a statement into the stack, creating a new empty statement to fill in.
     * The leave node method for lookup statements pops from the stack.
     */
    protected void pushStmtContext() {
        if (log.isDebugEnabled())
        {
            log.debug(".pushStmtContext");
        }
        statementSpecStack.push(statementSpec);
        astExprNodeMapStack.push(astExprNodeMap);

        statementSpec = new StatementSpecRaw(defaultStreamSelector);
        astExprNodeMap = new HashMap<Tree, ExprNode>();
    }

    /**
     * Returns statement specification.
     * @return statement spec.
     */
    public StatementSpecRaw getStatementSpec()
    {
        return statementSpec;
    }

    /**
     * Leave AST node and process it's type and child nodes.
     * @param node is the node to complete
     * @throws ASTWalkException if the node tree walk operation failed
     */
    protected void leaveNode(Tree node) throws ASTWalkException
    {
        if (log.isDebugEnabled())
        {
            log.debug(".leaveNode " + node);
        }

        switch (node.getType())
        {
            case STREAM_EXPR:
                leaveStreamExpr(node);
                break;
            case EVENT_FILTER_EXPR:
                leaveStreamFilter(node);
                break;
            case PATTERN_FILTER_EXPR:
                leavePatternFilter(node);
                break;
            case PATTERN_INCL_EXPR:
                return;
            case VIEW_EXPR:
                leaveView(node);
                break;
            case SELECTION_EXPR:
                leaveSelectClause(node);
                break;
            case WILDCARD_SELECT:
            	leaveWildcardSelect();
            	break;
            case SELECTION_ELEMENT_EXPR:
                leaveSelectionElement(node);
                break;
            case SELECTION_STREAM:
                leaveSelectionStream(node);
                break;
            case PROPERTY_SELECTION_ELEMENT_EXPR:
                leavePropertySelectionElement(node);
                break;
            case PROPERTY_SELECTION_STREAM:
                leavePropertySelectionStream(node);
                break;
            case PROPERTY_WILDCARD_SELECT:
            	leavePropertyWildcardSelect();
            	break;
            case EVENT_FILTER_PROPERTY_EXPR_ATOM:
            	leavePropertySelectAtom(node);
            	break;
            case EVENT_PROP_EXPR:
                leaveEventPropertyExpr(node);
                break;
            case EVAL_AND_EXPR:
                leaveJoinAndExpr(node);
                break;
            case EVAL_OR_EXPR:
                leaveJoinOrExpr(node);
                break;
            case EVAL_EQUALS_EXPR:
            case EVAL_NOTEQUALS_EXPR:
                leaveEqualsExpr(node);
                break;
            case EVAL_EQUALS_GROUP_EXPR:
            case EVAL_NOTEQUALS_GROUP_EXPR:
                leaveEqualsGroupExpr(node);
                break;
            case WHERE_EXPR:
                leaveWhereClause();
                break;
            case NUM_INT:
            case INT_TYPE:
            case LONG_TYPE:
            case BOOL_TYPE:
            case FLOAT_TYPE:
            case DOUBLE_TYPE:
            case STRING_TYPE:
            case NULL_TYPE:
                leaveConstant(node);
                break;
            case SUBSTITUTION:
                leaveSubstitution(node);
                break;
            case STAR:
            case MINUS:
            case PLUS:
            case DIV:
            case MOD:
                leaveMath(node);
                break;
            case BAND:
            case BOR:
            case BXOR:
            	leaveBitWise(node);
            	break;
             case LT:
            case GT:
            case LE:
            case GE:
                leaveRelationalOp(node);
                break;
            case COALESCE:
                leaveCoalesce(node);
                break;
            case NOT_EXPR:
                leaveExprNot(node);
                break;
            case PATTERN_NOT_EXPR:
                leavePatternNot(node);
                break;
            case SUM:
            case AVG:
            case COUNT:
            case MEDIAN:
            case STDDEV:
            case AVEDEV:
            case FIRST_AGGREG:
            case LAST_AGGREG:
            case WINDOW_AGGREG:
                leaveAggregate(node);
                break;
            case DOT_EXPR:
            	leaveDotExpr(node);
                break;
            case LIB_FUNC_CHAIN:
            	leaveLibFunction(node);
                break;
            case LEFT_OUTERJOIN_EXPR:
            case RIGHT_OUTERJOIN_EXPR:
            case FULL_OUTERJOIN_EXPR:
            case INNERJOIN_EXPR:
                leaveOuterInnerJoin(node);
                break;
            case GROUP_BY_EXPR:
                leaveGroupBy(node);
                break;
            case HAVING_EXPR:
                leaveHavingClause();
                break;
            case ORDER_BY_EXPR:
            	break;
            case ORDER_ELEMENT_EXPR:
            	leaveOrderByElement(node);
            	break;
            case EVENT_LIMIT_EXPR:
            case TIMEPERIOD_LIMIT_EXPR:
            case CRONTAB_LIMIT_EXPR:
            case WHEN_LIMIT_EXPR:
            case AFTER_LIMIT_EXPR:
            	leaveOutputLimit(node);
            	break;
            case ROW_LIMIT_EXPR:
            	leaveRowLimit(node);
            	break;
            case INSERTINTO_EXPR:
            	leaveInsertInto(node);
            	break;
            case CONCAT:
            	leaveConcat(node);
            	break;
            case CASE:
                leaveCaseNode(node, false);
                break;
            case CASE2:
                leaveCaseNode(node, true);
                break;
            case EVERY_EXPR:
                leaveEvery(node);
                break;
            case EVERY_DISTINCT_EXPR:
                leaveEveryDistinct(node);
                break;
            case FOLLOWED_BY_EXPR:
                leaveFollowedBy(node);
                break;
            case OR_EXPR:
                leaveOr(node);
                break;
            case AND_EXPR:
                leaveAnd(node);
                break;
            case GUARD_EXPR:
                leaveGuard(node);
                break;
            case OBSERVER_EXPR:
                leaveObserver(node);
                break;
            case MATCH_UNTIL_EXPR:
                leaveMatch(node);
                break;
            case IN_SET:
            case NOT_IN_SET:
                leaveInSet(node);
                break;
            case IN_RANGE:
            case NOT_IN_RANGE:
                leaveInRange(node);
                break;
            case BETWEEN:
            case NOT_BETWEEN:
                leaveBetween(node);
                break;
            case LIKE:
            case NOT_LIKE:
                leaveLike(node);
                break;
            case REGEXP:
            case NOT_REGEXP:
                leaveRegexp(node);
                break;
            case PREVIOUS:
            case PREVIOUSTAIL:
            case PREVIOUSWINDOW:
            case PREVIOUSCOUNT:
                leavePrevious(node);
                break;
            case PRIOR:
                leavePrior(node);
                break;
            case ARRAY_EXPR:
                leaveArray(node);
                break;
            case SUBSELECT_EXPR:
                leaveSubselectRow(node);
                break;
            case EXISTS_SUBSELECT_EXPR:
                leaveSubselectExists(node);
                break;
            case IN_SUBSELECT_EXPR:
            case NOT_IN_SUBSELECT_EXPR:
                leaveSubselectIn(node);
                break;
            case IN_SUBSELECT_QUERY_EXPR:
                leaveSubselectQueryIn(node);
                break;
            case INSTANCEOF:
                leaveInstanceOf(node);
                break;
            case TYPEOF:
                leaveTypeOf(node);
                break;
            case EXISTS:
                leaveExists(node);
                break;
            case CAST:
                leaveCast(node);
                break;
            case CURRENT_TIMESTAMP:
                leaveTimestamp(node);
                break;
            case CREATE_WINDOW_EXPR:
                leaveCreateWindow(node);
                break;
            case CREATE_INDEX_EXPR:
                leaveCreateIndex(node);
                break;
            case CREATE_SCHEMA_EXPR:
                leaveCreateSchema(node);
                break;
            case CREATE_WINDOW_SELECT_EXPR:
                leaveCreateWindowSelect();
                break;
            case CREATE_VARIABLE_EXPR:
                leaveCreateVariable(node);
                break;
            case ON_EXPR:
                leaveOnExpr(node);
                break;
            case UPDATE_EXPR:
                leaveUpdateExpr(node);
                break;
            case TIME_PERIOD:
                leaveTimePeriod(node);
                break;
            case NUMBERSETSTAR:
                leaveNumberSetStar(node);
                break;
            case NUMERIC_PARAM_FREQUENCY:
                leaveNumberSetFrequency(node);
                break;
            case NUMERIC_PARAM_RANGE:
                leaveNumberSetRange(node);
                break;
            case NUMERIC_PARAM_LIST:
                leaveNumberSetList(node);
                break;
            case LAST_OPERATOR:
            case LAST:
                leaveLastNumberSetOperator(node);
                break;
            case LW:
                leaveLastWeekdayNumberSetOperator(node);
                break;
            case WEEKDAY_OPERATOR:
                leaveWeekdayNumberSetOperator(node);
                break;
            case OBJECT_PARAM_ORDERED_EXPR:
                leaveObjectParamOrderedExpression(node);
                break;
            case ANNOTATION:
                leaveAnnotation(node);
                break;
            case MATCHREC_MEASURE_ITEM:
                leaveMatchRecognizeMeasureItem(node);
                break;
            case MATCHREC_PATTERN:
                leaveMatchRecognizePattern(node);
                break;
            case MATCHREC_PATTERN_NESTED:
                leaveMatchRecognizePatternNested(node);
                break;
            case MATCHREC_PATTERN_CONCAT:
                leaveMatchRecognizePatternConcat(node);
                break;
            case MATCHREC_PATTERN_ALTER:
                leaveMatchRecognizePatternAlter(node);
                break;
            case MATCHREC_PATTERN_ATOM:
                leaveMatchRecognizePatternAtom(node);
                break;
            case MATCHREC_DEFINE_ITEM:
                leaveMatchRecognizeDefineItem(node);
                break;
            case MATCHREC_PARTITION:
                leaveMatchRecognizePartition(node);
                break;
            case MATCH_RECOGNIZE:
                leaveMatchRecognize(node);
                break;
            case ON_SELECT_EXPR:
                leaveOnSelect(node);
                break;
            case ON_STREAM:
                leaveOnStream(node);
                break;
            case FOR:
                leaveForClause(node);
                break;
            case MERGE_UPD:
                leaveMergeUpdClause(node);
                break;
            case MERGE_INS:
                leaveMergeInsClause(node);
                break;
            default:
                throw new ASTWalkException("Unhandled node type encountered, type '" + node.getType() +
                        "' with text '" + node.getText() + '\'');
        }

        // For each AST child node of this AST node that generated an ExprNode add the child node to the expression node.
        // This is for automatic expression tree building.
        if (!astExprNodeMap.isEmpty())
        {
            mapChildASTToChildExprNode(node);
        }

        // For each AST child node of this AST node that generated an EvalNode add the EvalNode as a child
        if (!astPatternNodeMap.isEmpty())
        {
            EvalNode thisPatternNode = astPatternNodeMap.get(node);
            for (int i = 0; i < node.getChildCount(); i++)
            {
                Tree childNode = node.getChild(i);
                EvalNode childEvalNode = astPatternNodeMap.get(childNode);
                if (childEvalNode != null)
                {
                    thisPatternNode.addChildNode(childEvalNode);
                    astPatternNodeMap.remove(childNode);
                }
            }
        }

        // For each AST child node of this AST node that generated an RowRegexExprNode add the RowRegexExprNode as a child
        if (!astRowRegexNodeMap.isEmpty())
        {
            RowRegexExprNode thisRegexNode = astRowRegexNodeMap.get(node);
            for (int i = 0; i < node.getChildCount(); i++)
            {
                Tree childNode = node.getChild(i);
                RowRegexExprNode childEvalNode = astRowRegexNodeMap.get(childNode);
                if (childEvalNode != null)
                {
                    thisRegexNode.addChildNode(childEvalNode);
                    astRowRegexNodeMap.remove(childNode);
                }
            }
        }
    }

    private void mapChildASTToChildExprNode(Tree node)
    {
        ExprNode thisEvalNode = astExprNodeMap.get(node);
        for (int i = 0; i < node.getChildCount(); i++)
        {
            Tree childNode = node.getChild(i);

            ExprNode childEvalNode = astExprNodeMap.get(childNode);
            // If there was an expression node generated for the child node, and there is a current expression node,
            // add it to the current expression node (thisEvalNode)
            if ((childEvalNode != null) && (thisEvalNode != null))
            {
                thisEvalNode.addChildNode(childEvalNode);
                astExprNodeMap.remove(childNode);
            }
        }
    }

    private void leaveCreateWindow(Tree node)
    {
        log.debug(".leaveCreateWindow");

        String windowName = node.getChild(0).getText();

        String eventName = null;
        Tree eventNameNode = ASTUtil.findFirstNode(node, CLASS_IDENT);
        if (eventNameNode != null)
        {
            eventName = eventNameNode.getText();
        }
        if (eventName == null)
        {
            eventName = "java.lang.Object";
        }

        boolean isRetainUnion = false;
        boolean isRetainIntersection = false;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == RETAINUNION)
            {
                isRetainUnion = true;
                break;
            }
            if (node.getChild(i).getType() == RETAININTERSECTION)
            {
                isRetainIntersection = true;
                break;
            }
        }
        StreamSpecOptions streamSpecOptions = new StreamSpecOptions(false,isRetainUnion,isRetainIntersection);

        // handle table-create clause, i.e. (col1 type, col2 type)
        List<ColumnDesc> colums = getColTypeList(node);

        boolean isInsert = false;
        ExprNode insertWhereExpr = null;
        Tree insertNode = ASTUtil.findFirstNode(node, INSERT);
        if (insertNode != null)
        {
            isInsert = true;
            if (insertNode.getChildCount() > 0)
            {
                insertWhereExpr = ASTUtil.getRemoveExpr(insertNode.getChild(0),  this.astExprNodeMap);
            }
        }

        CreateWindowDesc desc = new CreateWindowDesc(windowName, viewSpecs, streamSpecOptions, isInsert, insertWhereExpr, colums);
        statementSpec.setCreateWindowDesc(desc);

        // this is good for indicating what is being selected from
        FilterSpecRaw rawFilterSpec = new FilterSpecRaw(eventName, new LinkedList<ExprNode>(), null);
        FilterStreamSpecRaw streamSpec = new FilterStreamSpecRaw(rawFilterSpec, new LinkedList<ViewSpec>(), null, streamSpecOptions);
        statementSpec.getStreamSpecs().add(streamSpec);
    }

    private List<ColumnDesc> getColTypeList(Tree node)
    {
        List<ColumnDesc> result = new ArrayList<ColumnDesc>();
        for (int nodeNum = 0; nodeNum < node.getChildCount(); nodeNum++) {
            if (node.getChild(nodeNum).getType() == CREATE_COL_TYPE_LIST)
            {
                Tree parent = node.getChild(nodeNum);
                for (int i = 0; i < parent.getChildCount(); i++)
                {
                    String name = parent.getChild(i).getChild(0).getText();
                    String type = parent.getChild(i).getChild(1).getText();
                    boolean array = parent.getChild(i).getChildCount() > 2;
                    result.add(new ColumnDesc(name, type, array));
                }
            }
        }
        return result;
    }

    private void leaveCreateIndex(Tree node)
    {
        log.debug(".leaveCreateIndex");

        String indexName = node.getChild(0).getText();
        String windowName = node.getChild(1).getText();

        Tree nodeExpr = node.getChild(2);
        List<String> columns = new ArrayList<String>();

        for (int i = 0; i < nodeExpr.getChildCount(); i++)
        {
            if (nodeExpr.getChild(i).getType() == IDENT)
            {
                columns.add(nodeExpr.getChild(i).getText());
            }
        }

        statementSpec.setCreateIndexDesc(new CreateIndexDesc(indexName, windowName, columns));
    }

    private void leaveCreateSchema(Tree node)
    {
        log.debug(".leaveCreateSchema");

        String schemaName = node.getChild(0).getText();

        List<ColumnDesc> columnTypes = getColTypeList(node);

        // get model-after types (could be multiple for variants)
        Set<String> typeNames = new LinkedHashSet<String>();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i).getType() == VARIANT_LIST) {
                for (int j = 0; j < node.getChild(i).getChildCount(); j++) {
                    typeNames.add(node.getChild(i).getChild(j).getText());
                }
            }
        }

        // get inherited
        Set<String> inherited = new LinkedHashSet<String>();
        for (int i = 0; i < node.getChildCount(); i++) {
            Tree p = node.getChild(i);
            if (p.getType() == CREATE_SCHEMA_EXPR_INH) {
                if (!p.getChild(0).getText().toLowerCase().equals("inherits")) {
                    throw new EPException("Expected 'inherits' keyword after create-schema clause but encountered '" + p.getChild(0).getText() + "'");
                }
                for (int j = 1; j < p.getChildCount(); j++) {
                    if (p.getChild(j).getType() == EXPRCOL) {
                        for (int k = 0; k < p.getChild(j).getChildCount(); k++) {
                            inherited.add(p.getChild(j).getChild(k).getText());
                        }
                    }
                }
            }
        }

        // get qualifier
        boolean variant = false;
        for (int i = 0; i < node.getChildCount(); i++) {
            Tree p = node.getChild(i);
            if (p.getType() == CREATE_SCHEMA_EXPR_QUAL) {
                if (!p.getChild(0).getText().toLowerCase().equals("variant")) {
                    throw new EPException("Expected 'variant' keyword after create-schema clause but encountered '" + p.getChild(0).getText() + "'");
                }
                variant = true;
            }
        }
        statementSpec.getStreamSpecs().add(new FilterStreamSpecRaw(new FilterSpecRaw(Object.class.getName(), Collections.<ExprNode>emptyList(), null), Collections.<ViewSpec>emptyList(), null, new StreamSpecOptions()));
        statementSpec.setCreateSchemaDesc(new CreateSchemaDesc(schemaName, typeNames, columnTypes, inherited, variant));
    }

    private void leaveCreateVariable(Tree node)
    {
        log.debug(".leaveCreateVariable");

        Tree child = node.getChild(0);
        String variableType = child.getText();
        child = node.getChild(1);
        String variableName = child.getText();

        ExprNode assignment = null;
        if (node.getChildCount() > 2)
        {
            child = node.getChild(2);
            assignment = astExprNodeMap.get(child);
            astExprNodeMap.remove(child);
        }

        CreateVariableDesc desc = new CreateVariableDesc(variableType, variableName, assignment);
        statementSpec.setCreateVariableDesc(desc);
    }

    private void leaveCreateWindowSelect()
    {
        log.debug(".leaveCreateWindowSelect");
    }

    private void leaveOnExpr(Tree node)
    {
        log.debug(".leaveOnExpr");

        // determine on-delete or on-select
        boolean isOnDelete = false;
        Tree typeChildNode = null;

        for (int i = 0; i < node.getChildCount(); i++)
        {
        	Tree childNode = node.getChild(i);

            if (childNode.getType() == ON_DELETE_EXPR)
            {
                typeChildNode = childNode;
                isOnDelete = true;
            }
            else if (childNode.getType() == ON_SELECT_EXPR)
            {
                typeChildNode = childNode;
            }
            else if (childNode.getType() == ON_UPDATE_EXPR)
            {
                typeChildNode = childNode;
            }
            else if (childNode.getType() == ON_SET_EXPR)
            {
                typeChildNode = childNode;
            }
            else if (childNode.getType() == ON_MERGE_EXPR)
            {
                typeChildNode = childNode;
            }
        }
        if (typeChildNode == null)
        {
            throw new IllegalStateException("Could not determine on-expr type");
        }

        if (typeChildNode.getType() == ON_MERGE_EXPR) {
            String windowName = typeChildNode.getChild(0).getText();
            String asName = null;
            if (typeChildNode.getChild(1).getType() == IDENT) {
                asName = typeChildNode.getChild(1).getText();
            }

            OnTriggerMergeDesc desc = new OnTriggerMergeDesc(windowName, asName, mergeInstructions == null ? Collections.<OnTriggerMergeItem>emptyList() : mergeInstructions);
            statementSpec.setOnTriggerDesc(desc);            
        }
        else if (typeChildNode.getType() != ON_SET_EXPR)
        {
            // The ON_EXPR_FROM contains the window name
            UniformPair<String> windowName = getWindowName(typeChildNode);
            if (windowName == null)
            {
                // on the statement spec, the deepest spec is the outermost
                List<OnTriggerSplitStream> splitStreams = new ArrayList<OnTriggerSplitStream>();
                for (int i = 1; i <= statementSpecStack.size() - 1; i++)
                {
                    StatementSpecRaw raw = statementSpecStack.get(i);
                    splitStreams.add(new OnTriggerSplitStream(raw.getInsertIntoDesc(), raw.getSelectClauseSpec(), raw.getFilterExprRootNode()));
                }
                splitStreams.add(new OnTriggerSplitStream(statementSpec.getInsertIntoDesc(), statementSpec.getSelectClauseSpec(), statementSpec.getFilterExprRootNode()));
                if (!statementSpecStack.isEmpty())
                {
                    statementSpec = statementSpecStack.get(0);
                }
                boolean isFirst = isSelectInsertFirst(node);
                statementSpec.setOnTriggerDesc(new OnTriggerSplitStreamDesc(OnTriggerType.ON_SPLITSTREAM, isFirst, splitStreams));
                statementSpecStack.clear();
            }
            else if (typeChildNode.getType() == ON_UPDATE_EXPR) {
                List<OnTriggerSetAssignment> assignments = getOnTriggerSetAssignments(typeChildNode, astExprNodeMap);
                statementSpec.setOnTriggerDesc(new OnTriggerWindowUpdateDesc(windowName.getFirst(), windowName.getSecond(), assignments));
                statementSpec.setFilterExprRootNode(getRemoveFirstByType(typeChildNode, WHERE_EXPR));
            }
            else
            {
                statementSpec.setOnTriggerDesc(new OnTriggerWindowDesc(windowName.getFirst(), windowName.getSecond(), isOnDelete ? OnTriggerType.ON_DELETE : OnTriggerType.ON_SELECT));
            }
        }
        else
        {
            List<OnTriggerSetAssignment> assignments = getOnTriggerSetAssignments(typeChildNode, astExprNodeMap);
            statementSpec.setOnTriggerDesc(new OnTriggerSetDesc(assignments));
        }
    }

    private void leaveOnStream(Tree node)
    {
        log.debug(".leaveOnStream");

        // get optional filter stream as-name
        Tree childNode = node.getChild(1);
        String streamAsName = null;
        if ((childNode != null) && (childNode.getType() == IDENT))
        {
            streamAsName = childNode.getText();
        }

        // get stream to use (pattern or filter)
        StreamSpecRaw streamSpec;
        if (node.getChild(0).getType() == EVENT_FILTER_EXPR)
        {
            streamSpec = new FilterStreamSpecRaw(filterSpec, new ArrayList<ViewSpec>(), streamAsName, new StreamSpecOptions());
        }
        else if (node.getChild(0).getType() == PATTERN_INCL_EXPR)
        {
            if ((astPatternNodeMap.size() > 1) || ((astPatternNodeMap.isEmpty())))
            {
                throw new ASTWalkException("Unexpected AST tree contains zero or more then 1 child elements for root");
            }
            // Get expression node sub-tree from the AST nodes placed so far
            EvalNode evalNode = astPatternNodeMap.values().iterator().next();
            streamSpec = new PatternStreamSpecRaw(evalNode, viewSpecs, streamAsName, new StreamSpecOptions());
            astPatternNodeMap.clear();
        }
        else
        {
            throw new IllegalStateException("Invalid AST type node, cannot map to stream specification");
        }

        statementSpec.getStreamSpecs().add(streamSpec);
    }

    private void leaveForClause(Tree node)
    {
        log.debug(".leaveForClause");

        if (statementSpec.getForClauseSpec() == null) {
            statementSpec.setForClauseSpec(new ForClauseSpec());
        }
        String ident = node.getChild(0).getText();
        List<ExprNode> expressions = getExprNodes(node, 1);
        statementSpec.getForClauseSpec().getClauses().add(new ForClauseItemSpec(ident, expressions));
    }

    private void leaveMergeUpdClause(Tree node)
    {
        log.debug(".leaveMergeUpdClause");

        if (mergeInstructions == null) {
            mergeInstructions = new ArrayList<OnTriggerMergeItem>();
        }
        ExprNode filterSpec = null;
        if ((node.getChild(0).getType() != INSERT) && (node.getChild(0).getType() != DELETE)) {
            filterSpec = ASTUtil.getRemoveExpr(node.getChild(0), astExprNodeMap);
        }

        boolean isDelete = ASTUtil.findFirstNode(node, DELETE) != null;
        if (isDelete) {
            mergeInstructions.add(new OnTriggerMergeItemDelete(filterSpec));
        }
        else {
            List<OnTriggerSetAssignment> sets = getOnTriggerSetAssignments(node, astExprNodeMap);
            mergeInstructions.add(new OnTriggerMergeItemUpdate(filterSpec, sets));
        }
    }

    private void leaveMergeInsClause(Tree node)
    {
        log.debug(".leaveMergeInsClause");

        ExprNode filterSpec = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            filterSpec = ASTUtil.getRemoveExpr(node.getChild(i), astExprNodeMap);
        }

        List<String> columsList = Collections.emptyList();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i).getType() == EXPRCOL) {
                columsList = getIdentList(node.getChild(i));
            }
        }

        List<SelectClauseElementRaw> expressions = new ArrayList<SelectClauseElementRaw>(statementSpec.getSelectClauseSpec().getSelectExprList());
        statementSpec.getSelectClauseSpec().getSelectExprList().clear();

        if (mergeInstructions == null) {
            mergeInstructions = new ArrayList<OnTriggerMergeItem>();
        }
        mergeInstructions.add(new OnTriggerMergeItemInsert(filterSpec, columsList, expressions));
    }

    private void leaveUpdateExpr(Tree node)
    {
        log.debug(".leaveUpdateExpr");

        String eventTypeName = node.getChild(0).getText();
        FilterStreamSpecRaw streamSpec = new FilterStreamSpecRaw(new FilterSpecRaw(eventTypeName, Collections.<ExprNode>emptyList(), null), new ArrayList<ViewSpec>(), eventTypeName, new StreamSpecOptions());
        statementSpec.getStreamSpecs().add(streamSpec);

        String optionalStreamName = null;
        if ((node.getChildCount() > 1) && (node.getChild(1).getType() == IDENT))
        {
            optionalStreamName = node.getChild(1).getText();
        }

        List<OnTriggerSetAssignment> assignments = getOnTriggerSetAssignments(node, astExprNodeMap);
        ExprNode whereClause = this.getRemoveFirstByType(node, WHERE_EXPR);
        statementSpec.setUpdateDesc(new UpdateDesc(optionalStreamName, assignments, whereClause));
    }

    /**
     * Returns the list of set-variable assignments under the given node.
     * @param node node to inspect
     * @param astExprNodeMap map of AST to expression
     * @return list of assignments
     */
    protected static List<OnTriggerSetAssignment> getOnTriggerSetAssignments(Tree node, Map<Tree, ExprNode> astExprNodeMap)
    {
        List<OnTriggerSetAssignment> assignments = new ArrayList<OnTriggerSetAssignment>();
        if (node == null) {
            return assignments;
        }

        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() != ON_SET_EXPR_ITEM)
            {
                continue;
            }

            Tree childNode = node.getChild(i);
            String variableName = ASTFilterSpecHelper.getPropertyName(childNode.getChild(0), 0);
            ExprNode childEvalNode = astExprNodeMap.get(childNode.getChild(1));
            astExprNodeMap.remove(childNode.getChild(1));
            assignments.add(new OnTriggerSetAssignment(variableName, childEvalNode));
        }
        return assignments;
    }

    private UniformPair<String> getWindowName(Tree typeChildNode)
    {
        String windowName = null;
        String windowStreamName = null;

        for (int i = 0; i < typeChildNode.getChildCount(); i++)
        {
        	Tree child = typeChildNode.getChild(i);
            if (child.getType() == ON_EXPR_FROM)
            {
                windowName = child.getChild(0).getText();
                if (child.getChildCount() > 1)
                {
                    windowStreamName = child.getChild(1).getText();
                }
                break;
            }
        }
        if (windowName == null)
        {
            return null;
        }
        return new UniformPair<String>(windowName, windowStreamName);
    }


    private void leavePrevious(Tree node)
    {
        log.debug(".leavePrevious");

        PreviousType previousType;
        if (node.getType() == PREVIOUS) {
            previousType = PreviousType.PREV;
        }
        else if (node.getType() == PREVIOUSTAIL) {
            previousType = PreviousType.PREVTAIL;
        }
        else if (node.getType() == PREVIOUSWINDOW) {
            previousType = PreviousType.PREVWINDOW;
        }
        else if (node.getType() == PREVIOUSCOUNT) {
            previousType = PreviousType.PREVCOUNT;
        }
        else {
            throw new IllegalStateException("Failed to handle type '" + node.getType() + "'");
        }

        ExprPreviousNode previousNode = new ExprPreviousNode(previousType);
        astExprNodeMap.put(node, previousNode);
    }

    private void leavePrior(Tree node)
    {
        log.debug(".leavePrior");

        ExprPriorNode priorNode = new ExprPriorNode();
        astExprNodeMap.put(node, priorNode);
    }

    private void leaveInstanceOf(Tree node)
    {
        log.debug(".leaveInstanceOf");

        // get class identifiers
        List<String> classes = new ArrayList<String>();
        for (int i = 1; i < node.getChildCount(); i++)
        {
            Tree classIdent = node.getChild(i);
            classes.add(classIdent.getText());
        }

        String idents[] = classes.toArray(new String[classes.size()]);
        ExprInstanceofNode instanceofNode = new ExprInstanceofNode(idents);
        astExprNodeMap.put(node, instanceofNode);
    }

    private void leaveTypeOf(Tree node)
    {
        log.debug(".leaveTypeOf");
        ExprTypeofNode typeofNode = new ExprTypeofNode();
        astExprNodeMap.put(node, typeofNode);
    }

    private void leaveExists(Tree node)
    {
        log.debug(".leaveExists");

        ExprPropertyExistsNode instanceofNode = new ExprPropertyExistsNode();
        astExprNodeMap.put(node, instanceofNode);
    }

    private void leaveCast(Tree node)
    {
        log.debug(".leaveCast");

        String classIdent = node.getChild(1).getText();
        ExprCastNode castNode = new ExprCastNode(classIdent);
        astExprNodeMap.put(node, castNode);
    }

    private void leaveTimestamp(Tree node)
    {
        log.debug(".leaveTimestamp");

        ExprTimestampNode timeNode = new ExprTimestampNode();
        astExprNodeMap.put(node, timeNode);
    }

    private void leaveTimePeriod(Tree node)
    {
        log.debug(".leaveTimePeriod");

        ExprNode nodes[] = new ExprNode[8];
        for (int i = 0; i < node.getChildCount(); i++)
        {
            Tree child = node.getChild(i);
            if (child.getType() == MILLISECOND_PART)
            {
                nodes[7] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == SECOND_PART)
            {
                nodes[6] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == MINUTE_PART)
            {
                nodes[5] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == HOUR_PART)
            {
                nodes[4] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == DAY_PART)
            {
                nodes[3] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == WEEK_PART)
            {
                nodes[2] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == MONTH_PART)
            {
                nodes[1] = astExprNodeMap.remove(child.getChild(0));
            }
            if (child.getType() == YEAR_PART)
            {
                nodes[0] = astExprNodeMap.remove(child.getChild(0));
            }
        }
        ExprTimePeriod timeNode = new ExprTimePeriod(nodes[0] != null, nodes[1]!= null, nodes[2]!= null, nodes[3]!= null, nodes[4]!= null, nodes[5]!= null, nodes[6]!= null, nodes[7]!= null);
        if (nodes[0] != null) timeNode.addChildNode(nodes[0]);
        if (nodes[1] != null) timeNode.addChildNode(nodes[1]);
        if (nodes[2] != null) timeNode.addChildNode(nodes[2]);
        if (nodes[3] != null) timeNode.addChildNode(nodes[3]);
        if (nodes[4] != null) timeNode.addChildNode(nodes[4]);
        if (nodes[5] != null) timeNode.addChildNode(nodes[5]);
        if (nodes[6] != null) timeNode.addChildNode(nodes[6]);
        if (nodes[7] != null) timeNode.addChildNode(nodes[7]);
        astExprNodeMap.put(node, timeNode);
    }

    private void leaveNumberSetStar(Tree node)
    {
        log.debug(".leaveNumberSetStar");
        ExprNumberSetWildcard exprNode = new ExprNumberSetWildcard();
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveNumberSetFrequency(Tree node)
    {
        log.debug(".leaveNumberSetFrequency");
        ExprNumberSetFrequency exprNode = new ExprNumberSetFrequency();
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveNumberSetRange(Tree node)
    {
        log.debug(".leaveNumberSetRange");
        ExprNumberSetRange exprNode = new ExprNumberSetRange();
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveNumberSetList(Tree node)
    {
        log.debug(".leaveNumberSetList");
        ExprNumberSetList exprNode = new ExprNumberSetList();
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveLastNumberSetOperator(Tree node)
    {
        log.debug(".leaveLastNumberSetOperator");        
        ExprNumberSetCronParam exprNode = new ExprNumberSetCronParam(CronOperatorEnum.LASTDAY);
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveLastWeekdayNumberSetOperator(Tree node)
    {
        log.debug(".leaveLastWeekdayNumberSetOperator");
        ExprNumberSetCronParam exprNode = new ExprNumberSetCronParam(CronOperatorEnum.LASTWEEKDAY);
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveWeekdayNumberSetOperator(Tree node)
    {
        log.debug(".leaveWeekdayNumberSetOperator");
        ExprNumberSetCronParam exprNode = new ExprNumberSetCronParam(CronOperatorEnum.WEEKDAY);
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveObjectParamOrderedExpression(Tree node)
    {
        log.debug(".leaveObjectParamOrderedExpression");

        boolean isDescending = false;
        if ((node.getChildCount() > 1) && (node.getChild(1).getText().toUpperCase().equals("DESC")))
        {
            isDescending = true;
        }
        ExprOrderedExpr exprNode = new ExprOrderedExpr(isDescending);
        astExprNodeMap.put(node, exprNode);
    }

    private void leaveAnnotation(Tree node)
    {
        log.debug(".leaveAnnotation");
        statementSpec.getAnnotations().add(ASTAnnotationHelper.walk(node, this.engineImportService));
    }

    private void leaveArray(Tree node)
    {
        log.debug(".leaveArray");

        ExprArrayNode arrayNode = new ExprArrayNode();
        astExprNodeMap.put(node, arrayNode);
    }

    private void leaveSubselectRow(Tree node)
    {
        log.debug(".leaveSubselectRow");

        StatementSpecRaw currentSpec = popStacks();
        ExprSubselectRowNode subselectNode = new ExprSubselectRowNode(currentSpec);
        astExprNodeMap.put(node, subselectNode);
    }

    private void leaveSubselectExists(Tree node)
    {
        log.debug(".leaveSubselectExists");

        StatementSpecRaw currentSpec = popStacks();
        ExprSubselectNode subselectNode = new ExprSubselectExistsNode(currentSpec);
        astExprNodeMap.put(node, subselectNode);
    }

    private void leaveSubselectIn(Tree node)
    {
        log.debug(".leaveSubselectIn");

        Tree nodeSubquery = node.getChild(1);

        boolean isNot = false;
        if (node.getType() == NOT_IN_SUBSELECT_EXPR)
        {
            isNot = true;
        }

        ExprSubselectInNode subqueryNode = (ExprSubselectInNode) astExprNodeMap.remove(nodeSubquery);
        subqueryNode.setNotIn(isNot);

        astExprNodeMap.put(node, subqueryNode);
    }

    private void leaveSubselectQueryIn(Tree node)
    {
        log.debug(".leaveSubselectQueryIn");

        StatementSpecRaw currentSpec = popStacks();
        ExprSubselectNode subselectNode = new ExprSubselectInNode(currentSpec);
        astExprNodeMap.put(node, subselectNode);
    }

    private StatementSpecRaw popStacks()
    {
        log.debug(".popStacks");

        StatementSpecRaw currentSpec = statementSpec;
        statementSpec = statementSpecStack.pop();

        if (currentSpec.isHasVariables())
        {
            statementSpec.setHasVariables(true);
        }
        if (currentSpec.getReferencedVariables() != null) {
            for (String var : currentSpec.getReferencedVariables()) {
                addVariable(statementSpec, var);
            }
        }

        astExprNodeMap = astExprNodeMapStack.pop();

        return currentSpec;
    }

    /**
     * End processing of the AST tree for stand-alone pattern expressions.
     * @throws ASTWalkException is the walk failed
     */
    protected void endPattern() throws ASTWalkException
    {
        log.debug(".endPattern");

        if ((astPatternNodeMap.size() > 1) || ((astPatternNodeMap.isEmpty())))
        {
            throw new ASTWalkException("Unexpected AST tree contains zero or more then 1 child elements for root");
        }

        // Get expression node sub-tree from the AST nodes placed so far
        EvalNode evalNode = astPatternNodeMap.values().iterator().next();

        PatternStreamSpecRaw streamSpec = new PatternStreamSpecRaw(evalNode, new LinkedList<ViewSpec>(), null, new StreamSpecOptions());
        statementSpec.getStreamSpecs().add(streamSpec);
        statementSpec.setSubstitutionParameters(substitutionParamNodes);

        astPatternNodeMap.clear();
    }

    /**
     * End processing of the AST tree, check that expression nodes found their homes.
     * @throws ASTWalkException is the walk failed
     */
    protected void end() throws ASTWalkException
    {
        log.debug(".end");

        if (astExprNodeMap.size() > 1)
        {
            throw new ASTWalkException("Unexpected AST tree contains left over child elements," +
                    " not all expression nodes have been removed from AST-to-expression nodes map");
        }
        if (astPatternNodeMap.size() > 1)
        {
            throw new ASTWalkException("Unexpected AST tree contains left over child elements," +
                    " not all pattern nodes have been removed from AST-to-pattern nodes map");
        }

        statementSpec.setSubstitutionParameters(substitutionParamNodes);
    }

    private void leaveSelectionElement(Tree node) throws ASTWalkException
    {
        log.debug(".leaveSelectionElement");

        if ((astExprNodeMap.size() > 1) || ((astExprNodeMap.isEmpty())))
        {
            throw new ASTWalkException("Unexpected AST tree contains zero or more then 1 child element for root");
        }

        // Get expression node sub-tree from the AST nodes placed so far
        ExprNode exprNode = astExprNodeMap.values().iterator().next();
        astExprNodeMap.clear();

        // Get list element name
        String optionalName = null;
        if (node.getChildCount() > 1)
        {
            optionalName = node.getChild(1).getText();
        }

        // Add as selection element
        statementSpec.getSelectClauseSpec().add(new SelectClauseExprRawSpec(exprNode, optionalName));
    }

    private void leavePropertySelectionElement(Tree node) throws ASTWalkException
    {
        log.debug(".leavePropertySelectionElement");

        if ((astExprNodeMap.size() > 1) || ((astExprNodeMap.isEmpty())))
        {
            throw new ASTWalkException("Unexpected AST tree contains zero or more then 1 child element for root");
        }

        // Get expression node sub-tree from the AST nodes placed so far
        ExprNode exprNode = astExprNodeMap.values().iterator().next();
        astExprNodeMap.clear();

        // Get list element name
        String optionalName = null;
        if (node.getChildCount() > 1)
        {
            optionalName = node.getChild(1).getText();
        }

        // Add as selection element
        if (propertySelectRaw == null)
        {
            propertySelectRaw = new ArrayList<SelectClauseElementRaw>();
        }
        this.propertySelectRaw.add(new SelectClauseExprRawSpec(exprNode, optionalName));
    }

    private void leavePropertySelectionStream(Tree node) throws ASTWalkException
    {
        log.debug(".leavePropertySelectionStream");

        String streamName = node.getChild(0).getText();

        // Get element name
        String optionalName = null;
        if (node.getChildCount() > 1)
        {
            optionalName = node.getChild(1).getText();
        }

        // Add as selection element
        if (propertySelectRaw == null)
        {
            propertySelectRaw = new ArrayList<SelectClauseElementRaw>();
        }
        this.propertySelectRaw.add(new SelectClauseStreamRawSpec(streamName, optionalName));
    }

    private void leaveSelectionStream(Tree node) throws ASTWalkException
    {
        log.debug(".leaveSelectionStream");

        String streamName = node.getChild(0).getText();

        // Get element name
        String optionalName = null;
        if (node.getChildCount() > 1)
        {
            optionalName = node.getChild(1).getText();
        }

        // Add as selection element
        statementSpec.getSelectClauseSpec().add(new SelectClauseStreamRawSpec(streamName, optionalName));
    }

    private void leaveWildcardSelect()
    {
    	log.debug(".leaveWildcardSelect");
        statementSpec.getSelectClauseSpec().add(new SelectClauseElementWildcard());
    }

    private void leavePropertyWildcardSelect()
    {
    	log.debug(".leavePropertyWildcardSelect");
        if (propertySelectRaw == null)
        {
            propertySelectRaw = new ArrayList<SelectClauseElementRaw>();
        }
        this.propertySelectRaw.add(new SelectClauseElementWildcard());
    }

    private void leavePropertySelectAtom(Tree node)
    {
    	log.debug(".leavePropertySelectAtom");

        // initialize if not set
        if (propertyEvalSpec == null)
        {
            propertyEvalSpec = new PropertyEvalSpec();
        }

        // get select clause
        SelectClauseSpecRaw optionalSelectClause = new SelectClauseSpecRaw();
        if (propertySelectRaw != null)
        {
            optionalSelectClause.getSelectExprList().addAll(propertySelectRaw);
            propertySelectRaw = null;
        }

        // get where-clause, if any
        ExprNode optionalWhereClause = null;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == WHERE_EXPR)
            {
                optionalWhereClause = this.astExprNodeMap.remove(node.getChild(i).getChild(0));
            }
        }

        String propertyName = null;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == EVENT_PROP_EXPR)
            {
                propertyName = ASTFilterSpecHelper.getPropertyName(node.getChild(i), 0);
            }
        }

        String optionalAsName = null;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == IDENT)
            {
               optionalAsName = node.getChild(i).getText();
            }
        }

        PropertyEvalAtom atom = new PropertyEvalAtom(propertyName, optionalAsName, optionalSelectClause, optionalWhereClause);
        propertyEvalSpec.add(atom);
    }

    private void leaveView(Tree node) throws ASTWalkException
    {
        log.debug(".leaveView");
        String objectNamespace = node.getChild(0).getText();
        String objectName = node.getChild(1).getText();
        List<ExprNode> viewParameters = getExprNodes(node, 2); 
        viewSpecs.add(new ViewSpec(objectNamespace, objectName, viewParameters));
    }

    private void leaveMatchRecognizeMeasureItem(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatchRecognizeMeasureItem");

        if (statementSpec.getMatchRecognizeSpec() == null)
        {
            statementSpec.setMatchRecognizeSpec(new MatchRecognizeSpec());
        }

        Tree currentNode = node.getChild(0);
        ExprNode exprNode = astExprNodeMap.get(currentNode);
        if (exprNode == null)
        {
            throw new IllegalStateException("Expression node for AST node not found for type " + currentNode.getType() + " and text " + currentNode.getText());
        }
        astExprNodeMap.remove(currentNode);

        String name = null;
        if (node.getChildCount() > 1)
        {
            name = node.getChild(1).getText();
        }
        statementSpec.getMatchRecognizeSpec().addMeasureItem(new MatchRecognizeMeasureItem(exprNode,name));
    }

    private void leaveMatchRecognizePatternAtom(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatchRecognizePatternAtom");
        
        String first = node.getChild(0).getText();
        RegexNFATypeEnum type = RegexNFATypeEnum.SINGLE;
        if (node.getChildCount() > 2)
        {
            type = RegexNFATypeEnum.fromString(node.getChild(1).getText(), node.getChild(2).getText());
        }
        else if (node.getChildCount() > 1)
        {
            type = RegexNFATypeEnum.fromString(node.getChild(1).getText(), null);
        }

        RowRegexExprNodeAtom item = new RowRegexExprNodeAtom(first, type);
        astRowRegexNodeMap.put(node, item);
    }

    private void leaveMatchRecognizePatternAlter(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatchRecognizePatternAlter");

        RowRegexExprNodeAlteration alterNode = new RowRegexExprNodeAlteration();
        astRowRegexNodeMap.put(node, alterNode);
    }

    private void leaveMatchRecognizePatternConcat(Tree node) throws ASTWalkException
    {
        RowRegexExprNodeConcatenation concatNode = new RowRegexExprNodeConcatenation();
        astRowRegexNodeMap.put(node, concatNode);
    }

    private void leaveMatchRecognizePatternNested(Tree node) throws ASTWalkException
    {
        RegexNFATypeEnum type = RegexNFATypeEnum.SINGLE;
        if (node.getChildCount() > 2)
        {
            type = RegexNFATypeEnum.fromString(node.getChild(1).getText(), node.getChild(2).getText());
        }
        else if (node.getChildCount() > 1)
        {
            type = RegexNFATypeEnum.fromString(node.getChild(1).getText(), null);
        }
        RowRegexExprNodeNested nestedNode = new RowRegexExprNodeNested(type);
        astRowRegexNodeMap.put(node, nestedNode);
    }

    private void leaveMatchRecognizePattern(Tree node) throws ASTWalkException
    {
        Tree currentNode = node.getChild(0);
        RowRegexExprNode exprNode = this.astRowRegexNodeMap.get(currentNode);
        if (exprNode == null)
        {
            throw new IllegalStateException("Expression node for AST node not found for type " + currentNode.getType() + " and text " + currentNode.getText());
        }
        astRowRegexNodeMap.remove(currentNode);
        statementSpec.getMatchRecognizeSpec().setPattern(exprNode);
    }

    private void leaveMatchRecognizeDefineItem(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatchRecognizeDefineItem");
        String first = node.getChild(0).getText();

        Tree currentNode = node.getChild(1);
        ExprNode exprNode = astExprNodeMap.get(currentNode);
        if (exprNode == null)
        {
            throw new IllegalStateException("Expression node for AST node not found for type " + currentNode.getType() + " and text " + currentNode.getText());
        }
        astExprNodeMap.remove(currentNode);
        statementSpec.getMatchRecognizeSpec().getDefines().add(new MatchRecognizeDefineItem(first, exprNode));
    }

    private void leaveMatchRecognize(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatchRecognize");

        boolean allMatches = false;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == ALL)
            {
                allMatches = true;
            }
        }

        MatchRecognizeSkipEnum skip;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == MATCHREC_AFTER_SKIP)
            {
                skip = ASTMatchRecognizeHelper.parseSkip(node.getChild(i));
                statementSpec.getMatchRecognizeSpec().getSkip().setSkip(skip);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == MATCHREC_INTERVAL)
            {
                Tree intervalParent = node.getChild(i);
                if (!intervalParent.getChild(0).getText().toLowerCase().equals("interval"))
                {
                    throw new ASTWalkException("Invalid interval-clause within match-recognize, expecting keyword INTERVAL");
                }
                ExprNode expression = astExprNodeMap.remove(intervalParent.getChild(1));
                ExprTimePeriod timePeriodExpr;
                try {
                    timePeriodExpr = (ExprTimePeriod) expression.getValidatedSubtree(new StreamTypeServiceImpl(engineURI, false), null, null, timeProvider, variableService, exprEvaluatorContext);
                }
                catch (ExprValidationException ex)
                {
                    throw new ASTWalkException("Invalid interval-clause within match-recognize: " + ex.getMessage(), ex);
                }
                statementSpec.getMatchRecognizeSpec().setInterval(new MatchRecognizeInterval(timePeriodExpr));
            }
        }

        statementSpec.getMatchRecognizeSpec().setAllMatches(allMatches);
    }

    private void leaveOnSelect(Tree node) throws ASTWalkException
    {
        log.debug(".leaveOnSelect");

        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == DISTINCT)
            {
                statementSpec.getSelectClauseSpec().setDistinct(true);
            }
        }
    }

    private void leaveMatchRecognizePartition(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatchRecognizePartition");
        if (statementSpec.getMatchRecognizeSpec() == null)
        {
            statementSpec.setMatchRecognizeSpec(new MatchRecognizeSpec());
        }
        statementSpec.getMatchRecognizeSpec().getPartitionByExpressions().addAll(getExprNodes(node, 0));
    }

    private void leaveStreamExpr(Tree node)
    {
        log.debug(".leaveStreamExpr");

        // Determine the optional stream name
        // Search for identifier node that carries the stream name in an "from Class.win:time().std:doit() as StreamName"
        Tree streamNameNode = null;
        for (int i = 1; i < node.getChildCount(); i++)
        {
            Tree child = node.getChild(i);
            if (child.getType() == IDENT)
            {
                streamNameNode = child;
                break;
            }
        }
        String streamName = null;
        if (streamNameNode != null)
        {
            streamName = streamNameNode.getText();
        }

        // The first child node may be a "stream" keyword
        boolean isUnidirectional = false;
        boolean isRetainUnion = false;
        boolean isRetainIntersection = false;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == UNIDIRECTIONAL)
            {
                isUnidirectional = true;
                break;
            }
            if (node.getChild(i).getType() == RETAINUNION)
            {
                isRetainUnion = true;
                break;
            }
            if (node.getChild(i).getType() == RETAININTERSECTION)
            {
                isRetainIntersection = true;
                break;
            }
        }

        // Convert to a stream specification instance
        StreamSpecRaw streamSpec;
        StreamSpecOptions options = new StreamSpecOptions(isUnidirectional, isRetainUnion, isRetainIntersection);

        // If the first subnode is a filter node, we have a filter stream specification
        if (node.getChild(0).getType() == EVENT_FILTER_EXPR)
        {
            streamSpec = new FilterStreamSpecRaw(filterSpec, viewSpecs, streamName, options);
        }
        else if (node.getChild(0).getType() == PATTERN_INCL_EXPR)
        {
            if ((astPatternNodeMap.size() > 1) || ((astPatternNodeMap.isEmpty())))
            {
                throw new ASTWalkException("Unexpected AST tree contains zero or more then 1 child elements for root");
            }

            // Get expression node sub-tree from the AST nodes placed so far
            EvalNode evalNode = astPatternNodeMap.values().iterator().next();

            streamSpec = new PatternStreamSpecRaw(evalNode, viewSpecs, streamName, options);
            astPatternNodeMap.clear();
        }
        else if (node.getChild(0).getType() == DATABASE_JOIN_EXPR)
        {
            Tree dbrootNode = node.getChild(0);
            String dbName = dbrootNode.getChild(0).getText();
            String sqlWithParams = StringValue.parseString(dbrootNode.getChild(1).getText().trim());

            // determine if there is variables used
            List<PlaceholderParser.Fragment> sqlFragments;
            try
            {
                sqlFragments = PlaceholderParser.parsePlaceholder(sqlWithParams);
                for (PlaceholderParser.Fragment fragment : sqlFragments)
                {
                    if (!(fragment instanceof PlaceholderParser.ParameterFragment)) {
                        continue;
                    }

                    // Parse expression, store for substitution parameters
                    String expression = fragment.getValue();
                    if (expression.toUpperCase().equals(DatabasePollingViewableFactory.SAMPLE_WHERECLAUSE_PLACEHOLDER)) {
                        continue;
                    }
                    
                    if (expression.trim().length() == 0) {
                        throw new ASTWalkException("Missing expression within ${...} in SQL statement");
                    }
                    String toCompile = "select * from java.lang.Object where " + expression;
                    StatementSpecRaw raw = EPAdministratorHelper.compileEPL(toCompile, expression, false, null, SelectClauseStreamSelectorEnum.ISTREAM_ONLY,
                            engineImportService, variableService, schedulingService, engineURI, configurationInformation, patternNodeFactory);

                    if ((raw.getSubstitutionParameters() != null) && (raw.getSubstitutionParameters().size() > 0)) {
                        throw new ASTWalkException("EPL substitution parameters are not allowed in SQL ${...} expressions, consider using a variable instead");
                    }

                    if (raw.isHasVariables()) {
                        statementSpec.setHasVariables(true);
                    }
                    
                    // add expression
                    if (statementSpec.getSqlParameters() == null) {
                        statementSpec.setSqlParameters(new HashMap<Integer, List<ExprNode>>());
                    }
                    List<ExprNode> listExp = statementSpec.getSqlParameters().get(statementSpec.getStreamSpecs().size());
                    if (listExp == null) {
                        listExp = new ArrayList<ExprNode>();
                        statementSpec.getSqlParameters().put(statementSpec.getStreamSpecs().size(), listExp);
                    }
                    listExp.add(raw.getFilterRootNode());
                }
            }
            catch (PlaceholderParseException ex)
            {
                log.warn("Failed to parse SQL text '" + sqlWithParams + "' :" + ex.getMessage());
                // Let the view construction handle the validation
            }

            String sampleSQL = null;
            if (dbrootNode.getChildCount() > 2)
            {
                sampleSQL = dbrootNode.getChild(2).getText();
                sampleSQL = StringValue.parseString(sampleSQL.trim());
            }

            streamSpec = new DBStatementStreamSpec(streamName, viewSpecs, dbName, sqlWithParams, sampleSQL);
        }
        else if (node.getChild(0).getType() == METHOD_JOIN_EXPR)
        {
            Tree methodRootNode = node.getChild(0);
            String prefixIdent = methodRootNode.getChild(0).getText();
            String className = methodRootNode.getChild(1).getText();

            int indexDot = className.lastIndexOf('.');
            String classNamePart;
            String methodNamePart;
            if (indexDot == -1)
            {
                classNamePart = className;
                methodNamePart = null;
            }
            else
            {
                classNamePart = className.substring(0, indexDot);
                methodNamePart = className.substring(indexDot + 1);
            }
            List<ExprNode> exprNodes = getExprNodes(methodRootNode, 2);

            streamSpec = new MethodStreamSpec(streamName, viewSpecs, prefixIdent, classNamePart, methodNamePart, exprNodes);
        }
        else
        {
            throw new ASTWalkException("Unexpected AST child node to stream expression, type=" + node.getChild(0).getType());
        }
        viewSpecs.clear();
        statementSpec.getStreamSpecs().add(streamSpec);
    }

    private void leaveEventPropertyExpr(Tree node)
    {
        log.debug(".leaveEventPropertyExpr");

        if (node.getChildCount() == 0)
        {
            throw new IllegalStateException("Empty event property expression encountered");
        }

        ExprNode exprNode;
        String propertyName;

        // The stream name may precede the event property name, but cannot be told apart from the property name:
        //      s0.p1 could be a nested property, or could be stream 's0' and property 'p1'

        // A single entry means this must be the property name.
        // And a non-simple property means that it cannot be a stream name.
        if ((node.getChildCount() == 1) || (node.getChild(0).getType() != EVENT_PROP_SIMPLE))
        {
            propertyName = ASTFilterSpecHelper.getPropertyName(node, 0);
            exprNode = new ExprIdentNode(propertyName);
        }
        // --> this is more then one child node, and the first child node is a simple property
        // we may have a stream name in the first simple property, or a nested property
        // i.e. 's0.p0' could mean that the event has a nested property to 's0' of name 'p0', or 's0' is the stream name
        else
        {
            String leadingIdentifier = node.getChild(0).getChild(0).getText();
            String streamOrNestedPropertyName = ASTFilterSpecHelper.escapeDot(leadingIdentifier);
            propertyName = ASTFilterSpecHelper.getPropertyName(node, 1);

            if (variableService.getReader(leadingIdentifier) != null)
            {
                exprNode = new ExprVariableNode(leadingIdentifier + "." + propertyName);
                statementSpec.setHasVariables(true);
                addVariable(statementSpec, propertyName);
            }
            else {
                exprNode = new ExprIdentNode(propertyName, streamOrNestedPropertyName);
            }
        }

        if (variableService.getReader(propertyName) != null)
        {
            exprNode = new ExprVariableNode(propertyName);
            statementSpec.setHasVariables(true);
            addVariable(statementSpec, propertyName);
        }

        astExprNodeMap.put(node, exprNode);
    }

    private void addVariable(StatementSpecRaw statementSpec, String propertyName) {
        if (statementSpec.getReferencedVariables() == null) {
            statementSpec.setReferencedVariables(new HashSet<String>());
        }
        statementSpec.getReferencedVariables().add(propertyName);
    }

    private void leaveLibFunctionOld(Tree parent, Tree node)
    {
    	log.debug(".leaveLibFunctionOld");

        String childNodeText = node.getChild(0).getText();
        if ((childNodeText.toLowerCase().equals("max")) || (childNodeText.toLowerCase().equals("min")))
        {
            handleMinMax(node);
            return;
        }

        if (node.getChild(0).getType() == CLASS_IDENT)
        {
            String className = node.getChild(0).getText();
            List<ExprChainedSpec> chained = getLibFuncChain(parent);
            astExprNodeMap.put(node, new ExprStaticMethodNode(className, chained, configurationInformation.getEngineDefaults().getExpression().isUdfCache()));
            return;
        }

        boolean isDistinct = false;
        if ((node.getChild(1) != null) && (node.getChild(1).getType() == DISTINCT))
        {
            isDistinct = true;
        }

        // try plug-in single-row function
        try
        {
            Pair<Class, String> classMethodPair = engineImportService.resolveSingleRow(childNodeText);
            List<ExprChainedSpec> spec = new ArrayList<ExprChainedSpec>();
            List<ExprNode> childExpressions = new ArrayList<ExprNode>();
            for (int i = 0; i < node.getChildCount(); i++) {
                ExprNode exprnode = astExprNodeMap.remove(node.getChild(i));
                if (exprnode != null) {
                    childExpressions.add(exprnode);
                }
            }
            spec.add(new ExprChainedSpec(classMethodPair.getSecond(), childExpressions));
            astExprNodeMap.put(node, new ExprPlugInSingleRowNode(childNodeText, classMethodPair.getFirst(), spec, false));
            return;
        }
        catch (EngineImportUndefinedException e)
        {
            // Not an single-row function
        }
        catch (EngineImportException e)
        {
            throw new IllegalStateException("Error resolving single-row function: " + e.getMessage(), e);
        }

        // try plug-in aggregation function
        try
        {
            AggregationSupport aggregation = engineImportService.resolveAggregation(childNodeText);

            astExprNodeMap.put(node, new ExprPlugInAggFunctionNode(isDistinct, aggregation, childNodeText));
            return;
        }
        catch (EngineImportUndefinedException e)
        {
            // Not an aggretaion function
        }
        catch (EngineImportException e)
        {
            throw new IllegalStateException("Error resolving aggregation: " + e.getMessage(), e);
        }

        // try built-in expanded set of aggregation functions
        ExprNode extentedBuiltIn = engineImportService.resolveAggExtendedBuiltin(childNodeText, isDistinct);
        if (extentedBuiltIn != null)
        {
            astExprNodeMap.put(node, extentedBuiltIn);
            return;
        }

        throw new IllegalStateException("Unknown single-row function or aggregation function named '" + childNodeText + "' could not be resolved");
    }

    private void leaveDotExpr(Tree node)
    {
    	log.debug(".leaveDotExpr");
        List<ExprChainedSpec> chainSpec = getLibFuncChain(node);
        astExprNodeMap.put(node, new ExprDotNode(chainSpec, configurationInformation.getEngineDefaults().getExpression().isDuckTyping()));
    }

    private void leaveLibFunction(Tree node)
    {
    	log.debug(".leaveLibFunction");

        // Single chain can include a class name or property name.
        // As the current node does not generate any expression for this 1-element chain, forward expression to this node.
        if (node.getChildCount() == 1) {
            leaveLibFunctionOld(node, node.getChild(0));
            mapChildASTToChildExprNode(node.getChild(0));
            ExprNode generated = astExprNodeMap.remove(node.getChild(0));
            astExprNodeMap.put(node, generated);
            return;
        }

        String className = node.getChild(0).getChild(0).getText();
        List<ExprChainedSpec> chained = this.getLibFuncChain(node);

        // try plug-in single-row function
        try
        {
            Pair<Class, String> classMethodPair = engineImportService.resolveSingleRow(className);
            chained.get(0).setName(classMethodPair.getSecond());
            astExprNodeMap.put(node, new ExprPlugInSingleRowNode(className, classMethodPair.getFirst(), chained, false));
            return;
        }
        catch (EngineImportUndefinedException e)
        {
            // Not an single-row function
        }
        catch (EngineImportException e)
        {
            throw new IllegalStateException("Error resolving single-row function: " + e.getMessage(), e);
        }

        // resolve as a static method invocation
        astExprNodeMap.put(node, new ExprStaticMethodNode(className, chained, configurationInformation.getEngineDefaults().getExpression().isUdfCache()));
    }

    private void leaveEqualsExpr(Tree node)
    {
        log.debug(".leaveEqualsExpr");

        boolean isNot = false;
        if (node.getType() == EVAL_NOTEQUALS_EXPR)
        {
            isNot = true;
        }

        ExprEqualsNode identNode = new ExprEqualsNode(isNot);
        astExprNodeMap.put(node, identNode);
    }

    private void leaveEqualsGroupExpr(Tree node)
    {
        log.debug(".leaveEqualsGroupExpr");

        boolean isNot = false;
        if (node.getType() == EVAL_NOTEQUALS_GROUP_EXPR)
        {
            isNot = true;
        }

        boolean isAll = false;
        if (node.getChild(1).getType() == ALL)
        {
            isAll = true;
        }

        if ((node.getChildCount() > 2) && (node.getChild(2).getType() == SUBSELECT_GROUP_EXPR))
        {
            StatementSpecRaw currentSpec = popStacks();
            ExprSubselectAllSomeAnyNode subselectNode = new ExprSubselectAllSomeAnyNode(currentSpec, isNot, isAll, null);
            astExprNodeMap.put(node, subselectNode);               
        }
        else
        {
            ExprEqualsAllAnyNode groupNode = new ExprEqualsAllAnyNode(isNot, isAll);
            astExprNodeMap.put(node, groupNode);
        }
    }

    private void leaveJoinAndExpr(Tree node)
    {
        log.debug(".leaveJoinAndExpr");
        ExprAndNode identNode = new ExprAndNode();
        astExprNodeMap.put(node, identNode);
    }

    private void leaveJoinOrExpr(Tree node)
    {
        log.debug(".leaveJoinOrExpr");
        ExprOrNode identNode = new ExprOrNode();
        astExprNodeMap.put(node, identNode);
    }

    private void leaveConstant(Tree node)
    {
        log.debug(".leaveConstant value '" + node.getText() + "'");
        ExprConstantNode constantNode = new ExprConstantNode(ASTConstantHelper.parse(node));
        astExprNodeMap.put(node, constantNode);
    }

    private void leaveSubstitution(Tree node)
    {
        log.debug(".leaveSubstitution");

        // Add the substitution parameter node, for later replacement
        int currentSize = this.substitutionParamNodes.size();
        ExprSubstitutionNode substitutionNode = new ExprSubstitutionNode(currentSize + 1);
        substitutionParamNodes.add(substitutionNode);

        astExprNodeMap.put(node, substitutionNode);
    }

    private void leaveMath(Tree node)
    {
        log.debug(".leaveMath");

        MathArithTypeEnum mathArithTypeEnum;

        switch (node.getType())
        {
            case DIV :
                mathArithTypeEnum = MathArithTypeEnum.DIVIDE;
                break;
            case STAR :
                mathArithTypeEnum = MathArithTypeEnum.MULTIPLY;
                break;
            case PLUS :
                mathArithTypeEnum = MathArithTypeEnum.ADD;
                break;
            case MINUS :
                mathArithTypeEnum = MathArithTypeEnum.SUBTRACT;
                break;
            case MOD :
                mathArithTypeEnum = MathArithTypeEnum.MODULO;
                break;
            default :
                throw new IllegalArgumentException("Node type " + node.getType() + " not a recognized math node type");
        }

        ExprMathNode mathNode = new ExprMathNode(mathArithTypeEnum,
                configurationInformation.getEngineDefaults().getExpression().isIntegerDivision(),
                configurationInformation.getEngineDefaults().getExpression().isDivisionByZeroReturnsNull());
        astExprNodeMap.put(node, mathNode);
    }

    // Min/Max nodes can be either an aggregate or a per-row function depending on the number or arguments
    private void handleMinMax(Tree libNode)
    {
        log.debug(".handleMinMax");

        // Determine min or max
        Tree childNode = libNode.getChild(0);
        MinMaxTypeEnum minMaxTypeEnum;
        if (childNode.getText().equals("min"))
        {
            minMaxTypeEnum = MinMaxTypeEnum.MIN;
        }
        else if (childNode.getText().equals("max"))
        {
            minMaxTypeEnum = MinMaxTypeEnum.MAX;
        }
        else
        {
            throw new IllegalArgumentException("Node type " + childNode.getType() + ' ' + childNode.getText() + " not a recognized min max node");
        }

        // Determine distinct or not
        Tree nextNode = libNode.getChild(1);
        boolean isDistinct = false;
        if (nextNode.getType() == DISTINCT)
        {
            isDistinct = true;
        }

        // Error if more then 3 nodes with distinct since it's an aggregate function
        if ((libNode.getChildCount() > 3) && (isDistinct))
        {
            throw new ASTWalkException("The distinct keyword is not valid in per-row min and max " +
                    "functions with multiple sub-expressions");
        }

        ExprNode minMaxNode;
        if ((!isDistinct) && (libNode.getChildCount() > 2))
        {
            // use the row function
            minMaxNode = new ExprMinMaxRowNode(minMaxTypeEnum);
        }
        else
        {
            // use the aggregation function
            minMaxNode = new ExprMinMaxAggrNode(isDistinct, minMaxTypeEnum);
        }
        astExprNodeMap.put(libNode, minMaxNode);
    }

    private void leaveCoalesce(Tree node)
    {
        log.debug(".leaveCoalesce");

        ExprNode coalesceNode = new ExprCoalesceNode();
        astExprNodeMap.put(node, coalesceNode);
    }

    private void leaveAggregate(Tree node)
    {
        log.debug(".leaveAggregate");

        boolean isDistinct = false;
        if ((node.getChild(0) != null) && (node.getChild(0).getType() == DISTINCT))
        {
            isDistinct = true;
        }

        ExprAggregateNode aggregateNode;

        switch (node.getType())
        {
            case AVG:
                aggregateNode = new ExprAvgNode(isDistinct);
                break;
            case SUM:
                aggregateNode = new ExprSumNode(isDistinct);
                break;
            case COUNT:
                aggregateNode = new ExprCountNode(isDistinct);
                break;
            case MEDIAN:
                aggregateNode = new ExprMedianNode(isDistinct);
                break;
            case STDDEV:
                aggregateNode = new ExprStddevNode(isDistinct);
                break;
            case AVEDEV:
                aggregateNode = new ExprAvedevNode(isDistinct);
                break;
            case FIRST_AGGREG:
            case WINDOW_AGGREG:
            case LAST_AGGREG:
                boolean isWildcard = ASTUtil.findFirstNode(node, PROPERTY_WILDCARD_SELECT) != null;
                Tree streamWCNode = ASTUtil.findFirstNode(node, PROPERTY_SELECTION_STREAM);
                String streamWildcard = null;
                if (streamWCNode != null) {
                    streamWildcard = streamWCNode.getChild(0).getText();
                }

                if (node.getType() == FIRST_AGGREG) {
                    aggregateNode = new ExprAccessAggNode(AggregationAccessType.FIRST, isWildcard, streamWildcard);
                }
                else if (node.getType() == WINDOW_AGGREG) {
                    aggregateNode = new ExprAccessAggNode(AggregationAccessType.WINDOW, isWildcard, streamWildcard);
                }
                else {
                    aggregateNode = new ExprAccessAggNode(AggregationAccessType.LAST, isWildcard, streamWildcard);
                }
                break;
            default:
                throw new IllegalArgumentException("Node type " + node.getType() + " not a recognized aggregate node type");
        }

        astExprNodeMap.put(node, aggregateNode);
    }

    private void leaveRelationalOp(Tree node)
    {
        log.debug(".leaveRelationalOp");

        RelationalOpEnum relationalOpEnum;

        switch (node.getType())
        {
            case LT :
                relationalOpEnum = RelationalOpEnum.LT;
                break;
            case GT :
                relationalOpEnum = RelationalOpEnum.GT;
                break;
            case LE :
                relationalOpEnum = RelationalOpEnum.LE;
                break;
            case GE :
                relationalOpEnum = RelationalOpEnum.GE;
                break;
            default :
                throw new IllegalArgumentException("Node type " + node.getType() + " not a recognized relational op node type");
        }

        boolean isAll = false;
        boolean isAny = false;
        if (node.getChild(1).getType() == ALL)
        {
            isAll = true;
        }
        if ((node.getChild(1).getType() == ANY) || (node.getChild(1).getType() == SOME))
        {
            isAny = true;
        }

        ExprNode result;
        if (isAll || isAny)
        {
            if ((node.getChildCount() > 2) && (node.getChild(2).getType() == SUBSELECT_GROUP_EXPR))
            {
                StatementSpecRaw currentSpec = popStacks();
                result = new ExprSubselectAllSomeAnyNode(currentSpec, false, isAll, relationalOpEnum);
            }
            else
            {
                result = new ExprRelationalOpAllAnyNode(relationalOpEnum, isAll);
            }
        }
        else
        {
            result = new ExprRelationalOpNode(relationalOpEnum);
        }
        
        astExprNodeMap.put(node, result);
    }

    private void leaveBitWise(Tree node)
    {
        log.debug(".leaveBitWise");

        BitWiseOpEnum bitWiseOpEnum;
        switch (node.getType())
        {
	        case BAND :
	        	bitWiseOpEnum = BitWiseOpEnum.BAND;
	            break;
	        case BOR :
	        	bitWiseOpEnum = BitWiseOpEnum.BOR;
	            break;
	        case BXOR :
	        	bitWiseOpEnum = BitWiseOpEnum.BXOR;
	            break;
	        default :
	            throw new IllegalArgumentException("Node type " + node.getType() + " not a recognized bit wise node type");
        }

	    ExprBitWiseNode bwNode = new ExprBitWiseNode(bitWiseOpEnum);
	    astExprNodeMap.put(node, bwNode);
    }

    private void leaveWhereClause()
    {
        log.debug(".leaveWhereClause");

        if (astExprNodeMap.size() != 1)
        {
            throw new IllegalStateException("Where clause generated zero or more then one expression nodes");
        }

        // Just assign the single root ExprNode not consumed yet
        statementSpec.setFilterRootNode(astExprNodeMap.values().iterator().next());
        astExprNodeMap.clear();
    }

    private void leaveHavingClause()
    {
        log.debug(".leaveHavingClause");

        if (astExprNodeMap.size() != 1)
        {
            throw new IllegalStateException("Having clause generated zero or more then one expression nodes");
        }

        // Just assign the single root ExprNode not consumed yet
        statementSpec.setHavingExprRootNode(astExprNodeMap.values().iterator().next());
        astExprNodeMap.clear();
    }

    private void leaveOutputLimit(Tree node) throws ASTWalkException
    {
        log.debug(".leaveOutputLimit");

        OutputLimitSpec spec = ASTOutputLimitHelper.buildOutputLimitSpec(node, astExprNodeMap, variableService, engineURI, timeProvider, exprEvaluatorContext);
        statementSpec.setOutputLimitSpec(spec);

        if (spec.getVariableName() != null)
        {
            statementSpec.setHasVariables(true);
            addVariable(statementSpec, spec.getVariableName());
        }
    }

    private void leaveRowLimit(Tree node) throws ASTWalkException
    {
        log.debug(".leaveRowLimit");

        RowLimitSpec spec = ASTOutputLimitHelper.buildRowLimitSpec(node);
        statementSpec.setRowLimitSpec(spec);

        if ((spec.getNumRowsVariable() != null) || (spec.getOptionalOffsetVariable() != null))
        {
            statementSpec.setHasVariables(true);
            addVariable(statementSpec, spec.getOptionalOffsetVariable());
        }
    }

    private void leaveOuterInnerJoin(Tree node)
    {
        log.debug(".leaveOuterInnerJoin");

        OuterJoinType joinType;
        switch (node.getType())
        {
            case LEFT_OUTERJOIN_EXPR:
                joinType = OuterJoinType.LEFT;
                break;
            case RIGHT_OUTERJOIN_EXPR:
                joinType = OuterJoinType.RIGHT;
                break;
            case FULL_OUTERJOIN_EXPR:
                joinType = OuterJoinType.FULL;
                break;
            case INNERJOIN_EXPR:
                joinType = OuterJoinType.INNER;
                break;
            default:
                throw new IllegalArgumentException("Node type " + node.getType() + " not a recognized outer join node type");
        }

        // get subnodes representing the expression
        ExprIdentNode left = (ExprIdentNode) astExprNodeMap.get(node.getChild(0));
        ExprIdentNode right = (ExprIdentNode) astExprNodeMap.get(node.getChild(1));

        // remove from AST-to-expression node map
        astExprNodeMap.remove(node.getChild(0));
        astExprNodeMap.remove(node.getChild(1));

        // get optional additional
        ExprIdentNode[] addLeftArr = null;
        ExprIdentNode[] addRightArr = null;
        if (node.getChildCount() > 2)
        {
            ArrayList<ExprIdentNode> addLeft = new ArrayList<ExprIdentNode>();
            ArrayList<ExprIdentNode> addRight = new ArrayList<ExprIdentNode>();
            for (int i = 2; i < node.getChildCount(); i+=2)
            {
                Tree child = node.getChild(i);
                addLeft.add((ExprIdentNode)astExprNodeMap.remove(child));
                addRight.add((ExprIdentNode)astExprNodeMap.remove(node.getChild(i + 1)));
            }
            addLeftArr = addLeft.toArray(new ExprIdentNode[addLeft.size()]);
            addRightArr = addRight.toArray(new ExprIdentNode[addRight.size()]);
        }

        OuterJoinDesc outerJoinDesc = new OuterJoinDesc(joinType, left, right, addLeftArr, addRightArr);
        statementSpec.getOuterJoinDescList().add(outerJoinDesc);
    }

    private void leaveGroupBy(Tree node)
    {
        log.debug(".leaveGroupBy");

        // there must be some expressions under the group by in our map
        if (astExprNodeMap.size() < 1)
        {
            throw new IllegalStateException("Group-by clause generated no expression nodes");
        }

        // For each child to the group-by AST node there must be a generated ExprNode
        for (int i = 0; i < node.getChildCount(); i++)
        {
        	Tree child = node.getChild(i);
            // get top expression node for the child node
            ExprNode exprNode = astExprNodeMap.get(child);

            if (exprNode == null)
            {
                throw new IllegalStateException("Expression node as a result of group-by child node not found in collection");
            }

            statementSpec.getGroupByExpressions().add(exprNode);
        }

        // Clear the map - all expression node should be gone
        astExprNodeMap.clear();
    }

    private void leaveInsertInto(Tree node)
    {
        log.debug(".leaveInsertInto");

        int count = 0;
        Tree child = node.getChild(count);

        // istream or rstream
        boolean isIStream = true;
        if (child.getType() == RSTREAM)
        {
            isIStream = false;
            child = node.getChild(++count);
        }
        if (child.getType() == ISTREAM)
        {
            child = node.getChild(++count);
        }

        // type name
        String eventTypeName = child.getText();
        InsertIntoDesc insertIntoDesc = new InsertIntoDesc(isIStream, eventTypeName);

        // optional columns
        child = node.getChild(++count);
        if ((child != null) && (child.getType() == EXPRCOL))
        {
            // Each child to the insert-into AST node represents a column name
            for (int i = 0; i < child.getChildCount(); i++)
            {
                Tree childNode = child.getChild(i);
                insertIntoDesc.add(childNode.getText());
            }
        }

        statementSpec.setInsertIntoDesc(insertIntoDesc);
    }

    private void leaveOrderByElement(Tree node) throws ASTWalkException
    {
        log.debug(".leaveOrderByElement");
        if ((astExprNodeMap.size() > 1) || ((astExprNodeMap.isEmpty())))
        {
            throw new ASTWalkException("Unexpected AST tree contains zero or more then 1 child element for root");
        }

        // Get expression node sub-tree from the AST nodes placed so far
        ExprNode exprNode = astExprNodeMap.values().iterator().next();
        astExprNodeMap.clear();

        // Get optional ascending or descending qualifier
        boolean descending = false;
        if (node.getChildCount() > 1)
        {
            descending = node.getChild(1).getType() == DESC;
        }

        // Add as order-by element
        statementSpec.getOrderByList().add(new OrderByItem(exprNode, descending));
    }

    private void leaveConcat(Tree node)
    {
        ExprConcatNode concatNode = new ExprConcatNode();
        astExprNodeMap.put(node, concatNode);
    }

    private void leaveEvery(Tree node)
    {
        log.debug(".leaveEvery");
        EvalEveryNode everyNode = this.patternNodeFactory.makeEveryNode();
        astPatternNodeMap.put(node, everyNode);
    }

    private void leaveEveryDistinct(Tree node)
    {
        log.debug(".leaveEveryDistinct");
        List<ExprNode> exprNodes = getExprNodes(node.getChild(0), 0);
        EvalEveryDistinctNode everyNode = this.patternNodeFactory.makeEveryDistinctNode(exprNodes);
        astPatternNodeMap.put(node, everyNode);
    }

    private void leaveStreamFilter(Tree node)
    {
        log.debug(".leaveStreamFilter");

        int count = 0;
        Tree startNode = node.getChild(0);
        if (startNode.getType() == IDENT)
        {
            startNode = node.getChild(++count);
        }

        // Determine event type
        String eventName = startNode.getText();
        count++;

        // get property expression if any
        if ((node.getChildCount() > count) && (node.getChild(count).getType() == EVENT_FILTER_PROPERTY_EXPR))
        {
            ++count;
        }

        List<ExprNode> exprNodes = getExprNodes(node, count);

        FilterSpecRaw rawFilterSpec = new FilterSpecRaw(eventName, exprNodes, propertyEvalSpec);
        propertyEvalSpec = null;
        // for event streams we keep the filter spec around for use when the stream definition is completed
        filterSpec = rawFilterSpec;

        // clear the sub-nodes for the filter since the event property expressions have been processed
        // by building the spec
        astExprNodeMap.clear();
    }

    private void leavePatternFilter(Tree node)
    {
        log.debug(".leavePatternFilter");

        int count = 0;
        Tree startNode = node.getChild(0);
        String optionalPatternTagName = null;
        if (startNode.getType() == IDENT)
        {
            optionalPatternTagName = startNode.getText();
            startNode = node.getChild(++count);
        }

        // Determine event type
        String eventName = startNode.getText();
        count++;

        // get property expression if any
        if ((node.getChildCount() > count) && (node.getChild(count).getType() == EVENT_FILTER_PROPERTY_EXPR))
        {
            ++count;
        }

        List<ExprNode> exprNodes = getExprNodes(node, count);

        FilterSpecRaw rawFilterSpec = new FilterSpecRaw(eventName, exprNodes, propertyEvalSpec);
        propertyEvalSpec = null;
        EvalFilterNode filterNode = patternNodeFactory.makeFilterNode(rawFilterSpec, optionalPatternTagName);
        astPatternNodeMap.put(node, filterNode);
    }

    private void leaveFollowedBy(Tree node)
    {
        log.debug(".leaveFollowedBy");
        ExprNode[] maxExpressions = new ExprNode[node.getChildCount() - 1];
        List<EvalNode> childNodes = new ArrayList<EvalNode>();
        for (int i = 0; i < node.getChildCount(); i++) {
            Tree child = node.getChild(i);
            if (child.getType() != FOLLOWED_BY_ITEM) {
                throw new ASTWalkException("Unexpected child node for followed-by item");
            }
            if (i == 0) {
                childNodes.add(astPatternNodeMap.remove(child.getChild(0)));    // first pattern sub-expression cannot have max
            }
            else {
                int current = 0;
                if (child.getChildCount() == 2) {
                    maxExpressions[i - 1] = astExprNodeMap.remove(child.getChild(current));
                    current++;
                }
                childNodes.add(astPatternNodeMap.remove(child.getChild(current)));
            }
        }
        List<ExprNode> expressions = Arrays.asList(maxExpressions); // can contain null elements as max/no-max can be mixed
        EvalFollowedByNode fbNode = patternNodeFactory.makeFollowedByNode(expressions);
        fbNode.getChildNodes().addAll(childNodes);
        astPatternNodeMap.put(node, fbNode);
    }

    private void leaveAnd(Tree node)
    {
        log.debug(".leaveAnd");
        EvalAndNode andNode = patternNodeFactory.makeAndNode();
        astPatternNodeMap.put(node, andNode);
    }

    private void leaveOr(Tree node)
    {
        log.debug(".leaveOr");
        EvalOrNode orNode = patternNodeFactory.makeOrNode();
        astPatternNodeMap.put(node, orNode);
    }

    private void leaveInSet(Tree node)
    {
        log.debug(".leaveInSet");

        ExprInNode inNode = new ExprInNode(node.getType() == NOT_IN_SET);
        astExprNodeMap.put(node, inNode);
    }

    private void leaveInRange(Tree node)
    {
        log.debug(".leaveInRange");

        // The second node must be braces
        Tree bracesNode = node.getChild(1);
        if ((bracesNode.getType() != LBRACK) && ((bracesNode.getType() != LPAREN)))
        {
            throw new IllegalStateException("Invalid in-range syntax, no braces but type '" + bracesNode.getType() + "'");
        }
        boolean isLowInclude = bracesNode.getType() == LBRACK;

        // The fifth node must be braces
        bracesNode = node.getChild(4);
        if ((bracesNode.getType() != RBRACK) && ((bracesNode.getType() != RPAREN)))
        {
            throw new IllegalStateException("Invalid in-range syntax, no braces but type '" + bracesNode.getType() + "'");
        }
        boolean isHighInclude = bracesNode.getType() == RBRACK;

        ExprBetweenNode betweenNode = new ExprBetweenNode(isLowInclude, isHighInclude, node.getType() == NOT_IN_RANGE);
        astExprNodeMap.put(node, betweenNode);
    }

    private void leaveBetween(Tree node)
    {
        log.debug(".leaveBetween");

        ExprBetweenNode betweenNode = new ExprBetweenNode(true, true, node.getType() == NOT_BETWEEN);
        astExprNodeMap.put(node, betweenNode);
    }

    private void leaveLike(Tree node)
    {
        log.debug(".leaveLike");

        boolean isNot = node.getType() == NOT_LIKE;
        ExprLikeNode likeNode = new ExprLikeNode(isNot);
        astExprNodeMap.put(node, likeNode);
    }

    private void leaveRegexp(Tree node)
    {
        log.debug(".leaveRegexp");

        boolean isNot = node.getType() == NOT_REGEXP;
        ExprRegexpNode regExpNode = new ExprRegexpNode(isNot);
        astExprNodeMap.put(node, regExpNode);
    }

    private void leaveExprNot(Tree node)
    {
        log.debug(".leaveExprNot");
        ExprNotNode notNode = new ExprNotNode();
        astExprNodeMap.put(node, notNode);
    }

    private void leavePatternNot(Tree node)
    {
        log.debug(".leavePatternNot");
        EvalNotNode notNode = this.patternNodeFactory.makeNotNode();
        astPatternNodeMap.put(node, notNode);
    }

    private void leaveGuard(Tree node) throws ASTWalkException
    {
        log.debug(".leaveGuard");
        String objectNamespace;
        String objectName;
        List<ExprNode> obsParameters;
        if (node.getChild(1).getType() == IDENT && node.getChild(2).getType() == IDENT) {
            objectNamespace = node.getChild(1).getText();
            objectName = node.getChild(2).getText();
            obsParameters = getExprNodes(node, 3);
        }
        else {
            objectNamespace = GuardEnum.WHILE_GUARD.getNamespace();
            objectName = GuardEnum.WHILE_GUARD.getName();
            obsParameters = getExprNodes(node, 1);
        }

        PatternGuardSpec guardSpec = new PatternGuardSpec(objectNamespace, objectName, obsParameters);
        EvalGuardNode guardNode = patternNodeFactory.makeGuardNode(guardSpec);
        astPatternNodeMap.put(node, guardNode);
    }

    private void leaveCaseNode(Tree node, boolean inCase2)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".leaveCase2Node inCase2=" + inCase2);
        }

        if (astExprNodeMap.isEmpty())
        {
            throw new ASTWalkException("Unexpected AST tree contains zero child element for case node");
        }
        if (astExprNodeMap.size() == 1)
        {
            throw new ASTWalkException("AST tree doesn not contain at least when node for case node");
        }

        ExprCaseNode caseNode = new ExprCaseNode(inCase2);
        astExprNodeMap.put(node, caseNode);
    }

    private void leaveObserver(Tree node) throws ASTWalkException
    {
        log.debug(".leaveObserver");

        // Get the object information from AST
        String objectNamespace = node.getChild(0).getText();
        String objectName = node.getChild(1).getText();
        List<ExprNode> obsParameters = getExprNodes(node, 2);

        PatternObserverSpec observerSpec = new PatternObserverSpec(objectNamespace, objectName, obsParameters);
        EvalObserverNode observerNode = this.patternNodeFactory.makeObserverNode(observerSpec);
        astPatternNodeMap.put(node, observerNode);
    }

    private void leaveMatch(Tree node) throws ASTWalkException
    {
        log.debug(".leaveMatch");

        boolean hasRange = true;
        int type = node.getChild(0).getType();
        ExprNode low = null;
        ExprNode high = null;
        boolean allowZeroLowerBounds = false;
        if (type == MATCH_UNTIL_RANGE_HALFOPEN) // [expr:]
        {
            low = astExprNodeMap.remove(node.getChild(0).getChild(0));
        }
        else if (type == MATCH_UNTIL_RANGE_HALFCLOSED) // [:expr]
        {
            high = astExprNodeMap.remove(node.getChild(0).getChild(0));
        }
        else if (type == MATCH_UNTIL_RANGE_BOUNDED) // [expr]
        {
            low = astExprNodeMap.remove(node.getChild(0).getChild(0));
            high = low;
        }
        else if (type == MATCH_UNTIL_RANGE_CLOSED) // [expr:expr]
        {
            low = astExprNodeMap.remove(node.getChild(0).getChild(0));
            high = astExprNodeMap.remove(node.getChild(0).getChild(1));
            allowZeroLowerBounds = true;
        }
        else
        {
            hasRange = false;
        }

        boolean tightlyBound = ASTMatchUntilHelper.validate(low, high, allowZeroLowerBounds);
        if ((node.getChildCount() == 2) && (hasRange) && (!tightlyBound))
        {
            throw new ASTWalkException("Variable bounds repeat operator requires an until-expression");            
        }

        EvalMatchUntilNode fbNode = this.patternNodeFactory.makeMatchUntilNode(low, high);
        astPatternNodeMap.put(node, fbNode);
    }

    private void leaveSelectClause(Tree node)
    {
        log.debug(".leaveSelectClause");

        int nodeType = node.getChild(0).getType();
        if (nodeType == RSTREAM)
        {
            statementSpec.setSelectStreamDirEnum(SelectClauseStreamSelectorEnum.RSTREAM_ONLY);
        }
        if (nodeType == ISTREAM)
        {
            statementSpec.setSelectStreamDirEnum(SelectClauseStreamSelectorEnum.ISTREAM_ONLY);
        }
        if (nodeType == IRSTREAM)
        {
            statementSpec.setSelectStreamDirEnum(SelectClauseStreamSelectorEnum.RSTREAM_ISTREAM_BOTH);
        }

        boolean isDistinct = false;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == DISTINCT)
            {
                isDistinct = true;
            }
        }
        statementSpec.getSelectClauseSpec().setDistinct(isDistinct);
    }

    private ExprNode getRemoveFirstByType(Tree parent, int type) {
        ExprNode exprNode = null;
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            if (parent.getChild(i).getType() == type)
            {
                exprNode = astExprNodeMap.get(parent.getChild(i).getChild(0));
                if (exprNode == null)
                {
                    throw new IllegalStateException("Expression node for AST node not found for type " + parent.getChild(i).getType() + " and text " + parent.getChild(i).getText());
                }
                astExprNodeMap.remove(parent.getChild(i));
            }
        }
        return exprNode;
    }

    private List<ExprNode> getExprNodes(Tree parentNode, int startIndex)
    {
        List<ExprNode> exprNodes = new LinkedList<ExprNode>();

        for (int i = startIndex; i < parentNode.getChildCount(); i++)
        {
        	Tree currentNode = parentNode.getChild(i);
            ExprNode exprNode = astExprNodeMap.get(currentNode);
            if (exprNode == null)
            {
                throw new IllegalStateException("Expression node for AST node not found for type " + currentNode.getType() + " and text " + currentNode.getText());
            }
            exprNodes.add(exprNode);
            astExprNodeMap.remove(currentNode);
        }
        return exprNodes;
    }

    private boolean isSelectInsertFirst(Tree child)
    {
        for (int i = 0; i < child.getChildCount(); i++)
        {
            if (child.getChild(i).getType() == ON_SELECT_INSERT_OUTPUT)
            {
                if (child.getChild(i).getChild(0).getType() == ALL)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private List<ExprChainedSpec> getLibFuncChain(Tree parent) {

        List<ExprChainedSpec> chained = new ArrayList<ExprChainedSpec>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            Tree chainElement = parent.getChild(i);
            if (chainElement.getType() != LIB_FUNCTION) {
                continue;
            }

            int count = 0;
            if (chainElement.getChild(0).getType() == CLASS_IDENT)
            {
                count++;
            }

            String methodName = ASTConstantHelper.removeTicks(chainElement.getChild(count).getText());
            count++;

            List<ExprNode> parameters = new ArrayList<ExprNode>();
            for (int exprNum = count; exprNum < chainElement.getChildCount(); exprNum++) {
                ExprNode parameter = astExprNodeMap.remove(chainElement.getChild(exprNum));
                parameters.add(parameter);
            }
            chained.add(new ExprChainedSpec(methodName, parameters));
        }
        return chained;
    }

    private List<String> getIdentList(Tree node) {
        List<String> columsList = new ArrayList<String>();
        for (int i = 0; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == IDENT)
            {
                columsList.add(node.getChild(i).getText());
            }
        }
        return columsList;
    }

    private static final Log log = LogFactory.getLog(EPLTreeWalker.class);
}

/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.schedule.TimeProvider;

import java.util.*;

/**
 * Utility functions around handling expressions.
 */
public class ExprNodeUtility
{
    public static Set<Pair<Integer, String>> getNonAggregatedProps(List<ExprNode> exprNodes)
    {
        // Determine all event properties in the clause
        Set<Pair<Integer, String>> nonAggProps = new HashSet<Pair<Integer, String>>();
        for (ExprNode node : exprNodes)
        {
            ExprNodeIdentifierVisitor visitor = new ExprNodeIdentifierVisitor(false);
            node.accept(visitor);
            List<Pair<Integer, String>> propertiesNode = visitor.getExprProperties();
            nonAggProps.addAll(propertiesNode);
        }

        return nonAggProps;
    }

    public static Set<Pair<Integer, String>> getAggregatedProperties(List<ExprAggregateNode> aggregateNodes)
    {
        // Get a list of properties being aggregated in the clause.
        Set<Pair<Integer, String>> propertiesAggregated = new HashSet<Pair<Integer, String>>();
        for (ExprNode selectAggExprNode : aggregateNodes)
        {
            ExprNodeIdentifierVisitor visitor = new ExprNodeIdentifierVisitor(true);
            selectAggExprNode.accept(visitor);
            List<Pair<Integer, String>> properties = visitor.getExprProperties();
            propertiesAggregated.addAll(properties);
        }

        return propertiesAggregated;
    }

    public static ExprEvaluator[] getEvaluators(ExprNode[] exprNodes) {
        if (exprNodes == null) {
            return null;
        }
        ExprEvaluator[] eval = new ExprEvaluator[exprNodes.length];
        for (int i = 0; i < exprNodes.length; i++) {
            ExprNode node = exprNodes[i];
            if (node != null) {
                eval[i] = node.getExprEvaluator();
            }
        }
        return eval;
    }

    public static ExprEvaluator[] getEvaluators(List<ExprNode> childNodes)
    {
        ExprEvaluator[] eval = new ExprEvaluator[childNodes.size()];
        for (int i = 0; i < childNodes.size(); i++) {
            eval[i] = childNodes.get(i).getExprEvaluator();
        }
        return eval;
    }

    public static Set<Integer> getIdentStreamNumbers(ExprNode child) {

        Set<Integer> streams = new HashSet<Integer>();
        ExprNodeIdentifierCollectVisitor visitor = new ExprNodeIdentifierCollectVisitor();
        child.accept(visitor);
        for (ExprIdentNode node : visitor.getExprProperties()) {
            streams.add(node.getStreamId());
        }
        return streams;
    }

    /**
     * Returns true if all properties within the expression are witin data window'd streams.
     * @param child expression to interrogate
     * @param streamTypeService streams
     * @return indicator
     */
    public static boolean hasRemoveStream(ExprNode child, StreamTypeService streamTypeService) {

        // Determine whether all streams are istream-only or irstream
        boolean[] isIStreamOnly = streamTypeService.getIStreamOnly();
        boolean isAllIStream = true;    // all true?
        boolean isAllIRStream = true;   // all false?
        for (boolean anIsIStreamOnly : isIStreamOnly) {
            if (!anIsIStreamOnly) {
                isAllIStream = false;
            }
            else {
                isAllIRStream = false;
            }
        }

        // determine if a data-window applies to this max function
        boolean hasDataWindows = true;
        if (isAllIStream) {
            hasDataWindows = false;
        }
        else if (!isAllIRStream) {
            hasDataWindows = false;
            // get all aggregated properties to determine if any is from a windowed stream
            ExprNodeIdentifierCollectVisitor visitor = new ExprNodeIdentifierCollectVisitor();
            child.accept(visitor);
            for (ExprIdentNode node : visitor.getExprProperties()) {
                if (!isIStreamOnly[node.getStreamId()]) {
                    hasDataWindows = true;
                    break;
                }
            }
        }

        return hasDataWindows;
    }


    /**
     * Apply a filter expression.
     * @param filter expression
     * @param streamZeroEvent the event that represents stream zero
     * @param streamOneEvents all events thate are stream one events
     * @param exprEvaluatorContext context for expression evaluation
     * @return filtered stream one events
     */
    public static EventBean[] applyFilterExpression(ExprEvaluator filter, EventBean streamZeroEvent, EventBean[] streamOneEvents, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean[] eventsPerStream = new EventBean[2];
        eventsPerStream[0] = streamZeroEvent;

        EventBean[] filtered = new EventBean[streamOneEvents.length];
        int countPass = 0;

        for (EventBean eventBean : streamOneEvents)
        {
            eventsPerStream[1] = eventBean;

            Boolean result = (Boolean) filter.evaluate(eventsPerStream, true, exprEvaluatorContext);
            if ((result != null) && result)
            {
                filtered[countPass] = eventBean;
                countPass++;
            }
        }

        if (countPass == streamOneEvents.length)
        {
            return streamOneEvents;
        }
        return EventBeanUtility.resizeArray(filtered, countPass);
    }

    /**
     * Apply a filter expression returning a pass indicator.
     * @param filter to apply
     * @param eventsPerStream events per stream
     * @param exprEvaluatorContext context for expression evaluation
     * @return pass indicator
     */
    public static boolean applyFilterExpression(ExprEvaluator filter, EventBean[] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        Boolean result = (Boolean) filter.evaluate(eventsPerStream, true, exprEvaluatorContext);
        return (result != null) && result;
    }

    /**
     * Compare two expression nodes and their children in exact child-node sequence,
     * returning true if the 2 expression nodes trees are equals, or false if they are not equals.
     * <p>
     * Recursive call since it uses this method to compare child nodes in the same exact sequence.
     * Nodes are compared using the equalsNode method.
     * @param nodeOne - first expression top node of the tree to compare
     * @param nodeTwo - second expression top node of the tree to compare
     * @return false if this or all child nodes are not equal, true if equal
     */
    public static boolean deepEquals(ExprNode nodeOne, ExprNode nodeTwo)
    {
        if (nodeOne.getChildNodes().size() != nodeTwo.getChildNodes().size())
        {
            return false;
        }
        if (!nodeOne.equalsNode(nodeTwo))
        {
            return false;
        }
        for (int i = 0; i < nodeOne.getChildNodes().size(); i++)
        {
            ExprNode childNodeOne = nodeOne.getChildNodes().get(i);
            ExprNode childNodeTwo = nodeTwo.getChildNodes().get(i);

            if (!ExprNodeUtility.deepEquals(childNodeOne, childNodeTwo))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two expression nodes via deep comparison, considering all
     * child nodes of either side.
     * @param one array of expressions
     * @param two array of expressions
     * @return true if the expressions are equal, false if not
     */
    public static boolean deepEquals(ExprNode[] one, ExprNode[] two)
    {
        if (one.length != two.length)
        {
            return false;
        }
        for (int i = 0; i < one.length; i++)
        {
            if (!ExprNodeUtility.deepEquals(one[i], two[i]))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean deepEquals(List<ExprNode> one, List<ExprNode> two)
    {
        if (one.size() != two.size())
        {
            return false;
        }
        for (int i = 0; i < one.size(); i++)
        {
            if (!ExprNodeUtility.deepEquals(one.get(i), two.get(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the expression is minimal: does not have a subselect, aggregation and does not need view resources
     * @param expression to inspect
     * @return null if minimal, otherwise name of offending sub-expression
     */
    public static String isMinimalExpression(ExprNode expression)
    {
        ExprNodeSubselectVisitor subselectVisitor = new ExprNodeSubselectVisitor();
        expression.accept(subselectVisitor);
        if (subselectVisitor.getSubselects().size() > 0)
        {
            return "a subselect";
        }

        ExprNodeViewResourceVisitor viewResourceVisitor = new ExprNodeViewResourceVisitor();
        expression.accept(viewResourceVisitor);
        if (viewResourceVisitor.getExprNodes().size() > 0)
        {
            return "a function that requires view resources (prior, prev)";
        }

        List<ExprAggregateNode> aggregateNodes = new LinkedList<ExprAggregateNode>();
        ExprAggregateNode.getAggregatesBottomUp(expression, aggregateNodes);
        if (!aggregateNodes.isEmpty())
        {
            return "an aggregation function";
        }
        return null;
    }

    protected static void toExpressionString(List<ExprChainedSpec> chainSpec, StringBuilder buffer)
    {
        for (ExprChainedSpec element : chainSpec) {
            buffer.append(".");
            buffer.append(element.getName());
            buffer.append("(");

            String delimiter = "";
            for (ExprNode param : element.getParameters()) {
                buffer.append(delimiter);
                delimiter = ", ";
                buffer.append(param.toExpressionString());
            }
            buffer.append(")");
        }
    }


    public static void validate(List<ExprChainedSpec> chainSpec, StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException {

        // validate all parameters
        for (ExprChainedSpec chainElement : chainSpec) {
            List<ExprNode> validated = new ArrayList<ExprNode>();
            for (ExprNode expr : chainElement.getParameters()) {
                validated.add(expr.getValidatedSubtree(streamTypeService, methodResolutionService, viewResourceDelegate, timeProvider, variableService, exprEvaluatorContext));
            }
            chainElement.setParameters(validated);
        }
    }

    public static List<ExprNode> collectChainParameters(List<ExprChainedSpec> chainSpec) {
        List<ExprNode> result = new ArrayList<ExprNode>();
        for (ExprChainedSpec chainElement : chainSpec) {
            result.addAll(chainElement.getParameters());
        }
        return result;
    }
}
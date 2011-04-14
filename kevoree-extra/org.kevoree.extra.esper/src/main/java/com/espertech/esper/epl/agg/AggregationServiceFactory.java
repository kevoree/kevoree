/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.annotation.Hint;
import com.espertech.esper.client.annotation.HintEnum;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.util.CollectionUtil;
import com.espertech.esper.view.StatementStopService;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Factory for aggregation service instances.
 * <p>
 * Consolidates aggregation nodes such that result futures point to a single instance and
 * no re-evaluation of the same result occurs.
 */
public class AggregationServiceFactory
{
    /**
     * Produces an aggregation service for use with match-recognice.
     * @param numStreams number of streams
     * @param measureExprNodesPerStream measure nodes
     * @param methodResolutionService method resolution
     * @param exprEvaluatorContext context for expression evaluatiom
     * @return service
     */
    public static AggregationServiceMatchRecognize getServiceMatchRecognize(int numStreams, Map<Integer, List<ExprAggregateNode>> measureExprNodesPerStream,
                                                       MethodResolutionService methodResolutionService,
                                                       ExprEvaluatorContext exprEvaluatorContext)
    {
        Map<Integer, List<ExprAggDesc>> equivalencyListPerStream = new HashMap<Integer, List<ExprAggDesc>>();

        for (Map.Entry<Integer, List<ExprAggregateNode>> entry : measureExprNodesPerStream.entrySet())
        {
            List<ExprAggDesc> equivalencyList = new ArrayList<ExprAggDesc>();
            equivalencyListPerStream.put(entry.getKey(), equivalencyList);
            for (ExprAggregateNode selectAggNode : entry.getValue())
            {
                addEquivalent(selectAggNode, equivalencyList);
            }
        }

        LinkedHashMap<Integer, AggregationMethod[]> aggregatorsPerStream = new LinkedHashMap<Integer, AggregationMethod[]>();
        Map<Integer, ExprEvaluator[]> evaluatorsPerStream = new HashMap<Integer, ExprEvaluator[]>();

        for (Map.Entry<Integer, List<ExprAggDesc>> equivalencyPerStream : equivalencyListPerStream.entrySet())
        {
            int index = 0;
            int stream = equivalencyPerStream.getKey();

            AggregationMethod[] aggregators = new AggregationMethod[equivalencyPerStream.getValue().size()];
            aggregatorsPerStream.put(stream, aggregators);

            ExprEvaluator[] evaluators = new ExprEvaluator[equivalencyPerStream.getValue().size()];
            evaluatorsPerStream.put(stream, evaluators);

            for (ExprAggDesc aggregation : equivalencyPerStream.getValue())
            {
                ExprAggregateNode aggregateNode = aggregation.getAggregationNode();
                if (aggregateNode.getChildNodes().size() > 1)
                {
                    evaluators[index] = getMultiNodeEvaluator(aggregateNode.getChildNodes(), exprEvaluatorContext);
                }
                else if (!aggregateNode.getChildNodes().isEmpty())
                {
                    // Use the evaluation node under the aggregation node to obtain the aggregation value
                    evaluators[index] = aggregateNode.getChildNodes().get(0).getExprEvaluator();
                }
                // For aggregation that doesn't evaluate any particular sub-expression, return null on evaluation
                else
                {
                    evaluators[index] = new ExprEvaluator() {
                        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                        {
                            return null;
                        }

                        public Class getType()
                        {
                            return null;
                        }
                        public Map<String, Object> getEventType() {
                            return null;
                        }
                    };
                }

                aggregators[index] = aggregateNode.getFactory().getPrototypeAggregator(methodResolutionService);
                index++;
            }
        }

        // Assign a column number to each aggregation node. The regular aggregation goes first followed by access-aggregation.
        int columnNumber = 0;
        for (Map.Entry<Integer, List<ExprAggDesc>> equivalencyPerStream : equivalencyListPerStream.entrySet())
        {
            for (ExprAggDesc entry : equivalencyPerStream.getValue())
            {
                entry.setColumnNum(columnNumber++);
            }
        }

        AggregationServiceMatchRecognizeImpl service = new AggregationServiceMatchRecognizeImpl(numStreams, aggregatorsPerStream, evaluatorsPerStream);

        // Hand a service reference to the aggregation nodes themselves.
        // Thus on expression evaluation time each aggregate node calls back to find out what the
        // group's state is (and thus does not evaluate by asking its child node for its result).
        for (Map.Entry<Integer, List<ExprAggDesc>> equivalencyPerStream : equivalencyListPerStream.entrySet())
        {
            for (ExprAggDesc aggregation : equivalencyPerStream.getValue())
            {
                aggregation.assignFuture(service);
            }
        }

        return service;
    }

    /**
     * Returns an instance to handle the aggregation required by the aggregation expression nodes, depending on
     * whether there are any group-by nodes.
     * @param selectAggregateExprNodes - aggregation nodes extracted out of the select expression
     * @param havingAggregateExprNodes - aggregation nodes extracted out of the select expression
     * @param orderByAggregateExprNodes - aggregation nodes extracted out of the select expression
     * @param hasGroupByClause - indicator on whethere there is group-by required, or group-all
     * @param methodResolutionService - is required to resolve aggregation methods
     * @param exprEvaluatorContext context for expression evaluatiom
     * @param annotations - statement annotations
     * @param variableService - variable
     * @param statementStopService - stop callbacks
     * @param isJoin - true for joins
     * @param whereClause the where-clause function if any
     * @param havingClause the having-clause function if any
     * @return instance for aggregation handling
     * @throws com.espertech.esper.epl.expression.ExprValidationException if validation fails
     */
    public static AggregationService getService(List<ExprAggregateNode> selectAggregateExprNodes,
                                                List<ExprAggregateNode> havingAggregateExprNodes,
                                                List<ExprAggregateNode> orderByAggregateExprNodes,
                                                boolean hasGroupByClause,
                                                MethodResolutionService methodResolutionService,
                                                ExprEvaluatorContext exprEvaluatorContext,
                                                Annotation[] annotations,
                                                VariableService variableService,
                                                StatementStopService statementStopService,
                                                boolean isJoin,
                                                ExprNode whereClause,
                                                ExprNode havingClause)
            throws ExprValidationException
    {
        // No aggregates used, we do not need this service
        if ((selectAggregateExprNodes.isEmpty()) && (havingAggregateExprNodes.isEmpty()))
        {
        	return new AggregationServiceNull();
        }

        // Validate the absence of "prev" function in where-clause:
        // Since the "previous" function does not post remove stream results, disallow when used with aggregations.
        if ((whereClause != null) || (havingClause != null)) {
            ExprNodePreviousVisitorWParent visitor = new ExprNodePreviousVisitorWParent();
            if (whereClause != null) {
                whereClause.accept(visitor);
            }
            if (havingClause != null) {
                havingClause.accept(visitor);
            }
            if ((visitor.getPrevious() != null) && (!visitor.getPrevious().isEmpty())) {
                String funcname = visitor.getPrevious().get(0).getSecond().getPreviousType().toString().toLowerCase();
                throw new ExprValidationException("The '" + funcname + "' function may not occur in the where-clause or having-clause of a statement with aggregations as 'previous' does not provide remove stream data; Use the 'first','last','window' or 'count' aggregation functions instead");
            }
        }

        // Compile a map of aggregation nodes and equivalent-to aggregation nodes.
        // Equivalent-to functions are for example "select sum(a*b), 5*sum(a*b)".
        // Reducing the total number of aggregation functions.
        List<ExprAggDesc> aggregations = new ArrayList<ExprAggDesc>();
        for (ExprAggregateNode selectAggNode : selectAggregateExprNodes)
        {
            addEquivalent(selectAggNode, aggregations);
        }
        for (ExprAggregateNode havingAggNode : havingAggregateExprNodes)
        {
            addEquivalent(havingAggNode, aggregations);
        }
        for (ExprAggregateNode orderByAggNode : orderByAggregateExprNodes)
        {
            addEquivalent(orderByAggNode, aggregations);
        }

        // Assign a column number to each aggregation node. The regular aggregation goes first followed by access-aggregation.
        int columnNumber = 0;
        for (ExprAggDesc entry : aggregations)
        {
            if (entry.getFactory().getSpec(false) == null) {
                entry.setColumnNum(columnNumber++);
            }
        }
        for (ExprAggDesc entry : aggregations)
        {
            if (entry.getFactory().getSpec(false) != null) {
                entry.setColumnNum(columnNumber++);
            }
        }

        // handle regular aggregation (function provides value(s) to aggregate)
        List<AggregationMethod> aggregators = new ArrayList<AggregationMethod>();
        List<ExprEvaluator> evaluators = new ArrayList<ExprEvaluator>();

        // handle accessor aggregation (direct data window by-group access to properties)
        Map<Integer, Integer> streamSlots = new TreeMap<Integer, Integer>();
        List<AggregationAccessorSlotPair> accessorPairs = new ArrayList<AggregationAccessorSlotPair>();

        // Construct a list of evaluation node for the aggregation functions (regular agg).
        // For example "sum(2 * 3)" would make the sum an evaluation node.
        // Also determine all the streams that need direct access and compute a index (slot) for each (access agg).
        int currentSlot = 0;
        for (ExprAggDesc aggregation : aggregations)
        {
            ExprAggregateNode aggregateNode = aggregation.getAggregationNode();
            if (aggregateNode.getFactory().getSpec(false) == null) {
                ExprEvaluator evaluator;
                if (aggregateNode.getChildNodes().size() > 1)
                {
                    evaluator = getMultiNodeEvaluator(aggregateNode.getChildNodes(), exprEvaluatorContext);
                }
                else if (!aggregateNode.getChildNodes().isEmpty())
                {
                    // Use the evaluation node under the aggregation node to obtain the aggregation value
                    evaluator = aggregateNode.getChildNodes().get(0).getExprEvaluator();
                }
                // For aggregation that doesn't evaluate any particular sub-expression, return null on evaluation
                else
                {
                    evaluator = new ExprEvaluator() {
                        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                        {
                            return null;
                        }
                        public Class getType()
                        {
                            return null;
                        }
                        public Map<String, Object> getEventType() {
                            return null;
                        }
                    };
                }
                AggregationMethod aggregator = aggregateNode.getFactory().getPrototypeAggregator(methodResolutionService);

                evaluators.add(evaluator);
                aggregators.add(aggregator);
            }
            else {
                AggregationSpec spec = aggregateNode.getFactory().getSpec(false);
                AggregationAccessor accessor = aggregateNode.getFactory().getAccessor();

                Integer slot = streamSlots.get(spec.getStreamNum());
                if (slot == null) {
                    streamSlots.put(spec.getStreamNum(), currentSlot);
                    slot = currentSlot++;
                }

                accessorPairs.add(new AggregationAccessorSlotPair(slot, accessor));
            }
        }

        // handle no group-by clause cases
        ExprEvaluator[] evaluatorsArr = evaluators.toArray(new ExprEvaluator[evaluators.size()]);
        AggregationMethod[] aggregatorsArr = aggregators.toArray(new AggregationMethod[aggregators.size()]);
        AggregationAccessorSlotPair[] pairs = accessorPairs.toArray(new AggregationAccessorSlotPair[accessorPairs.size()]);
        int[] accessedStreams = CollectionUtil.intArray(streamSlots.keySet());

        AggregationService service;

        // Handle without a group-by clause: we group all into the same pot
        if (!hasGroupByClause) {
            if ((evaluatorsArr.length > 0) && (accessorPairs.isEmpty())) {
                service = new AggSvcGroupAllNoAccessImpl(evaluatorsArr, aggregatorsArr);
            }
            else if ((evaluatorsArr.length == 0) && (!accessorPairs.isEmpty())) {
                service = new AggSvcGroupAllAccessOnlyImpl(methodResolutionService, pairs, accessedStreams, isJoin);
            }
            else {
                service = new AggSvcGroupAllMixedAccessImpl(evaluatorsArr, aggregatorsArr, methodResolutionService, pairs, accessedStreams, isJoin);
            }
        }
        else {
            boolean hasNoReclaim = HintEnum.DISABLE_RECLAIM_GROUP.getHint(annotations) != null;
            Hint reclaimGroupAged = HintEnum.RECLAIM_GROUP_AGED.getHint(annotations);
            Hint reclaimGroupFrequency = HintEnum.RECLAIM_GROUP_AGED.getHint(annotations);
            if (hasNoReclaim)
            {
                if ((evaluatorsArr.length > 0) && (accessorPairs.isEmpty())) {
                    service = new AggSvcGroupByNoAccessImpl(evaluatorsArr, aggregatorsArr, methodResolutionService);
                }
                else if ((evaluatorsArr.length == 0) && (!accessorPairs.isEmpty())) {
                    service = new AggSvcGroupByAccessOnlyImpl(methodResolutionService, pairs, accessedStreams, isJoin);
                }
                else {
                    service = new AggSvcGroupByMixedAccessImpl(evaluatorsArr, aggregatorsArr, methodResolutionService, pairs, accessedStreams, isJoin);
                }
            }
            else if (reclaimGroupAged != null)
            {
                service = new AggSvcGroupByReclaimAged(evaluatorsArr, aggregatorsArr, methodResolutionService, reclaimGroupAged, reclaimGroupFrequency, variableService, statementStopService, pairs, accessedStreams, isJoin);
            }
            else
            {
                if ((evaluatorsArr.length > 0) && (accessorPairs.isEmpty())) {
                    service = new AggSvcGroupByRefcountedNoAccessImpl(evaluatorsArr, aggregatorsArr, methodResolutionService);
                }
                else {
                    service = new AggSvcGroupByRefcountedWAccessImpl(evaluatorsArr, aggregatorsArr, methodResolutionService, pairs, accessedStreams, isJoin);
                }
            }
        }

        // Hand a service reference to the aggregation nodes themselves.
        // Thus on expression evaluation time each aggregate node calls back to find out what the
        // group's state is (and thus does not evaluate by asking its child node for its result).
        for (ExprAggDesc aggregation : aggregations)
        {
            aggregation.assignFuture(service);
        }

        return service;
    }

    private static ExprEvaluator getMultiNodeEvaluator(List<ExprNode> childNodes, ExprEvaluatorContext exprEvaluatorContext)
    {
        final int size = childNodes.size();
        final List<ExprNode> exprNodes = childNodes;
        final Object[] prototype = new Object[size];

        // determine constant nodes
        int count = 0;
        for (ExprNode node : exprNodes)
        {
            if (node.isConstantResult())
            {
                prototype[count] = node.getExprEvaluator().evaluate(null, true, exprEvaluatorContext);
            }
            count++;
        }

        return new ExprEvaluator() {
            public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
            {
                int count = 0;
                for (ExprNode node : exprNodes)
                {
                    prototype[count] = node.getExprEvaluator().evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                    count++;
                }
                return prototype;
            }

            public Class getType()
            {
                return Object[].class;
            }
            public Map<String, Object> getEventType() {
                return null;
            }                                                
        };
    }

    private static void addEquivalent(ExprAggregateNode aggNodeToAdd, List<ExprAggDesc> equivalencyList)
    {
        // Check any same aggregation nodes among all aggregation clauses
        boolean foundEquivalent = false;
        for (ExprAggDesc existing : equivalencyList)
        {
            ExprAggregateNode aggNode = existing.getAggregationNode();
            if (ExprNodeUtility.deepEquals(aggNode, aggNodeToAdd))
            {
                existing.addEquivalent(aggNodeToAdd);
                foundEquivalent = true;
                break;
            }
        }

        if (!foundEquivalent)
        {
            equivalencyList.add(new ExprAggDesc(aggNodeToAdd, aggNodeToAdd.getFactory()));
        }
    }

    /**
     * Descriptor assigning to an aggregation expression the factory to use and other equivalent aggregation nodes.
     */
    public static class ExprAggDesc {
        private ExprAggregateNode aggregationNode;
        private AggregationMethodFactory factory;

        private List<ExprAggregateNode> equivalentNodes;
        private Integer columnNum;

        /**
         * Ctor.
         * @param aggregationNode expression
         * @param factory method factory
         */
        public ExprAggDesc(ExprAggregateNode aggregationNode, AggregationMethodFactory factory)
        {
            this.aggregationNode = aggregationNode;
            this.factory = factory;
        }

        /**
         * Returns the equivalent aggregation functions.
         * @return list of agg nodes
         */
        public List<ExprAggregateNode> getEquivalentNodes()
        {
            return equivalentNodes;
        }

        /**
         * Returns the method factory.
         * @return factory
         */
        public AggregationMethodFactory getFactory()
        {
            return factory;
        }

        /**
         * Returns the column number assigned.
         * @return column number
         */
        public Integer getColumnNum()
        {
            return columnNum;
        }

        /**
         * Assigns a column number.
         * @param columnNum column number
         */
        public void setColumnNum(Integer columnNum)
        {
            this.columnNum = columnNum;
        }

        /**
         * Add an equivalent aggregation function node
         * @param aggNodeToAdd node to add
         */
        public void addEquivalent(ExprAggregateNode aggNodeToAdd)
        {
            if (equivalentNodes == null) {
                equivalentNodes = new ArrayList<ExprAggregateNode>();
            }
            equivalentNodes.add(aggNodeToAdd);
        }

        /**
         * Returns the expression.
         * @return expression
         */
        public ExprAggregateNode getAggregationNode()
        {
            return aggregationNode;
        }

        /**
         * Assigns a future to the expression
         * @param service the future
         */
        public void assignFuture(AggregationResultFuture service)
        {
            aggregationNode.setAggregationResultFuture(service, columnNum);
            if (equivalentNodes == null) {
                return;
            }
            for (ExprAggregateNode equivalentAggNode : equivalentNodes)
            {
                equivalentAggNode.setAggregationResultFuture(service, columnNum);
            }
        }
    }
}

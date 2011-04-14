/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.expression.ExprAggregateNode;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.epl.spec.OrderByItem;
import com.espertech.esper.epl.spec.RowLimitSpec;
import com.espertech.esper.epl.spec.SelectClauseExprCompiledSpec;
import com.espertech.esper.epl.variable.VariableService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Factory for {@link com.espertech.esper.epl.core.OrderByProcessor} processors.
 */
public class OrderByProcessorFactory {
	private static final Log log = LogFactory.getLog(OrderByProcessorFactory.class);

    /**
     * Returns processor for order-by clauses.
     * @param selectionList is a list of select expressions
     * @param groupByNodes is a list of group-by expressions
     * @param orderByList is a list of order-by expressions
     * @param aggregationService is the service for aggregation, ie. building sums and averages per group or overall
     * @param rowLimitSpec specification for row limit, or null if no row limit is defined
     * @param variableService for retrieving variable state for use with row limiting
     * @param isSortUsingCollator for string value sorting using compare or Collator 
     * @return ordering processor instance
     * @throws com.espertech.esper.epl.expression.ExprValidationException when validation of expressions fails
     */
    public static OrderByProcessor getProcessor(List<SelectClauseExprCompiledSpec> selectionList,
											   List<ExprNode> groupByNodes,
											   List<OrderByItem> orderByList,
											   AggregationService aggregationService,
                                               RowLimitSpec rowLimitSpec,
                                               VariableService variableService,
                                               boolean isSortUsingCollator)
	throws ExprValidationException
	{
		// Get the order by expression nodes
		List<ExprNode> orderByNodes = new ArrayList<ExprNode>();
		for(OrderByItem element : orderByList)
		{
			orderByNodes.add(element.getExprNode());
		}

		// No order-by clause
		if(orderByList.isEmpty())
		{
			log.debug(".getProcessor Using no OrderByProcessor");
            if (rowLimitSpec != null)
            {
                return new OrderByProcessorRowLimit(rowLimitSpec, variableService);
            }
            return null;
        }
		
        // Determine aggregate functions used in select, if any
        List<ExprAggregateNode> selectAggNodes = new LinkedList<ExprAggregateNode>();
        for (SelectClauseExprCompiledSpec element : selectionList)
        {
            ExprAggregateNode.getAggregatesBottomUp(element.getSelectExpression(), selectAggNodes);
        }
        
		// Get all the aggregate functions occuring in the order-by clause
        List<ExprAggregateNode> orderAggNodes = new LinkedList<ExprAggregateNode>();
        for (ExprNode orderByNode : orderByNodes)
        {
            ExprAggregateNode.getAggregatesBottomUp(orderByNode, orderAggNodes);
        }
		
		validateOrderByAggregates(selectAggNodes, orderAggNodes);
		
        // Tell the order-by processor whether to compute group-by
        // keys if they are not present
    	boolean needsGroupByKeys = !selectionList.isEmpty() && !orderAggNodes.isEmpty();

        log.debug(".getProcessor Using OrderByProcessorImpl");
        OrderByProcessorImpl orderByProcessor = new OrderByProcessorImpl(orderByList, groupByNodes, needsGroupByKeys, aggregationService, isSortUsingCollator);
        if (rowLimitSpec == null)
        {
            return orderByProcessor;
        }
        else
        {
            return new OrderByProcessorOrderedLimit(orderByProcessor, new OrderByProcessorRowLimit(rowLimitSpec, variableService));
        }
	}
	

	
	private static void validateOrderByAggregates(List<ExprAggregateNode> selectAggNodes,
									   List<ExprAggregateNode> orderAggNodes)
	throws ExprValidationException
	{
		// Check that the order-by clause doesn't contain 
		// any aggregate functions not in the select expression
		for(ExprAggregateNode orderAgg : orderAggNodes)
		{
			boolean inSelect = false;
			for(ExprAggregateNode selectAgg : selectAggNodes)
			{
				if(ExprNodeUtility.deepEquals(selectAgg, orderAgg))
				{
					inSelect = true;
					break;
				}
			}
			if(!inSelect)
			{
				throw new ExprValidationException("Aggregate functions in the order-by clause must also occur in the select expression");
			}
		}
	}
}

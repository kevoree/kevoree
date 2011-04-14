/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.spec.RowLimitSpec;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.epl.variable.VariableReader;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An order-by processor that sorts events according to the expressions
 * in the order_by clause.
 */
public class OrderByProcessorRowLimit implements OrderByProcessor {

	private static final Log log = LogFactory.getLog(OrderByProcessorImpl.class);

    private final VariableReader numRowsVariableReader;
    private final VariableReader offsetVariableReader;
    private int currentRowLimit;
    private int currentOffset;

    /**
     * Ctor.
     * @param rowLimitSpec specification for row limit, or null if no row limit is defined
     * @param variableService for retrieving variable state for use with row limiting
     * @throws ExprValidationException if row limit specification validation fails
     */
    public OrderByProcessorRowLimit(RowLimitSpec rowLimitSpec, VariableService variableService)
            throws ExprValidationException
    {
        if (rowLimitSpec.getNumRowsVariable() != null)
        {
            numRowsVariableReader = variableService.getReader(rowLimitSpec.getNumRowsVariable());
            if (numRowsVariableReader == null)
            {
                throw new ExprValidationException("Limit clause variable by name '" + rowLimitSpec.getNumRowsVariable() + "' has not been declared");
            }
            if (!JavaClassHelper.isNumeric(numRowsVariableReader.getType()))
            {
                throw new ExprValidationException("Limit clause requires a variable of numeric type");
            }
        }
        else
        {
            numRowsVariableReader = null;
            currentRowLimit = rowLimitSpec.getNumRows();

            if (currentRowLimit < 0)
            {
                currentRowLimit = Integer.MAX_VALUE;
            }
        }

        if (rowLimitSpec.getOptionalOffsetVariable() != null)
        {
            offsetVariableReader = variableService.getReader(rowLimitSpec.getOptionalOffsetVariable());
            if (offsetVariableReader == null)
            {
                throw new ExprValidationException("Limit clause variable by name '" + rowLimitSpec.getOptionalOffsetVariable() + "' has not been declared");
            }
            if (!JavaClassHelper.isNumeric(offsetVariableReader.getType()))
            {
                throw new ExprValidationException("Limit clause requires a variable of numeric type");
            }
        }
        else
        {
            offsetVariableReader = null;
            if (rowLimitSpec.getOptionalOffset() != null)
            {
                currentOffset = rowLimitSpec.getOptionalOffset();

                if (currentOffset <= 0)
                {
                    throw new ExprValidationException("Limit clause requires a positive offset");
                }
            }
            else
            {
                currentOffset = 0;
            }
        }
    }

    public EventBean[] sort(EventBean[] outgoingEvents, EventBean[][] generatingEvents, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return applyLimit(outgoingEvents);
    }

    public EventBean[] sort(EventBean[] outgoingEvents, EventBean[][] generatingEvents, MultiKeyUntyped[] groupByKeys, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return applyLimit(outgoingEvents);
    }

    public MultiKeyUntyped getSortKey(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return null;
    }

    public MultiKeyUntyped[] getSortKeyPerRow(EventBean[] generatingEvents, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return null;
    }

    public EventBean[] sort(EventBean[] outgoingEvents, MultiKeyUntyped[] orderKeys, ExprEvaluatorContext exprEvaluatorContext)
    {
        return applyLimit(outgoingEvents);
    }

    /**
     * Applys the limiting function to outgoing events.
     * @param outgoingEvents unlimited
     * @return limited
     */
    protected EventBean[] applyLimit(EventBean[] outgoingEvents)
    {
        if (outgoingEvents == null) {
            return null;
        }
        if (numRowsVariableReader != null)
        {
            Number varValue = (Number) numRowsVariableReader.getValue();
            if (varValue != null)
            {
                currentRowLimit = varValue.intValue();
            }
            else
            {
                currentRowLimit = Integer.MAX_VALUE;
            }
            if (currentRowLimit < 0)
            {
                currentRowLimit = Integer.MAX_VALUE;
            }
        }

        if (offsetVariableReader != null)
        {
            Number varValue = (Number) offsetVariableReader.getValue();
            if (varValue != null)
            {
                currentOffset = varValue.intValue();
            }
            else
            {
                currentOffset = 0;
            }
            if (currentOffset < 0)
            {
                currentOffset = 0;
            }
        }

        // no offset
        if (currentOffset == 0)
        {
            if ((outgoingEvents.length <= currentRowLimit))
            {
                return outgoingEvents;
            }

            if (currentRowLimit == 0)
            {
                return null;
            }

            EventBean[] limited = new EventBean[currentRowLimit];
            System.arraycopy(outgoingEvents, 0, limited, 0, currentRowLimit);
            return limited;
        }
        // with offset
        else
        {
            int maxInterested = currentRowLimit + currentOffset;
            if (currentRowLimit == Integer.MAX_VALUE)
            {
                maxInterested = Integer.MAX_VALUE;
            }

            // more rows then requested
            if (outgoingEvents.length > maxInterested)
            {
                EventBean[] limited = new EventBean[currentRowLimit];
                System.arraycopy(outgoingEvents, currentOffset, limited, 0, currentRowLimit);
                return limited;
            }

            // less or equal rows to offset
            if (outgoingEvents.length <= currentOffset)
            {
                return null;
            }

            int size = outgoingEvents.length - currentOffset;
            EventBean[] limited = new EventBean[size];
            System.arraycopy(outgoingEvents, currentOffset, limited, 0, size);
            return limited;
        }
    }
}

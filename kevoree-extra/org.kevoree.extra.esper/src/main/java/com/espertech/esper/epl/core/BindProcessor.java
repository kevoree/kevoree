/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.spec.SelectClauseElementCompiled;
import com.espertech.esper.epl.spec.SelectClauseElementWildcard;
import com.espertech.esper.epl.spec.SelectClauseExprCompiledSpec;
import com.espertech.esper.epl.spec.SelectClauseStreamCompiledSpec;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Works in conjunction with {@link SelectExprResultProcessor} to present
 * a result as an object array for 'natural' delivery.
 */
public class BindProcessor
{
    private ExprEvaluator[] expressionNodes;
    private Class[] expressionTypes;
    private String[] columnNamesAssigned;

    /**
     * Ctor.
     * @param selectionList the select clause
     * @param typesPerStream the event types per stream
     * @param streamNames the stream names
     * @throws ExprValidationException when the validation of the select clause failed
     */
    public BindProcessor(List<SelectClauseElementCompiled> selectionList,
                         EventType[] typesPerStream,
                         String[] streamNames)
            throws ExprValidationException
    {
        ArrayList<ExprEvaluator> expressions = new ArrayList<ExprEvaluator>();
        ArrayList<Class> types = new ArrayList<Class>();
        ArrayList<String> columnNames = new ArrayList<String>();

        for (SelectClauseElementCompiled element : selectionList)
        {
            // handle wildcards by outputting each stream's underlying event
            if (element instanceof SelectClauseElementWildcard)
            {
                for (int i = 0; i < typesPerStream.length; i++)
                {
                    final int streamNum = i;
                    final Class returnType = typesPerStream[streamNum].getUnderlyingType();
                    
                    expressions.add(new ExprEvaluator() {

                        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                        {
                            EventBean event = eventsPerStream[streamNum];
                            if (event != null)
                            {
                                return event.getUnderlying();
                            }
                            else
                            {
                                return null;
                            }
                        }

                        public Class getType()
                        {
                            return returnType;
                        }

                        public Map<String, Object> getEventType() {
                            return null;
                        }
                    });
                    types.add(returnType);
                    columnNames.add(streamNames[streamNum]);
                }
            }

            // handle stream wildcards by outputting the stream underlying event
            else if (element instanceof SelectClauseStreamCompiledSpec)
            {
                final SelectClauseStreamCompiledSpec streamSpec = (SelectClauseStreamCompiledSpec) element;
                final Class returnType = typesPerStream[streamSpec.getStreamNumber()].getUnderlyingType();

                expressions.add(new ExprEvaluator() {

                    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                    {
                        EventBean event = eventsPerStream[streamSpec.getStreamNumber()];
                        if (event != null)
                        {
                            return event.getUnderlying();
                        }
                        else
                        {
                            return null;
                        }
                    }

                    public Class getType()
                    {
                        return returnType;
                    }

                    public Map<String, Object> getEventType() {
                        return null;
                    }
                });
                types.add(returnType);
                columnNames.add(streamNames[streamSpec.getStreamNumber()]);
            }

            // handle expressions
            else if (element instanceof SelectClauseExprCompiledSpec)
            {
                SelectClauseExprCompiledSpec expr = (SelectClauseExprCompiledSpec) element;
                ExprEvaluator evaluator = expr.getSelectExpression().getExprEvaluator();
                expressions.add(evaluator);
                types.add(evaluator.getType());
                if (expr.getAssignedName() != null)
                {
                    columnNames.add(expr.getAssignedName());
                }
                else
                {
                    columnNames.add(expr.getSelectExpression().toExpressionString());
                }
            }
            else
            {
                throw new IllegalStateException("Unrecognized select expression element of type " + element.getClass());
            }
        }

        expressionNodes = expressions.toArray(new ExprEvaluator[expressions.size()]);
        expressionTypes = types.toArray(new Class[types.size()]);
        columnNamesAssigned = columnNames.toArray(new String[columnNames.size()]);
    }

    /**
     * Process select expressions into columns for native dispatch.
     * @param eventsPerStream each stream's events
     * @param isNewData true for new events
     * @param exprEvaluatorContext context for expression evaluatiom
     * @return object array with select-clause results
     */
    public Object[] process(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object[] parameters = new Object[expressionNodes.length];

        for (int i = 0; i < parameters.length; i++)
        {
            Object result = expressionNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            parameters[i] = result;
        }

        return parameters;
    }

    /**
     * Returns the expression types generated by the select-clause expressions.
     * @return types
     */
    public Class[] getExpressionTypes() {
        return expressionTypes;
    }

    /**
     * Returns the column names of select-clause expressions.
     * @return column names
     */
    public String[] getColumnNamesAssigned() {
        return columnNamesAssigned;
    }
}

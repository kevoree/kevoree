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
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements an aggregation service for match recognize.
 */
public class AggregationServiceMatchRecognizeImpl implements AggregationServiceMatchRecognize
{
    private ExprEvaluator evaluatorsEachStream[][];
    private AggregationMethod aggregatorsEachStream[][];
    private AggregationMethod aggregatorsAll[];

    /**
     * Ctor.
     * @param countStreams number of streams/variables
     * @param aggregatorsPerStream aggregation methods per stream
     * @param evaluatorsPerStream evaluation functions per stream
     */
    public AggregationServiceMatchRecognizeImpl(int countStreams, LinkedHashMap<Integer, AggregationMethod[]> aggregatorsPerStream, Map<Integer, ExprEvaluator[]> evaluatorsPerStream) {
        evaluatorsEachStream = new ExprEvaluator[countStreams][];
        aggregatorsEachStream = new AggregationMethod[countStreams][];

        int count = 0;
        for (Map.Entry<Integer, AggregationMethod[]> agg : aggregatorsPerStream.entrySet())
        {
            aggregatorsEachStream[agg.getKey()] = agg.getValue();
            count += agg.getValue().length;
        }

        aggregatorsAll = new AggregationMethod[count];
        count = 0;
        for (Map.Entry<Integer, AggregationMethod[]> agg : aggregatorsPerStream.entrySet())
        {
            for (AggregationMethod method : agg.getValue())
            {
                aggregatorsAll[count++] = method;
            }
        }

        for (Map.Entry<Integer, ExprEvaluator[]> eval : evaluatorsPerStream.entrySet())
        {
            evaluatorsEachStream[eval.getKey()] = eval.getValue();
        }
    }

    public void applyEnter(EventBean[] eventsPerStream, int streamId, ExprEvaluatorContext exprEvaluatorContext) {

        ExprEvaluator[] evaluatorsStream = evaluatorsEachStream[streamId];
        if (evaluatorsStream == null)
        {
            return;
        }

        AggregationMethod[] aggregatorsStream = aggregatorsEachStream[streamId];
        for (int j = 0; j < evaluatorsStream.length; j++)
        {
            Object columnResult = evaluatorsStream[j].evaluate(eventsPerStream, true, exprEvaluatorContext);
            aggregatorsStream[j].enter(columnResult);
        }
    }

    public Object getValue(int column)
    {
        return aggregatorsAll[column].getValue();
    }

    public void clearResults()
    {
        for (AggregationMethod aggregator : aggregatorsAll)
        {
            aggregator.clear();
        }
    }
}
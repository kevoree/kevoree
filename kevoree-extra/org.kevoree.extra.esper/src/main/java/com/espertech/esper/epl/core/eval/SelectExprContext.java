package com.espertech.esper.epl.core.eval;

import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.event.EventAdapterService;

public class SelectExprContext {
    private final ExprEvaluator[] expressionNodes;
    private final String[] columnNames;
    private final ExprEvaluatorContext exprEvaluatorContext;
    private final EventAdapterService eventAdapterService;

    public SelectExprContext(ExprEvaluator[] expressionNodes, String[] columnNames, ExprEvaluatorContext exprEvaluatorContext, EventAdapterService eventAdapterService) {
        this.expressionNodes = expressionNodes;
        this.columnNames = columnNames;
        this.exprEvaluatorContext = exprEvaluatorContext;
        this.eventAdapterService = eventAdapterService;
    }

    public ExprEvaluator[] getExpressionNodes() {
        return expressionNodes;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public ExprEvaluatorContext getExprEvaluatorContext() {
        return exprEvaluatorContext;
    }

    public EventAdapterService getEventAdapterService() {
        return eventAdapterService;
    }
}

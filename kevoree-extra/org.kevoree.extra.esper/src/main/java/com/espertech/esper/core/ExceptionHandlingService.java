package com.espertech.esper.core;

import com.espertech.esper.client.hook.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class ExceptionHandlingService {

    private static final Log log = LogFactory.getLog(ExceptionHandlingService.class);

    private final String engineURI;
    private final List<ExceptionHandler> exceptionHandlers;
    private final List<ConditionHandler> conditionHandlers;

    public ExceptionHandlingService(String engineURI, List<ExceptionHandler> exceptionHandlers, List<ConditionHandler> conditionHandlers) {
        this.engineURI = engineURI;
        this.exceptionHandlers = exceptionHandlers;
        this.conditionHandlers = conditionHandlers;
    }

    public void handleCondition(BaseCondition condition, EPStatementHandle handle) {
        if (conditionHandlers.isEmpty()) {
            log.info("Condition encountered processing statement '" + handle.getStatementName() + "' statement text '" + handle.getEPL() + "' : " + condition.toString());
            return;
        }

        ConditionHandlerContext context = new ConditionHandlerContext(engineURI, handle.getStatementName(), handle.getEPL(), condition);
        for (ConditionHandler handler : conditionHandlers) {
            handler.handle(context);
        }
    }

    public void handleException(RuntimeException ex, EPStatementHandle handle) {
        if (exceptionHandlers.isEmpty()) {
            log.error("Exception encountered processing statement '" + handle.getStatementName() + "' statement text '" + handle.getEPL() + "' : " + ex.getMessage(), ex);
            return;
        }

        ExceptionHandlerContext context = new ExceptionHandlerContext(engineURI, ex, handle.getStatementName(), handle.getEPL());
        for (ExceptionHandler handler : exceptionHandlers) {
            handler.handle(context);
        }
    }
}

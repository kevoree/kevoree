package com.espertech.esper.client.hook;

/**
 * Interface for a handler registered with an engine instance to receive reported engine conditions.
 */
public interface ConditionHandler {
    /**
     * Handle the engine condition as contained in the context object passed.
     * @param context the condition information
     */
    public void handle(ConditionHandlerContext context);
}

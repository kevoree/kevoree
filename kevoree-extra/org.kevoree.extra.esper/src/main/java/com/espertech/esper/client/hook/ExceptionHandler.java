package com.espertech.esper.client.hook;

/**
 * Interface for an exception handler.
 * <p>
 * When the engine encounters an unchecked exception processing a continous-query statement it allows
 * any exception handler that is registered with the engine to handle the exception, in the order
 * any handlers are registered.
 * <p>
 * On-demand queries as well as any exceptions thrown by static method invocations or event method invocations
 * or the API other then the sendEvent method are not provided to an exception handler.
 * <p>
 * An application may throw a runtime exception in the @handle method to cancel further processing
 * of an event against statements.
 */
public interface ExceptionHandler {
    
    /**
     * Handle the exception as contained in the context object passed.
     * @param context the exception information
     */
    public void handle(ExceptionHandlerContext context);
}

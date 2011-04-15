package com.espertech.esper.client.hook;

/**
 * Factory for exception handler instance(s).
 * <p>
 * Receives CEP engine contextual information and should return an implementation of the
 * {@link ExceptionHandler} interface.
 */
public interface ExceptionHandlerFactory {

    /**
     * Returns an exception handler instances, or null if the factory decided not to contribute an exception handler.
     * @param context contains the engine URI
     * @return exception handler
     */
    public ExceptionHandler getHandler(ExceptionHandlerFactoryContext context);
}

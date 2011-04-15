package com.espertech.esper.client.hook;

/**
 * Factory for engine condition handler instance(s).
 * <p>
 * Receives CEP engine contextual information and should return an implementation of the
 * {@link ConditionHandler} interface.
 */
public interface ConditionHandlerFactory {

    /**
     * Returns an exception handler instances, or null if the factory decided not to contribute an exception handler.
     * @param context contains the engine URI
     * @return exception handler
     */
    public ConditionHandler getHandler(ConditionHandlerFactoryContext context);
}

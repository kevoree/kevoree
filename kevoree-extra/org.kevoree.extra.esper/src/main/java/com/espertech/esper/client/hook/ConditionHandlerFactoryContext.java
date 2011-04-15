package com.espertech.esper.client.hook;

/**
 * Context provided to {@link ConditionHandlerFactory} implementations providing
 * engine contextual information.
 */
public class ConditionHandlerFactoryContext {
    private final String engineURI;

    /**
     * Ctor.
     * @param engineURI engine URI
     */
    public ConditionHandlerFactoryContext(String engineURI) {
        this.engineURI = engineURI;
    }

    /**
     * Returns the engine URI.
     * @return engine URI
     */
    public String getEngineURI() {
        return engineURI;
    }
}

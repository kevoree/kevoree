package com.espertech.esper.client.hook;

/**
 * Context provided to {@link ExceptionHandlerFactory} implementations providing
 * engine contextual information.
 */
public class ExceptionHandlerFactoryContext {
    private final String engineURI;

    /**
     * Ctor.
     * @param engineURI engine URI
     */
    public ExceptionHandlerFactoryContext(String engineURI) {
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

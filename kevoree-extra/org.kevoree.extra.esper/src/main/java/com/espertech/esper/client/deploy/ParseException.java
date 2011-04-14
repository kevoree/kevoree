package com.espertech.esper.client.deploy;

/**
 * Exception thrown when an EPL text could not be parsed.
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 566081132579187386L;
    
    /**
     * Ctor.
     * @param message error message
     */
    public ParseException(String message) {
        super(message);
    }
}

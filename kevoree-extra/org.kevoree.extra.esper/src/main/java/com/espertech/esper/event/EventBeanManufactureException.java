package com.espertech.esper.event;

/**
 * Thrown to indicate a problem creating or populating an underlying event objects. 
 */
public class EventBeanManufactureException extends Exception
{
    private static final long serialVersionUID = -7713342108994541449L;

    /**
     * Ctor.
     * @param message message
     * @param cause cause
     */
    public EventBeanManufactureException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

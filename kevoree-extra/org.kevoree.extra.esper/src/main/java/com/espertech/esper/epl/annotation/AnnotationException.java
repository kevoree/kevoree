package com.espertech.esper.epl.annotation;

/**
 * Thrown to indicate a problem processing an EPL statement annotation.
 */
public class AnnotationException extends Exception
{
    private static final long serialVersionUID = 167248816444780182L;
    
    /**
     * Ctor.
     * @param message error message
     */
    public AnnotationException(String message)
    {
        super(message);
    }
}

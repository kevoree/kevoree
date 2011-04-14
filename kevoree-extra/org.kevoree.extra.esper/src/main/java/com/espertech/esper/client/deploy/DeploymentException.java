package com.espertech.esper.client.deploy;

import java.util.List;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Base deployment exception.
 */
public class DeploymentException extends Exception {

    private static final long serialVersionUID = 6859620436230176552L;

    /**
     * Ctor.
     * @param message error message
     */
    public DeploymentException(String message)
    {
        super(message);
    }

    /**
     * Ctor.
     * @param message error message
     * @param cause cause
     */
    public DeploymentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Ctor.
     * @param cause cause
     */
    public DeploymentException(Throwable cause)
    {
        super(cause);
    }
}
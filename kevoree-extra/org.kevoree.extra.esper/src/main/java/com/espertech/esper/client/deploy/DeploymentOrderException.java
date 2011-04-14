package com.espertech.esper.client.deploy;

/**
 * Exception indicates a problem when determining delpoyment order and uses-dependency checking.
 */
public class DeploymentOrderException extends DeploymentException {

    private static final long serialVersionUID = -6298842035260203021L;

    /**
     * Ctor.
     * @param message error message
     */
    public DeploymentOrderException(String message)
    {
        super(message);
    }
}
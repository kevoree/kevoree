package com.espertech.esper.client.deploy;

/**
 * Inner exception to {@link DeploymentActionException} available on statement level.
 */
public class DeploymentStateException extends DeploymentException {

    private static final long serialVersionUID = 8451246235746829231L;

    /**
     * Ctor.
     * @param message error message
     */
    public DeploymentStateException(String message)
    {
        super(message);
    }
}

package com.espertech.esper.client.deploy;

/**
 * Inner exception to {@link com.espertech.esper.client.deploy.DeploymentActionException} available on statement level.
 */
public class DeploymentNotFoundException extends DeploymentException {private static final long serialVersionUID = -1243745018013856125L;

    /**
     * Ctor.
     * @param message error message
     */
    public DeploymentNotFoundException(String message)
    {
        super(message);
    }
}

package com.espertech.esper.client.deploy;

/**
 * Deployment state.
 */
public enum DeploymentState
{
    /**
     * In undeployed state a deployment is added but not currently deployed.
     */
    UNDEPLOYED,

    /**
     * In deployed state a deployment is added and it is deployed, i.e. has zero to many active EPL statements
     * associated.
     */
    DEPLOYED
}

package com.espertech.esper.client.deploy;

import java.util.List;

/**
 * Returned by the {@link EPDeploymentAdmin#getDeploymentOrder} operation to holds an ordered list of modules considering each module's uses-dependencies
 * on other modules. 
 */
public class DeploymentOrder
{
    private List<Module> ordered;

    /**
     * Cotr.
     * @param ordered list of modules
     */
    public DeploymentOrder(List<Module> ordered)
    {
        this.ordered = ordered;
    }

    /**
     * Returns the list of modules.
     * @return modules
     */
    public List<Module> getOrdered()
    {
        return ordered;
    }
}

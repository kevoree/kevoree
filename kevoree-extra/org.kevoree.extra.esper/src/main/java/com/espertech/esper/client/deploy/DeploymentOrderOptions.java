package com.espertech.esper.client.deploy;

/**
 * Options class passed to {@link EPDeploymentAdmin#getDeploymentOrder} for controlling the behavior of ordering and dependency checking logic.
 */
public class DeploymentOrderOptions
{
    private boolean checkCircularDependency = true;
    private boolean checkUses = true;

    /**
     * Returns true (the default) to indicate that the algorithm checks for circular dependencies among the uses-dependency graph,
     * or false to not perform this check.
     * @return indicator.
     */
    public boolean isCheckCircularDependency()
    {
        return checkCircularDependency;
    }

    /**
     * Set this indicator to true (the default) to indicate that the algorithm checks for circular dependencies among the uses-dependency graph,
     * or false to not perform this check.
     * @param checkCircularDependency indicator.
     */
    public void setCheckCircularDependency(boolean checkCircularDependency)
    {
        this.checkCircularDependency = checkCircularDependency;
    }

    /**
     * Returns true (the default) to cause the algorithm to check uses-dependencies ensuring all dependencies are satisfied i.e.
     * all dependent modules are either deployed or are part of the modules passed in, or false to not perform the checking.
     * @return indicator
     */
    public boolean isCheckUses()
    {
        return checkUses;
    }

    /**
     * Set this indicator to true (the default) to cause the algorithm to check uses-dependencies ensuring all dependencies are satisfied i.e.
     * all dependent modules are either deployed or are part of the modules passed in, or false to not perform the checking.
     * @param checkUses indicator
     */
    public void setCheckUses(boolean checkUses)
    {
        this.checkUses = checkUses;
    }
}
package com.espertech.esper.client;

/**
 * Interface for receiving callback events pertaining to statement creation and statement state transitions.
 * <p>
 * Implementations must not block the operation.
 */
public interface EPStatementStateListener
{
    /**
     * Called to indicate that a new statement has been created in stopped state.
     * <p>
     * The #onStatementStateChange method is also invoked upon statement start. 
     * @param serviceProvider the service provider instance under which the statement has been created
     * @param statement the new statement
     */
    public void onStatementCreate(EPServiceProvider serviceProvider, EPStatement statement);

    /**
     * Called to indicate that a statement has changed state.
     * @param serviceProvider the service provider instance under which the statement has been created
     * @param statement the statement that changed state
     */
    public void onStatementStateChange(EPServiceProvider serviceProvider, EPStatement statement);
}

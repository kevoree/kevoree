/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.variable;

import com.espertech.esper.core.StatementExtensionSvcContext;
import com.espertech.esper.client.EventType;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.Map;

/**
 * Variables service for reading and writing variables, and for setting a version number for the current thread to
 * consider variables for.
 * <p>
 * See implementation class for further details.
 */
public interface VariableService
{
    /**
     * Sets the variable version that subsequent reads consider.
     */
    public void setLocalVersion();

    /**
     * Lock for use in atomic writes to the variable space.
     * @return read write lock for external coordinated write
     */
    public ReadWriteLock getReadWriteLock();

    /**
     * Creates a new variable.
     * @param variableName name of the variable
     * @param type variable type
     * @param value initialization value; String values are allowed and parsed according to type
     * @param extensionServicesContext is extensions for implementing resilience attributes of variables
     * @throws VariableExistsException if the variable name is already in use
     * @throws VariableTypeException if the variable type cannot be recognized
     */
    public void createNewVariable(String variableName, String type, Object value, StatementExtensionSvcContext extensionServicesContext)
            throws VariableExistsException, VariableTypeException;

    /**
     * Returns a reader that provides access to variable values. The reader considers the
     * version currently set via setLocalVersion.
     * @param variableName the variable that the reader should read
     * @return reader
     */
    public VariableReader getReader(String variableName);

    /**
     * Registers a callback invoked when the variable is written with a new value.
     * @param variableNumber the variable index number
     * @param variableChangeCallback a callback 
     */
    public void registerCallback(int variableNumber, VariableChangeCallback variableChangeCallback);

    /**
     * Removes a callback.
     * @param variableNumber the variable index number
     * @param variableChangeCallback a callback
     */
    public void unregisterCallback(int variableNumber, VariableChangeCallback variableChangeCallback);

    /**
     * Writes a new variable value.
     * <p>
     * Must be followed by either a commit or rollback.
     * @param variableNumber the index number of the variable to write (from VariableReader)
     * @param newValue the new value
     */
    public void write(int variableNumber, Object newValue);

    /**
     * Check type of the value supplied and writes the new variable value.
     * <p>
     * Must be followed by either a commit or rollback.
     * @param variableNumber the index number of the variable to write (from VariableReader)
     * @param newValue the new value
     */
    public void checkAndWrite(int variableNumber, Object newValue);

    /**
     * Commits the variable outstanding changes.
     */
    public void commit();

    /**
     * Rolls back the variable outstanding changes.
     */
    public void rollback();

    /**
     * Returns a map of variable name and reader, for thread-safe iteration.
     * @return variable names and readers
     */
    public Map<String, VariableReader> getVariables();

    /**
     * Removes a variable.
     * @param name to remove
     */
    public void removeVariable(String name);
}

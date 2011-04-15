package com.espertech.esper.core;

import java.util.Set;

/**
 * Service for maintaining references between statement name and variables.
 */
public interface StatementVariableRef
{
    /**
     * Returns true if the variable is listed as in-use by any statement, or false if not
     * @param variableName name
     * @return indicator whether variable is in use
     */
    public boolean isInUse(String variableName);

    /**
     * Returns the set of statement names that use a given variable.
     * @param variableName name
     * @return set of statements or null if none found
     */
    public Set<String> getStatementNamesForVar(String variableName);
    
    /**
     * Add a reference from a statement name to a set of variables.
     * @param statementName name of statement
     * @param variablesReferenced types
     */
    public void addReferences(String statementName, Set<String> variablesReferenced);

    /**
     * Remove all references for a given statement.
     * @param statementName statement name
     */
    public void removeReferencesStatement(String statementName);

    /**
     * Remove all references for a given event type.
     * @param variableName variable name
     */
    public void removeReferencesVariable(String variableName);

    /**
     * Add a preconfigured variable.
     * @param variableName name
     */
    public void addConfiguredVariable(String variableName);

    /**
     * Remove a preconfigured variable.
     * @param variableName var
     */
    public void removeConfiguredVariable(String variableName);
}
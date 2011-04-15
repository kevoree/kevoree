package com.espertech.esper.core;

/**
 * Type of the statement.
 */
public enum StatementType
{
    /**
     * Pattern statement.
     */
    PATTERN,

    /**
     * Select statement that may contain one or more patterns.
     */
    SELECT,

    /**
     * Insert-into statement.
     */
    INSERT_INTO,

    /**
     * Create a named window statement.
     */
    CREATE_WINDOW,

    /**
     * Create a variable statement.
     */
    CREATE_VARIABLE,

    /**
     * Create-schema statement.
     */
    CREATE_SCHEMA,

    /**
     * Create-index statement.
     */
    CREATE_INDEX,

    /**
     * On-delete statement.
     */
    ON_DELETE,

    /**
     * On-select statement.
     */
    ON_SELECT,

    /**
     * On-insert statement.
     */
    ON_INSERT,

    /**
     * On-set statement.
     */
    ON_SET,

    /**
     * On-update statement.
     */
    ON_UPDATE,

    /**
     * Update statement.
     */
    UPDATE;
}

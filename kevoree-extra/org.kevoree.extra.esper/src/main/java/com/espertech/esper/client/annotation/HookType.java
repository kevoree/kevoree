package com.espertech.esper.client.annotation;

/**
 * Enumeration for the different types of statement-processing hooks (callbacks) that can be provided for a statement.
 */
public enum HookType
{
    /**
     * For use when installing a callback for converting SQL input parameters or column output values.
     */
    SQLCOL,

    /**
     * For use when installing a callback for converting SQL row results to a POJO object.
     */
    SQLROW
}

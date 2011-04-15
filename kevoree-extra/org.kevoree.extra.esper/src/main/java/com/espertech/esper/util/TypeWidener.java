package com.espertech.esper.util;

/**
 * Interface for a type widener.
 */
public interface TypeWidener
{
    /**
     * Ctor.
     * @param input the object to widen.
     * @return widened object.
     */
    public Object widen(Object input);
}

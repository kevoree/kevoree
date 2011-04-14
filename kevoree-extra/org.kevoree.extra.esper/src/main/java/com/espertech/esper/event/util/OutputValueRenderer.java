package com.espertech.esper.event.util;

/**
 * For rendering an output value returned by a property.
 */
public interface OutputValueRenderer
{
    /**
     * Renders the value to the buffer.
     * @param object to render
     * @param buf buffer to populate
     */
    public void render(Object object, StringBuilder buf);
}

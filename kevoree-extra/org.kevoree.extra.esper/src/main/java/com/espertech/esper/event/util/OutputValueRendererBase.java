package com.espertech.esper.event.util;

/**
 * Renderer for a Object values that can simply be output via to-string.
 */
public class OutputValueRendererBase implements OutputValueRenderer
{
    public void render(Object object, StringBuilder buf)
    {
        if (object == null)
        {
            buf.append("null");
            return;
        }
        
        buf.append(object.toString());
    }
}

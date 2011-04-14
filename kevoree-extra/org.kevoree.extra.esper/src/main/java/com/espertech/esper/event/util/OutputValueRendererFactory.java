package com.espertech.esper.event.util;

/**
 * For rendering an output value returned by a property.
 */
public class OutputValueRendererFactory
{
    private static OutputValueRenderer jsonStringOutput = new OutputValueRendererJSONString();
    private static OutputValueRenderer xmlStringOutput = new OutputValueRendererXMLString();
    private static OutputValueRenderer baseOutput = new OutputValueRendererBase();

    /**
     * Returns a renderer for an output value.
     * @param type to render
     * @param options options
     * @return renderer
     */
    protected static OutputValueRenderer getOutputValueRenderer(Class type, RendererMetaOptions options)
    {
        if (type.isArray())
        {
            type = type.getComponentType();
        }
        if ((type == String.class) || (type == Character.class) || (type == char.class))
        {
            if (options.isXMLOutput())
            {
                return xmlStringOutput;
            }
            else
            {
                return jsonStringOutput;
            }
        }
        else
        {
            return baseOutput;
        }
    }
}

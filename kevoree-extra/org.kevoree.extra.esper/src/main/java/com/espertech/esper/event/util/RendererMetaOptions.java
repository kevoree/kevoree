package com.espertech.esper.event.util;

/**
 * Options for use by {@link RendererMeta} with rendering metadata.
 */
public class RendererMetaOptions
{
    private final boolean isXMLOutput;
    private final boolean preventLooping;

    /**
     * Ctor.
     * @param preventLooping true to prevent looping
     * @param isXMLOutput true for XML output
     */
    public RendererMetaOptions(boolean preventLooping, boolean isXMLOutput)
    {
        this.preventLooping = preventLooping;
        this.isXMLOutput = isXMLOutput;
    }

    /**
     * Returns true to prevent looping.
     * @return prevent looping indicator
     */
    public boolean isPreventLooping()
    {
        return preventLooping;
    }

    /**
     * Returns true for XML output.
     * @return XML output flag
     */
    public boolean isXMLOutput()
    {
        return isXMLOutput;
    }
}

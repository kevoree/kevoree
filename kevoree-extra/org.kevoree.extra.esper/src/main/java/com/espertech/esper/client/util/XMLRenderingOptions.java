package com.espertech.esper.client.util;

/**
 * XML rendering options.
 */
public class XMLRenderingOptions
{
    private boolean preventLooping;
    private boolean defaultAsAttribute;

    /**
     * Ctor.
     */
    public XMLRenderingOptions()
    {
        preventLooping = true;
        defaultAsAttribute = false;
    }

    /**
     * Indicator whether to prevent looping, by default set to true.
     * Set to false to allow looping in the case where nested properties may refer to themselves, for example.
     * <p>
     * The algorithm to control looping considers the combination of event type and property name for each
     * level of nested property.
     * @return indicator whether the rendering algorithm prevents looping behavior
     */
    public boolean isPreventLooping()
    {
        return preventLooping;
    }

    /**
     * Indicator whether simple properties are rendered as attributes, this setting is false by default thereby
     * simple properties are rendered as elements.
     * @return true for simple properties rendered as attributes
     */
    public boolean isDefaultAsAttribute()
    {
        return defaultAsAttribute;
    }

    /**
     * Indicator whether to prevent looping, by default set to true.
     * Set to false to allow looping in the case where nested properties may refer to themselves, for example.
     * <p>
     * The algorithm to control looping considers the combination of event type and property name for each
     * level of nested property.
     * @param preventLooping indicator whether the rendering algorithm prevents looping behavior
     * @return options object
     */
    public XMLRenderingOptions setPreventLooping(boolean preventLooping)
    {
        this.preventLooping = preventLooping;
        return this;
    }

    /**
     * Indicator whether simple properties are rendered as elements (the default) or as attributes, this setting is false by default thereby
     * simple properties are rendered as elements.
     * @param defaultAsAttribute true for simple properties rendered as attributes
     * @return options object
     */
    public XMLRenderingOptions setDefaultAsAttribute(boolean defaultAsAttribute)
    {
        this.defaultAsAttribute = defaultAsAttribute;
        return this;
    }
}

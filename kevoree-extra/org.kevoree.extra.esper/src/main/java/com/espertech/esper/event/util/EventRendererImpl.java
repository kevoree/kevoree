package com.espertech.esper.event.util;

import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.util.*;
import com.espertech.esper.event.util.JSONRendererImpl;
import com.espertech.esper.event.util.XMLRendererImpl;

/**
 * Provider for rendering services of {@link com.espertech.esper.client.EventBean} events.
 */
public class EventRendererImpl implements EventRenderer
{
    /**
     * Returns a render for the JSON format, valid only for the given event type and its subtypes.
     * @param eventType to return renderer for
     * @param options rendering options
     * @return JSON format renderer
     */
    public JSONEventRenderer getJSONRenderer(EventType eventType, JSONRenderingOptions options)
    {
        return new JSONRendererImpl(eventType, options);
    }

    /**
     * Returns a render for the JSON format, valid only for the given event type and its subtypes.
     * @param eventType to return renderer for
     * @return JSON format renderer
     */
    public JSONEventRenderer getJSONRenderer(EventType eventType)
    {
        return new JSONRendererImpl(eventType, new JSONRenderingOptions());
    }

    /**
     * Quick-access method to render a given event in the JSON format.
     * <p>
     * Use the #getJSONRenderer to obtain a renderer instance that allows repeated rendering of the same type of event.
     * For performance reasons obtaining a dedicated renderer instance is the preferred method compared to repeated rendering via this method.
     * @param title the JSON root title
     * @param event the event to render
     * @return JSON formatted text
     */
    public String renderJSON(String title, EventBean event)
    {
        return renderJSON(title, event, new JSONRenderingOptions());
    }

    /**
     * Quick-access method to render a given event in the JSON format.
     * <p>
     * Use the #getJSONRenderer to obtain a renderer instance that allows repeated rendering of the same type of event.
     * For performance reasons obtaining a dedicated renderer instance is the preferred method compared to repeated rendering via this method.
     * @param title the JSON root title
     * @param event the event to render
     * @param options are JSON rendering options
     * @return JSON formatted text
     */
    public String renderJSON(String title, EventBean event, JSONRenderingOptions options)
    {
        if (event == null)
        {
            return null;
        }
        return getJSONRenderer(event.getEventType(), options).render(title, event);
    }

    /**
     * Returns a render for the XML format, valid only for the given event type and its subtypes.
     * @param eventType to return renderer for
     * @return XML format renderer
     */
    public XMLEventRenderer getXMLRenderer(EventType eventType)
    {
        return new XMLRendererImpl(eventType, new XMLRenderingOptions());
    }

    /**
     * Returns a render for the XML format, valid only for the given event type and its subtypes.
     * @param eventType to return renderer for
     * @param options rendering options
     * @return XML format renderer
     */
    public XMLEventRenderer getXMLRenderer(EventType eventType, XMLRenderingOptions options)
    {
        return new XMLRendererImpl(eventType, options);
    }

    /**
     * Quick-access method to render a given event in the XML format.
     * <p>
     * Use the #getXMLRenderer to obtain a renderer instance that allows repeated rendering of the same type of event.
     * For performance reasons obtaining a dedicated renderer instance is the preferred method compared to repeated rendering via this method.
     * @param rootElementName the root element name that may also include namespace information
     * @param event the event to render
     * @return XML formatted text
     */
    public String renderXML(String rootElementName, EventBean event)
    {
        return renderXML(rootElementName, event, new XMLRenderingOptions());
    }

    /**
     * Quick-access method to render a given event in the XML format.
     * <p>
     * Use the #getXMLRenderer to obtain a renderer instance that allows repeated rendering of the same type of event.
     * For performance reasons obtaining a dedicated renderer instance is the preferred method compared to repeated rendering via this method.
     * @param rootElementName the root element name that may also include namespace information
     * @param event the event to render
     * @param options are XML rendering options
     * @return XML formatted text
     */
    public String renderXML(String rootElementName, EventBean event, XMLRenderingOptions options)
    {
        if (event == null)
        {
            return null;
        }
        return getXMLRenderer(event.getEventType(), options).render(rootElementName, event);
    }
}

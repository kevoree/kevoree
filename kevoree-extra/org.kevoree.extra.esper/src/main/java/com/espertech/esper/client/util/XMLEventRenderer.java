package com.espertech.esper.client.util;

import com.espertech.esper.client.EventBean;

/**
 * Renderer for an event into the XML textual format.
 * <p>
 * A renderer is dedicated to rendering only a certain type of events and subtypes of that type, as the
 * render cache type metadata and prepares structures to enable fast rendering.
 * <p>
 * For rendering events of different types, use a quick-access method in {@link EventRenderer}.
 */
public interface XMLEventRenderer
{
    /**
     * Render a given event in the XML format.
     * @param rootElementName the name of the root element, may include namespace information
     * @param event the event to render
     * @return XML formatted text
     */
    public String render(String rootElementName, EventBean event);
}

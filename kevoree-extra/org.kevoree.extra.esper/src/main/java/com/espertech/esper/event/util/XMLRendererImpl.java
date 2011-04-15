package com.espertech.esper.event.util;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.util.XMLEventRenderer;
import com.espertech.esper.client.util.XMLRenderingOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;
import java.util.Stack;
import java.util.Map;
import java.util.Iterator;

/**
 * Renderer for XML-formatted properties.
 */
public class XMLRendererImpl implements XMLEventRenderer
{
    private static final Log log = LogFactory.getLog(XMLRendererImpl.class);
    private final static String NEWLINE = System.getProperty("line.separator");
    
    private final RendererMeta meta;
    private final XMLRenderingOptions options;
    private final RendererMetaOptions rendererMetaOptions;

    /**
     * Ctor.
     * @param eventType type of event to render
     * @param options rendering options
     */
    public XMLRendererImpl(EventType eventType, XMLRenderingOptions options)
    {
        rendererMetaOptions = new RendererMetaOptions(options.isPreventLooping(), true);
        meta = new RendererMeta(eventType, new Stack<EventTypePropertyPair>(), rendererMetaOptions);
        this.options = options;
    }

    public String render(String rootElementName, EventBean event)
    {
        if (options.isDefaultAsAttribute())
        {
            return renderAttributeXML(rootElementName, event);
        }
        return renderElementXML(rootElementName, event);
    }

    private String renderElementXML(String rootElementName, EventBean event)
    {
        StringBuilder buf = new StringBuilder();

        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buf.append(NEWLINE);

        buf.append('<');
        buf.append(rootElementName);
        buf.append('>');
        buf.append(NEWLINE);

        recursiveRender(event, buf, 1, meta, rendererMetaOptions);
            
        buf.append("</");
        buf.append(getFirstWord(rootElementName));
        buf.append('>');

        return buf.toString();
    }

    private String renderAttributeXML(String rootElementName, EventBean event)
    {
        StringBuilder buf = new StringBuilder();

        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buf.append(NEWLINE);

        buf.append('<');
        buf.append(rootElementName);
        renderAttributes(event, buf, meta);

        String inner = renderAttElements(event, 1, meta);

        if ((inner == null) || (inner.trim().length() == 0))
        {
            buf.append("/>");
            buf.append(NEWLINE);
        }
        else
        {
            buf.append(">");
            buf.append(NEWLINE);
            buf.append(inner);
            buf.append("</");
            buf.append(getFirstWord(rootElementName));
            buf.append('>');
        }

        return buf.toString();
    }

    private String renderAttElements(EventBean event, int level, RendererMeta meta)
    {
        StringBuilder buf = new StringBuilder();

        GetterPair[] indexProps = meta.getIndexProperties();
        for (GetterPair indexProp : indexProps)
        {
            Object value = indexProp.getGetter().get(event);

            if (value == null)
            {
                continue;
            }
            if (!value.getClass().isArray())
            {
                log.warn("Property '" + indexProp.getName() + "' returned a non-array object");
                continue;
            }
            for (int i = 0; i < Array.getLength(value); i++)
            {
                Object arrayItem = Array.get(value, i);

                if (arrayItem == null)
                {
                    continue;
                }

                ident(buf, level);
                buf.append('<');
                buf.append(indexProp.getName());
                buf.append('>');
                indexProp.getOutput().render(arrayItem, buf);
                buf.append("</");
                buf.append(indexProp.getName());
                buf.append('>');
                buf.append(NEWLINE);
            }
        }

        GetterPair[] mappedProps = meta.getMappedProperties();
        for (GetterPair mappedProp : mappedProps)
        {
            Object value = mappedProp.getGetter().get(event);

            if ((value != null) && (!(value instanceof Map)))
            {
                log.warn("Property '" + mappedProp.getName() + "' expected to return Map and returned " + value.getClass() + " instead");
                continue;
            }

            ident(buf, level);
            buf.append('<');
            buf.append(mappedProp.getName());

            if (value != null)
            {
                Map map = (Map) value;
                if (!map.isEmpty())
                {
                    Iterator<Map.Entry> it = map.entrySet().iterator();
                    for (;it.hasNext();)
                    {
                        Map.Entry entry = it.next();
                        if ((entry.getKey() == null) || (entry.getValue() == null))
                        {
                            continue;
                        }

                        buf.append(" ");
                        buf.append(entry.getKey().toString());
                        buf.append("=\"");
                        OutputValueRenderer out = OutputValueRendererFactory.getOutputValueRenderer(entry.getValue().getClass(), rendererMetaOptions);
                        out.render(entry.getValue(), buf);
                        buf.append("\"");
                    }
                }
            }

            buf.append("/>");
            buf.append(NEWLINE);
        }

        NestedGetterPair[] nestedProps = meta.getNestedProperties();
        for (NestedGetterPair nestedProp : nestedProps)
        {
            Object value = nestedProp.getGetter().getFragment(event);

            if (value == null)
            {
                continue;
            }

            if (!nestedProp.isArray())
            {
                if (!(value instanceof EventBean))
                {
                    log.warn("Property '" + nestedProp.getName() + "' expected to return EventBean and returned " + value.getClass() + " instead");
                    buf.append("null");
                    continue;
                }
                EventBean nestedEventBean = (EventBean) value;
                renderAttInner(buf, level, nestedEventBean, nestedProp);
            }
            else
            {
                if (!(value instanceof EventBean[]))
                {
                    log.warn("Property '" + nestedProp.getName() + "' expected to return EventBean[] and returned " + value.getClass() + " instead");
                    buf.append("null");
                    continue;
                }

                EventBean[] nestedEventArray = (EventBean[]) value;
                for (int i = 0; i < nestedEventArray.length; i++)
                {
                    EventBean arrayItem = nestedEventArray[i];
                    renderAttInner(buf, level, arrayItem, nestedProp);
                }
            }
        }

        return buf.toString();
    }

    private void renderAttributes(EventBean event, StringBuilder buf, RendererMeta meta)
    {
        String delimiter = " ";
        GetterPair[] simpleProps = meta.getSimpleProperties();
        for (GetterPair simpleProp : simpleProps)
        {
            Object value = simpleProp.getGetter().get(event);

            if (value == null)
            {
                continue;
            }

            buf.append(delimiter);
            buf.append(simpleProp.getName());
            buf.append("=\"");
            simpleProp.getOutput().render(value, buf);
            buf.append('"');
        }
    }

    private static void ident(StringBuilder buf, int level)
    {
        for (int i = 0; i < level; i++)
        {
            indentChar(buf);
        }
    }

    private static void indentChar(StringBuilder buf)
    {
        buf.append(' ');
        buf.append(' ');
    }

    private static void recursiveRender(EventBean event, StringBuilder buf, int level, RendererMeta meta, RendererMetaOptions rendererMetaOptions)
    {
        GetterPair[] simpleProps = meta.getSimpleProperties();
        for (GetterPair simpleProp : simpleProps)
        {
            Object value = simpleProp.getGetter().get(event);

            if (value == null)
            {
                continue;
            }

            ident(buf, level);
            buf.append('<');
            buf.append(simpleProp.getName());
            buf.append('>');
            simpleProp.getOutput().render(value, buf);
            buf.append("</");
            buf.append(simpleProp.getName());
            buf.append('>');
            buf.append(NEWLINE);
        }

        GetterPair[] indexProps = meta.getIndexProperties();
        for (GetterPair indexProp : indexProps)
        {
            Object value = indexProp.getGetter().get(event);

            if (value == null)
            {
                continue;
            }
            if (!value.getClass().isArray())
            {
                log.warn("Property '" + indexProp.getName() + "' returned a non-array object");
                continue;
            }
            for (int i = 0; i < Array.getLength(value); i++)
            {
                Object arrayItem = Array.get(value, i);

                if (arrayItem == null)
                {
                    continue;
                }

                ident(buf, level);
                buf.append('<');
                buf.append(indexProp.getName());
                buf.append('>');
                indexProp.getOutput().render(arrayItem, buf);
                buf.append("</");
                buf.append(indexProp.getName());
                buf.append('>');
                buf.append(NEWLINE);
            }
        }

        GetterPair[] mappedProps = meta.getMappedProperties();
        for (GetterPair mappedProp : mappedProps)
        {
            Object value = mappedProp.getGetter().get(event);

            if ((value != null) && (!(value instanceof Map)))
            {
                log.warn("Property '" + mappedProp.getName() + "' expected to return Map and returned " + value.getClass() + " instead");
                continue;
            }

            ident(buf, level);
            buf.append('<');
            buf.append(mappedProp.getName());
            buf.append('>');
            buf.append(NEWLINE);

            if (value != null)
            {
                Map map = (Map) value;
                if (!map.isEmpty())
                {
                    String localDelimiter = "";
                    Iterator<Map.Entry> it = map.entrySet().iterator();
                    for (;it.hasNext();)
                    {
                        Map.Entry entry = it.next();
                        if (entry.getKey() == null)
                        {
                            continue;
                        }

                        buf.append(localDelimiter);
                        ident(buf, level + 1);
                        buf.append('<');
                        buf.append(entry.getKey().toString());
                        buf.append('>');

                        if (entry.getValue() != null)
                        {
                            OutputValueRenderer out = OutputValueRendererFactory.getOutputValueRenderer(entry.getValue().getClass(), rendererMetaOptions);
                            out.render(entry.getValue(), buf);
                        }

                        buf.append('<');
                        buf.append(entry.getKey().toString());
                        buf.append('>');
                        localDelimiter = NEWLINE;
                    }
                }
            }

            buf.append(NEWLINE);
            ident(buf, level);
            buf.append("</");
            buf.append(mappedProp.getName());
            buf.append('>');
            buf.append(NEWLINE);
        }

        NestedGetterPair[] nestedProps = meta.getNestedProperties();
        for (NestedGetterPair nestedProp : nestedProps)
        {
            Object value = nestedProp.getGetter().getFragment(event);

            if (value == null)
            {
                continue;
            }

            if (!nestedProp.isArray())
            {
                if (!(value instanceof EventBean))
                {
                    log.warn("Property '" + nestedProp.getName() + "' expected to return EventBean and returned " + value.getClass() + " instead");
                    buf.append("null");
                    continue;
                }
                renderElementFragment((EventBean) value, buf, level, nestedProp, rendererMetaOptions);
            }
            else
            {
                if (!(value instanceof EventBean[]))
                {
                    log.warn("Property '" + nestedProp.getName() + "' expected to return EventBean[] and returned " + value.getClass() + " instead");
                    buf.append("null");
                    continue;
                }

                EventBean[] nestedEventArray = (EventBean[]) value;
                for (int i = 0; i < nestedEventArray.length; i++)
                {
                    EventBean arrayItem = nestedEventArray[i];
                    if (arrayItem == null)
                    {
                        continue;
                    }
                    renderElementFragment(arrayItem, buf, level, nestedProp, rendererMetaOptions);
                }
            }
        }
    }

    private static void renderElementFragment(EventBean eventBean, StringBuilder buf, int level, NestedGetterPair nestedProp, RendererMetaOptions rendererMetaOptions)
    {
        ident(buf, level);
        buf.append('<');
        buf.append(nestedProp.getName());
        buf.append('>');
        buf.append(NEWLINE);

        recursiveRender(eventBean, buf, level + 1, nestedProp.getMetadata(), rendererMetaOptions);

        ident(buf, level);
        buf.append("</");
        buf.append(nestedProp.getName());
        buf.append('>');
        buf.append(NEWLINE);
    }

    private void renderAttInner(StringBuilder buf, int level, EventBean nestedEventBean, NestedGetterPair nestedProp)
    {
        ident(buf, level);
        buf.append('<');
        buf.append(nestedProp.getName());

        renderAttributes(nestedEventBean, buf, nestedProp.getMetadata());

        String inner = renderAttElements(nestedEventBean, level + 1, nestedProp.getMetadata());

        if ((inner == null) || (inner.trim().length() == 0))
        {
            buf.append("/>");
            buf.append(NEWLINE);
        }
        else
        {
            buf.append(">");
            buf.append(NEWLINE);
            buf.append(inner);

            ident(buf, level);
            buf.append("</");
            buf.append(nestedProp.getName());
            buf.append('>');
            buf.append(NEWLINE);
        }
    }

    private String getFirstWord(String rootElementName)
    {
        if ((rootElementName == null) || (rootElementName.trim().length() == 0))
        {
            return rootElementName;
        }
        int index = rootElementName.indexOf(' ');
        if (index < 0)
        {
            return rootElementName;
        }
        return rootElementName.substring(0, index);
    }
}

package com.espertech.esper.event.util;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.util.JSONEventRenderer;
import com.espertech.esper.client.util.JSONRenderingOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;
import java.util.Stack;
import java.util.Map;
import java.util.Iterator;

/**
 * Render for the JSON format.
 */
public class JSONRendererImpl implements JSONEventRenderer
{
    private static final Log log = LogFactory.getLog(JSONRendererImpl.class);

    private final static String NEWLINE = System.getProperty("line.separator");
    private final static String COMMA_DELIMITER_NEWLINE = "," + NEWLINE;

    private final RendererMeta meta;
    private final RendererMetaOptions rendererOptions;

    /**
     * Ctor.
     * @param eventType type of event(s)
     * @param options rendering options
     */
    public JSONRendererImpl(EventType eventType, JSONRenderingOptions options)
    {
        rendererOptions = new RendererMetaOptions(options.isPreventLooping(), false);
        meta = new RendererMeta(eventType, new Stack<EventTypePropertyPair>(), rendererOptions);
    }

    public String render(String title, EventBean event)
    {
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        buf.append(NEWLINE);

        ident(buf, 1);
        buf.append('\"');
        buf.append(title);
        buf.append("\": {");
        buf.append(NEWLINE);

        recursiveRender(event, buf, 2, meta, rendererOptions);
        
        ident(buf, 1);
        buf.append('}');
        buf.append(NEWLINE);

        buf.append('}');
        buf.append(NEWLINE);

        return buf.toString();
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

    private static void recursiveRender(EventBean event, StringBuilder buf, int level, RendererMeta meta, RendererMetaOptions rendererOptions)
    {
        String delimiter = "";

        GetterPair[] simpleProps = meta.getSimpleProperties();
        for (GetterPair simpleProp : simpleProps)
        {
            Object value = simpleProp.getGetter().get(event);
            
            buf.append(delimiter);
            ident(buf, level);
            buf.append('\"');
            buf.append(simpleProp.getName());
            buf.append("\": ");
            simpleProp.getOutput().render(value, buf);
            delimiter = COMMA_DELIMITER_NEWLINE;
        }

        GetterPair[] indexProps = meta.getIndexProperties();
        for (GetterPair indexProp : indexProps)
        {
            Object value = indexProp.getGetter().get(event);

            buf.append(delimiter);
            ident(buf, level);

            buf.append('\"');
            buf.append(indexProp.getName());
            buf.append("\": ");

            if (value == null)
            {
                buf.append("null");
            }
            else
            {
                if (!value.getClass().isArray())
                {
                    buf.append("[]");
                }
                else
                {
                    buf.append('[');
                    String arrayDelimiter = "";
                    for (int i = 0; i < Array.getLength(value); i++)
                    {
                        Object arrayItem = Array.get(value, i);
                        buf.append(arrayDelimiter);
                        indexProp.getOutput().render(arrayItem, buf);
                        arrayDelimiter = ", ";
                    }
                    buf.append(']');
                }
            }
            delimiter = COMMA_DELIMITER_NEWLINE;
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

            buf.append(delimiter);
            ident(buf, level);

            buf.append('\"');
            buf.append(mappedProp.getName());
            buf.append("\": ");

            if (value == null)
            {
                buf.append("null");
            }
            else
            {
                Map map = (Map) value;
                if (map.isEmpty())
                {
                    buf.append("{}");
                }
                else
                {
                    buf.append('{');
                    buf.append(NEWLINE);

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
                        buf.append('\"');
                        buf.append(entry.getKey().toString());
                        buf.append("\": ");

                        if (entry.getValue() == null)
                        {
                            buf.append("null");
                        }
                        else
                        {
                            OutputValueRenderer out = OutputValueRendererFactory.getOutputValueRenderer(entry.getValue().getClass(), rendererOptions);
                            out.render(entry.getValue(), buf);
                        }
                        localDelimiter = COMMA_DELIMITER_NEWLINE;
                    }
                }
            }
            
            buf.append(NEWLINE);
            ident(buf, level);
            buf.append('}');

            delimiter = COMMA_DELIMITER_NEWLINE;
        }

        NestedGetterPair[] nestedProps = meta.getNestedProperties();
        for (NestedGetterPair nestedProp : nestedProps)
        {
            Object value = nestedProp.getGetter().getFragment(event);

            buf.append(delimiter);
            ident(buf, level);

            buf.append('\"');
            buf.append(nestedProp.getName());
            buf.append("\": ");

            if (value == null)
            {
                buf.append("null");
            }
            else if (!nestedProp.isArray())
            {
                if (!(value instanceof EventBean))
                {
                    log.warn("Property '" + nestedProp.getName() + "' expected to return EventBean and returned " + value.getClass() + " instead");
                    buf.append("null");
                    continue;
                }
                EventBean nestedEventBean = (EventBean) value;
                buf.append('{');
                buf.append(NEWLINE);
                
                recursiveRender(nestedEventBean, buf, level + 1, nestedProp.getMetadata(), rendererOptions);

                ident(buf, level);
                buf.append('}');
            }
            else
            {
                if (!(value instanceof EventBean[]))
                {
                    log.warn("Property '" + nestedProp.getName() + "' expected to return EventBean[] and returned " + value.getClass() + " instead");
                    buf.append("null");
                    continue;
                }


                StringBuilder arrayDelimiterBuf = new StringBuilder();
                arrayDelimiterBuf.append(',');
                arrayDelimiterBuf.append(NEWLINE);
                ident(arrayDelimiterBuf, level + 1);

                EventBean[] nestedEventArray = (EventBean[]) value;
                String arrayDelimiter = "";
                buf.append('[');
                
                for (int i = 0; i < nestedEventArray.length; i++)
                {
                    EventBean arrayItem = nestedEventArray[i];
                    buf.append(arrayDelimiter);
                    arrayDelimiter = arrayDelimiterBuf.toString();

                    buf.append('{');
                    buf.append(NEWLINE);

                    recursiveRender(arrayItem, buf, level + 2, nestedProp.getMetadata(), rendererOptions);

                    ident(buf, level + 1);
                    buf.append('}');
                }
                buf.append(']');
            }
            delimiter = COMMA_DELIMITER_NEWLINE;
        }

        buf.append(NEWLINE);
    }
}

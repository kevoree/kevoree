package com.espertech.esper.event.xml;

import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.util.SimpleTypeParserFactory;
import com.espertech.esper.util.SimpleTypeParser;
import org.w3c.dom.Node;

/**
 * Getter for parsing node content to a desired type.  
 */
public class DOMConvertingGetter implements EventPropertyGetter
{
    private final DOMPropertyGetter getter;
    private final SimpleTypeParser parser;

    /**
     * Ctor.
     * @param propertyExpression property name
     * @param domPropertyGetter getter
     * @param returnType desired result type
     */
    public DOMConvertingGetter(String propertyExpression, DOMPropertyGetter domPropertyGetter, Class returnType)
    {
        this.getter = domPropertyGetter;
        this.parser = SimpleTypeParserFactory.getParser(returnType);
    }

    public Object get(EventBean obj) throws PropertyAccessException
    {
        // The underlying is expected to be a map
        if (!(obj.getUnderlying() instanceof Node))
        {
            throw new PropertyAccessException("Mismatched property getter to event bean type, " +
                    "the underlying data object is not of type Node");
        }

        Node node = (Node) obj.getUnderlying();

        Node result = getter.getValueAsNode(node);
        if (result == null)
        {
            return null;
        }

        String text = result.getTextContent();
        if (text == null)
        {
            return null;
        }
        
        return parser.parse(text);
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true;
    }

    public Object getFragment(EventBean eventBean) throws PropertyAccessException
    {
        return null;
    }
}

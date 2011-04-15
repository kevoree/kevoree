/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.xml;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.property.Property;
import com.espertech.esper.event.property.PropertyParser;
import com.espertech.esper.type.IntValue;
import com.espertech.esper.type.StringValue;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.List;

/**
 * Parses event property names and transforms to XPath expressions using the schema information supplied. Supports the
 * nested, indexed and mapped event properties.
 */
public class SchemaXMLPropertyParser
{
    /**
     * Return the xPath corresponding to the given property.
     * The propertyName String may be simple, nested, indexed or mapped.
     *
     * @param propertyName is the event property name
     * @param namespace is the default namespace
     * @param schemaModel is the schema model
     * @param xPathFactory is the xpath factory instance to use
     * @param rootElementName is the name of the root element
     * @param eventAdapterService for type lookup and creation
     * @param xmlEventType the resolving type
     * @param isAllowFragment whether fragmenting is allowed
     * @param defaultNamespace default namespace
     * @return xpath expression
     * @throws EPException is there are XPath errors
     */
    public static EventPropertyGetter getXPathResolution(String propertyName,
                                                         XPathFactory xPathFactory,
                                                         String rootElementName,
                                                         String namespace,
                                                         SchemaModel schemaModel,
                                                         EventAdapterService eventAdapterService,
                                                         BaseXMLEventType xmlEventType,
                                                         boolean isAllowFragment,
                                                         String defaultNamespace) throws EPException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Determining XPath expression for property '" + propertyName + "'");
        }

        XPathNamespaceContext ctx = new XPathNamespaceContext();
        List<String> namespaces = schemaModel.getNamespaces();

        String defaultNamespacePrefix = null;
        for (int i = 0; i < namespaces.size(); i++)
        {
            String prefix = "n" + i;
            ctx.addPrefix(prefix, namespaces.get(i));
            if ((defaultNamespace != null) && (defaultNamespace.equals(namespaces.get(i))))
            {
                defaultNamespacePrefix = prefix;
            }
        }

        Tree ast = PropertyParser.parse(propertyName);        
        Property property = PropertyParser.parse(propertyName, false);
        boolean isDynamic = property.isDynamic();

        SchemaElementComplex rootComplexElement = SchemaUtil.findRootElement(schemaModel, namespace, rootElementName);
        String prefix = ctx.getPrefix(rootComplexElement.getNamespace());
        if (prefix == null) {
            prefix = "";
        }
        else {
            prefix += ':';
        }

        StringBuilder xPathBuf = new StringBuilder();
        xPathBuf.append('/');
        xPathBuf.append(prefix);
        if (rootElementName.startsWith("//"))
        {
            xPathBuf.append(rootElementName.substring(2));    
        }
        else
        {
            xPathBuf.append(rootElementName);
        }

        SchemaElementComplex parentComplexElement = rootComplexElement;
        Pair<String, QName> pair = null;

        if (ast.getChildCount() == 1)
        {
            pair = makeProperty(rootComplexElement, ast.getChild(0), ctx, true, isDynamic, defaultNamespacePrefix);
            if (pair == null)
            {
                throw new PropertyAccessException("Failed to locate property '" + propertyName + "' in schema");
            }
            xPathBuf.append(pair.getFirst());
        }
        else
        {
            for (int i = 0; i < ast.getChildCount(); i++)
            {
                boolean isLast = (i == ast.getChildCount() - 1);
                Tree child = ast.getChild(i);
                pair = makeProperty(parentComplexElement, child, ctx, isLast, isDynamic, defaultNamespacePrefix);
                if (pair == null)
                {
                    throw new PropertyAccessException("Failed to locate property '" + propertyName + "' nested property part '" + child.toString() + "' in schema");
                }

                String text = child.getChild(0).getText();
                SchemaItem obj = SchemaUtil.findPropertyMapping(parentComplexElement, text);
                if (obj instanceof SchemaElementComplex)
                {
                    parentComplexElement = (SchemaElementComplex) obj;
                }
                xPathBuf.append(pair.getFirst());
            }
        }

        String xPath = xPathBuf.toString();
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".parse XPath for property '" + propertyName + "' is expression=" + xPath);
        }

        // Compile assembled XPath expression
        XPath path = xPathFactory.newXPath();
        path.setNamespaceContext(ctx);

        if (log.isDebugEnabled())
        {
            log.debug("Compiling XPath expression '" + xPath + "' for property '" + propertyName + "' using namespace context :" + ctx);
        }

        XPathExpression expr;
        try
        {
            expr = path.compile(xPath);
        }
        catch (XPathExpressionException e) {
            String detail = "Error constructing XPath expression from property expression '" + propertyName + "' expression '" + xPath + "'";
            if (e.getMessage() != null)
            {
                throw new EPException(detail + " :" + e.getMessage(), e);
            }
            throw new EPException(detail, e);
        }

        // get type
        SchemaItem item = property.getPropertyTypeSchema(rootComplexElement, eventAdapterService);
        if ((item == null) && (!isDynamic))
        {
            return null;
        }

        Class resultType;
        if (!isDynamic)
        {
            resultType = SchemaUtil.toReturnType(item);
        }
        else
        {
            resultType = Node.class;
        }

        FragmentFactory fragmentFactory = null;
        if (isAllowFragment)
        {
            fragmentFactory = new FragmentFactoryDOMGetter(eventAdapterService, xmlEventType, propertyName);
        }
        return new XPathPropertyGetter(propertyName, xPath, expr, pair.getSecond(), resultType, fragmentFactory);
    }

    private static Pair<String, QName> makeProperty(SchemaElementComplex parent, Tree child, XPathNamespaceContext ctx, boolean isLast, boolean isDynamic, String defaultNamespacePrefix)
    {
        String text = child.getChild(0).getText();
        SchemaItem obj = SchemaUtil.findPropertyMapping(parent, text);
        if ((obj instanceof SchemaElementSimple) || (obj instanceof SchemaElementComplex)){
            return makeElementProperty((SchemaElement) obj, child, ctx, isLast, isDynamic, defaultNamespacePrefix);
        }
        else if (obj != null) {
            return makeAttributeProperty((SchemaItemAttribute) obj, child, ctx);
        }
        else if (isDynamic)
        {
            return makeElementProperty(null, child, ctx, isLast, isDynamic, defaultNamespacePrefix);
        }
        else
        {
            return null;
        }
    }

    private static Pair<String, QName> makeAttributeProperty(SchemaItemAttribute attribute, Tree child, XPathNamespaceContext ctx)
    {
        String prefix = ctx.getPrefix(attribute.getNamespace());
        if (prefix == null)
            prefix = "";
        else
            prefix += ':';
        switch (child.getType())
        {
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_SIMPLE:
            case EsperEPL2GrammarParser.EVENT_PROP_SIMPLE:
                QName type = SchemaUtil.simpleTypeToQName(attribute.getXsSimpleType());
                String path = "/@" + prefix + child.getChild(0).getText();
                return new Pair<String, QName>(path, type);
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_MAPPED:
            case EsperEPL2GrammarParser.EVENT_PROP_MAPPED:
                throw new RuntimeException("Mapped properties not applicable to attributes");
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_INDEXED:
            case EsperEPL2GrammarParser.EVENT_PROP_INDEXED:
                throw new RuntimeException("Mapped properties not applicable to attributes");
            default:
                throw new IllegalStateException("Event property AST node not recognized, type=" + child.getType());
        }
    }

    private static Pair<String, QName> makeElementProperty(SchemaElement schemaElement, Tree child, XPathNamespaceContext ctx, boolean isAlone, boolean isDynamic, String defaultNamespacePrefix)
    {
        QName type;
        if (isDynamic)
        {
            type = XPathConstants.NODE;
        }
        else if (schemaElement instanceof SchemaElementSimple) {
            type = SchemaUtil.simpleTypeToQName(((SchemaElementSimple) schemaElement).getXsSimpleType());
        }
        else
        {
            SchemaElementComplex complex = (SchemaElementComplex) schemaElement;
            if (complex.getOptionalSimpleType() != null)
            {
                type = SchemaUtil.simpleTypeToQName(complex.getOptionalSimpleType());
            }
            else
            {
                // The result is a node
                type = XPathConstants.NODE;
            }
        }

        String prefix;
        if (!isDynamic) {
            prefix = ctx.getPrefix(schemaElement.getNamespace());
        }
        else
        {
            prefix = defaultNamespacePrefix;
        }
        if (prefix == null) {
            prefix = "";
        }
        else {
            prefix += ':';
        }

        switch (child.getType())
        {
            case EsperEPL2GrammarParser.EVENT_PROP_SIMPLE:
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_SIMPLE:
                if (!isDynamic && schemaElement.isArray() && !isAlone) {
                    throw new PropertyAccessException("Simple property not allowed in repeating elements at '" + schemaElement.getName() + "'");
                }
                return new Pair<String, QName>('/' + prefix + child.getChild(0).getText(), type);

            case EsperEPL2GrammarParser.EVENT_PROP_MAPPED:
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_MAPPED:
                if (!isDynamic && !schemaElement.isArray()) {
                    throw new PropertyAccessException("Element " + child.getChild(0).getText() + " is not a collection, cannot be used as mapped property");
                }
                String key = StringValue.parseString(child.getChild(1).getText());
                return new Pair<String, QName>('/' + prefix + child.getChild(0).getText() + "[@id='" + key + "']", type);

            case EsperEPL2GrammarParser.EVENT_PROP_INDEXED:
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_INDEXED:
                if (!isDynamic && !schemaElement.isArray()) {
                    throw new PropertyAccessException("Element " + child.getChild(0).getText() + " is not a collection, cannot be used as mapped property");
                }
                int index = IntValue.parseString(child.getChild(1).getText());
                int xPathPosition = index + 1;
                return new Pair<String, QName>('/' + prefix + child.getChild(0).getText() + "[position() = " + xPathPosition + ']', type);

            default:
                throw new IllegalStateException("Event property AST node not recognized, type=" + child.getType());
        }
    }

    private static Log log = LogFactory.getLog(SchemaXMLPropertyParser.class);
}

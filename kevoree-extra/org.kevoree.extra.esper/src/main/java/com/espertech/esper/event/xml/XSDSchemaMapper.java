package com.espertech.esper.event.xml;

import com.espertech.esper.client.ConfigurationException;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.ResourceLoader;
import com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl;
import com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.xs.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Helper class for mapping a XSD schema model to an internal representation.
 */
public class XSDSchemaMapper
{
    private static final Log log = LogFactory.getLog(XSDSchemaMapper.class);

    private static final int JAVA5_COMPLEX_TYPE = 13;
    private static final int JAVA5_SIMPLE_TYPE = 14;
    private static final int JAVA6_COMPLEX_TYPE = 15;
    private static final int JAVA6_SIMPLE_TYPE = 16;

    /**
     * Loading and mapping of the schema to the internal representation.
     * @param schemaResource schema to load and map.
     * @param maxRecusiveDepth depth of maximal recursive element
     * @return model
     */
    public static SchemaModel loadAndMap(String schemaResource, String schemaText, int maxRecusiveDepth)
    {
        // Load schema
        XSModel model;
        try
        {
            model = readSchemaInternal(schemaResource, schemaText);
        }
        catch (ConfigurationException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ConfigurationException("Failed to read schema '" + schemaResource + "' : " + ex.getMessage(), ex);
        }

        // Map schema to internal representation
        return map(model, maxRecusiveDepth);
    }

    private static XSModel readSchemaInternal(String schemaResource, String schemaText) throws IllegalAccessException, InstantiationException, ClassNotFoundException,
            ConfigurationException, URISyntaxException
    {
        LSInputImpl input = null;
        String baseURI = null;
        if (schemaResource != null) {
            URL url = ResourceLoader.resolveClassPathOrURLResource("schema", schemaResource);
            baseURI = url.toURI().toString();
        }
        else {
            input = new LSInputImpl(schemaText);
        }

        // Uses Xerxes internal classes
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        registry.addSource(new DOMXSImplementationSourceImpl());
        Object xsImplementation = registry.getDOMImplementation("XS-Loader");
        if (xsImplementation == null) {
            throw new ConfigurationException("Failed to retrieve XS-Loader implementation from registry obtained via DOMImplementationRegistry.newInstance, please check that registry.getDOMImplementation(\"XS-Loader\") returns an instance");
        }
        if (!JavaClassHelper.isImplementsInterface(xsImplementation.getClass(), XSImplementation.class)) {
            String message = "The XS-Loader instance returned by the DOM registry class '" + xsImplementation.getClass().getName() + "' does not implement the interface '" + XSImplementation.class.getName() + "'; If you have a another Xerces distribution in your classpath please ensure the classpath order loads the JRE Xerces distribution or set the DOMImplementationRegistry.PROPERTY system property";
            throw new ConfigurationException(message);
        }
        XSImplementation impl =(XSImplementation) xsImplementation; 
        XSLoader schemaLoader = impl.createXSLoader(null);
        XSModel xsModel;
        if (input != null) {
            xsModel = schemaLoader.load(input);
        }
        else {
            xsModel = schemaLoader.loadURI(baseURI);
        }

        if (xsModel == null)
        {
            throw new ConfigurationException("Failed to read schema via URL '" + schemaResource + '\'');
        }

        return xsModel;
    }

    private static SchemaModel map(XSModel xsModel, int maxRecusiveDepth)
    {
        // get namespaces
        StringList namespaces = xsModel.getNamespaces();
        List<String> namesspaceList = new ArrayList<String>();
        for (int i = 0; i < namespaces.getLength(); i++)
        {
            namesspaceList.add(namespaces.item(i));
        }

        // get top-level complex elements
        XSNamedMap elements = xsModel.getComponents(XSConstants.ELEMENT_DECLARATION);
        List<SchemaElementComplex> components = new ArrayList<SchemaElementComplex>();

        for (int i = 0; i < elements.getLength(); i++)
        {
            XSObject object = elements.item(i);
            if (!(object instanceof XSElementDeclaration))
            {
                continue;
            }

            XSElementDeclaration decl = (XSElementDeclaration) elements.item(i);
            if (!isComplexTypeCategory(decl.getTypeDefinition().getTypeCategory()))
            {
                continue;
            }            

            XSComplexTypeDefinition complexActualElement = (XSComplexTypeDefinition) decl.getTypeDefinition();
            String name = object.getName();
            String namespace = object.getNamespace();
            Stack<NamespaceNamePair> nameNamespaceStack = new Stack<NamespaceNamePair>();
            NamespaceNamePair nameNamespace = new NamespaceNamePair(namespace, name);
            nameNamespaceStack.add(nameNamespace);

            if (log.isDebugEnabled())
            {
                log.debug("Processing component " + namespace + " " + name);
            }

            SchemaElementComplex complexElement = process(name, namespace, complexActualElement, false, nameNamespaceStack, maxRecusiveDepth);

            if (log.isDebugEnabled())
            {
                log.debug("Adding component " + namespace + " " + name);
            }
            components.add(complexElement);
        }

        return new SchemaModel(components, namesspaceList);
    }

    private static boolean isComplexTypeCategory(short typeCategory)
    {
        return (typeCategory == XSTypeDefinition.COMPLEX_TYPE) || (typeCategory == JAVA5_COMPLEX_TYPE) || (typeCategory == JAVA6_COMPLEX_TYPE);
    }

    private static boolean isSimpleTypeCategory(short typeCategory)
    {
        return (typeCategory == XSTypeDefinition.SIMPLE_TYPE) || (typeCategory == JAVA5_SIMPLE_TYPE) || (typeCategory == JAVA6_SIMPLE_TYPE);
    }

    private static SchemaElementComplex process(String complexElementName, String complexElementNamespace, XSComplexTypeDefinition complexActualElement, boolean isArray, Stack<NamespaceNamePair> nameNamespaceStack, int maxRecursiveDepth)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Processing complex " + complexElementNamespace + " " + complexElementName + " stack " + nameNamespaceStack);
        }

        List<SchemaItemAttribute> attributes = new ArrayList<SchemaItemAttribute>();
        List<SchemaElementSimple> simpleElements = new ArrayList<SchemaElementSimple>();
        List<SchemaElementComplex> complexElements = new ArrayList<SchemaElementComplex>();

        Short optionalSimplyType = null;
        String optionalSimplyTypeName = null;
        if (complexActualElement.getSimpleType() != null) {
            XSSimpleTypeDecl simpleType = (XSSimpleTypeDecl) complexActualElement.getSimpleType();
            optionalSimplyType = simpleType.getPrimitiveKind();
            optionalSimplyTypeName = simpleType.getName();
        }

        SchemaElementComplex complexElement = new SchemaElementComplex(complexElementName, complexElementNamespace, attributes, complexElements, simpleElements, isArray, optionalSimplyType, optionalSimplyTypeName);

        // add attributes
        XSObjectList attrs = complexActualElement.getAttributeUses();
        for(int i = 0; i < attrs.getLength(); i++)
        {
            XSAttributeUse attr = (XSAttributeUse)attrs.item(i);
            String namespace = attr.getAttrDeclaration().getNamespace();
            String name = attr.getAttrDeclaration().getName();
            XSSimpleTypeDecl simpleType = (XSSimpleTypeDecl) attr.getAttrDeclaration().getTypeDefinition();
            attributes.add(new SchemaItemAttribute(namespace, name, simpleType.getPrimitiveKind(), simpleType.getName()));
        }

        if ((complexActualElement.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_ELEMENT) ||
            (complexActualElement.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED))
        {
            // has children
            XSParticle particle = complexActualElement.getParticle();
            if (particle.getTerm() instanceof XSModelGroup )
            {
                XSModelGroup group = (XSModelGroup)particle.getTerm();
                XSObjectList particles = group.getParticles();
                for (int i = 0; i < particles.getLength(); i++)
                {
                    XSParticle childParticle = (XSParticle)particles.item(i);

                    if (childParticle.getTerm() instanceof XSElementDeclaration)
                    {
                        XSElementDeclaration decl = (XSElementDeclaration) childParticle.getTerm();
                        boolean isArrayFlag = isArray(childParticle);

                        if (isSimpleTypeCategory(decl.getTypeDefinition().getTypeCategory())) {

                            XSSimpleTypeDecl simpleType = (XSSimpleTypeDecl) decl.getTypeDefinition();
                            Integer fractionDigits = getFractionRestriction(simpleType);
                            simpleElements.add(new SchemaElementSimple(decl.getName(), decl.getNamespace(), simpleType.getPrimitiveKind(), simpleType.getName(), isArrayFlag, fractionDigits));
                        }

                        if (isComplexTypeCategory(decl.getTypeDefinition().getTypeCategory()))
                        {
                            String name = decl.getName();
                            String namespace = decl.getNamespace();
                            NamespaceNamePair nameNamespace = new NamespaceNamePair(namespace, name);
                            nameNamespaceStack.add(nameNamespace);

                            // if the stack contains
                            if (maxRecursiveDepth != Integer.MAX_VALUE)
                            {
                                int containsCount = 0;
                                for (NamespaceNamePair pair : nameNamespaceStack)
                                {
                                    if (nameNamespace.equals(pair))
                                    {
                                        containsCount++;
                                    }
                                }

                                if (containsCount >= maxRecursiveDepth)
                                {
                                    continue;
                                }
                            }

                            complexActualElement = (XSComplexTypeDefinition) decl.getTypeDefinition();
                            SchemaElementComplex innerComplex = process(name, namespace, complexActualElement, isArrayFlag, nameNamespaceStack, maxRecursiveDepth);

                            nameNamespaceStack.pop();

                            if (log.isDebugEnabled())
                            {
                                log.debug("Adding complex " + complexElement);
                            }
                            complexElements.add(innerComplex);
                        }
                    }
                }
            }
        }

        return complexElement;
    }

    private static Integer getFractionRestriction(XSSimpleTypeDecl simpleType)
    {
        if ((simpleType.getDefinedFacets() & XSSimpleType.FACET_FRACTIONDIGITS) != 0){
            XSObjectList facets = simpleType.getFacets();
            Integer digits = null;
            for (int f = 0; f < facets.getLength(); f++)
            {
                XSObject item = facets.item(f);
                if (item instanceof XSFacet)
                {
                    XSFacet facet = (XSFacet) item;
                    if (facet.getFacetKind() == XSSimpleType.FACET_FRACTIONDIGITS)
                    {
                        try
                        {
                            digits = Integer.parseInt(facet.getLexicalFacetValue());
                        }
                        catch (RuntimeException ex)
                        {
                            log.warn("Error parsing fraction facet value '" + facet.getLexicalFacetValue() + "' : " + ex.getMessage(), ex);
                        }
                    }
                }
            }
            return digits;
        }
        return null;
    }

    private static boolean isArray(XSParticle particle)
    {
        return particle.getMaxOccursUnbounded() || (particle.getMaxOccurs() > 1); 
    }
    
    public static class LSInputImpl implements LSInput {

        private String stringData;

        public LSInputImpl(String stringData) {
            this.stringData = stringData;
        }

        @Override
        public Reader getCharacterStream() {
            return null;  
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
        }

        @Override
        public InputStream getByteStream() {
            return null;  
        }

        @Override
        public void setByteStream(InputStream byteStream) {
        }

        @Override
        public String getStringData() {
            return stringData;
        }

        @Override
        public void setStringData(String stringData) {
            this.stringData = stringData;
        }

        @Override
        public String getSystemId() {
            return null;  
        }

        @Override
        public void setSystemId(String systemId) {
        }

        @Override
        public String getPublicId() {
            return null;  
        }

        @Override
        public void setPublicId(String publicId) {
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public void setBaseURI(String baseURI) {
        }

        @Override
        public String getEncoding() {
            return null;  
        }

        @Override
        public void setEncoding(String encoding) {
        }

        @Override
        public boolean getCertifiedText() {
            return false;  
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
        }
    }
}

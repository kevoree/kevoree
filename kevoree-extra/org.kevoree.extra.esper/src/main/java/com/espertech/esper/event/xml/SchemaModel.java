package com.espertech.esper.event.xml;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Represents a XSD schema or other metadata for a class of XML documents. 
 */
public class SchemaModel implements Serializable
{
    private static final long serialVersionUID = -8949230627736368311L;
    private List<SchemaElementComplex> components;
    private List<String> namespaces;

    /**
     * Ctor.
     * @param components the top level components.
     * @param namespaces list of namespaces
     */
    public SchemaModel(List<SchemaElementComplex> components, List<String> namespaces)
    {
        this.components = components;
        this.namespaces = namespaces;
    }

    /**
     * Ctor.
     * @param component top level component
     * @param namespaces list of namespaces
     */
    public SchemaModel(SchemaElementComplex component, List<String> namespaces)
    {
        components = new ArrayList<SchemaElementComplex>(1);
        components.add(component);
        this.namespaces = namespaces;
    }

    /**
     * Returns top-level components.
     * @return components
     */
    public List<SchemaElementComplex> getComponents()
    {
        return components;
    }

    /**
     * Returns namespaces.
     * @return namespaces
     */
    public List<String> getNamespaces()
    {
        return namespaces;
    }
}

package com.espertech.esper.epl.spec;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.util.MetaDefItem;

import java.util.List;
import java.io.Serializable;

/**
 * Describes an annotation.
 */
public class AnnotationDesc implements MetaDefItem, Serializable
{
    private static final long serialVersionUID = 5474641956626793366L;
    private String name;

    // Map of Identifier and value={constant, array of value (Object[]), AnnotationDesc} (exclusive with value)
    private List<Pair<String, Object>> attributes;

    /**
     * Ctor.
     * @param name name of annotation
     * @param attributes are the attribute values
     */
    public AnnotationDesc(String name, List<Pair<String, Object>> attributes)
    {
        this.name = name;
        this.attributes = attributes;
    }

    /**
     * Returns annotation interface class name.
     * @return name of class, can be fully qualified
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns annotation attributes.
     * @return the attribute values
     */
    public List<Pair<String, Object>> getAttributes()
    {
        return attributes;
    }
}

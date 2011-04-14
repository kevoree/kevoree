package com.espertech.esper.core;

import com.espertech.esper.client.EventType;

/**
 * Selected properties for a create-window expression in the model-after syntax.
 */
public class NamedWindowSelectedProps
{
    private Class selectExpressionType;
    private String assignedName;
    private EventType fragmentType;

    /**
     * Ctor.
     * @param selectExpressionType expression result type
     * @param assignedName name of column
     * @param fragmentType null if not a fragment, or event type of fragment if one was selected
     */
    public NamedWindowSelectedProps(Class selectExpressionType, String assignedName, EventType fragmentType)
    {
        this.selectExpressionType = selectExpressionType;
        this.assignedName = assignedName;
        this.fragmentType = fragmentType;
    }

    /**
     * Returns the type of the expression result.
     * @return type
     */
    public Class getSelectExpressionType()
    {
        return selectExpressionType;
    }

    /**
     * Returns the assigned column name.
     * @return name
     */
    public String getAssignedName()
    {
        return assignedName;
    }

    /**
     * Returns the fragment type or null if not a fragment type.
     * @return type
     */
    public EventType getFragmentType()
    {
        return fragmentType;
    }
}

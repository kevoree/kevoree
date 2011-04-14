package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Atom in a specification for property evaluation.
 */
public class PropertyEvalAtom implements MetaDefItem, Serializable
{
    private final String propertyName;
    private final String optionalAsName;
    private final SelectClauseSpecRaw optionalSelectClause;
    private final ExprNode optionalWhereClause;
    private static final long serialVersionUID = -7123359550634592847L;

    /**
     * Ctor.
     * @param propertyName property name
     * @param optionalAsName column name assigned, if any
     * @param optionalSelectClause select clause, if any
     * @param optionalWhereClause where clause, if any
     */
    public PropertyEvalAtom(String propertyName, String optionalAsName, SelectClauseSpecRaw optionalSelectClause, ExprNode optionalWhereClause)
    {
        this.optionalAsName = optionalAsName;
        this.optionalSelectClause = optionalSelectClause;
        this.optionalWhereClause = optionalWhereClause;
        this.propertyName = propertyName;
    }

    /**
     * Returns the column name if assigned.
     * @return column name
     */
    public String getOptionalAsName()
    {
        return optionalAsName;
    }

    /**
     * Returns the select clause if specified.
     * @return select clause
     */
    public SelectClauseSpecRaw getOptionalSelectClause()
    {
        return optionalSelectClause;
    }

    /**
     * Returns the where clause, if specified.
     * @return filter expression
     */
    public ExprNode getOptionalWhereClause()
    {
        return optionalWhereClause;
    }

    /**
     * Returns the property name.
     * @return property name
     */
    public String getPropertyName()
    {
        return propertyName;
    }
}

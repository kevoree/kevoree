package com.espertech.esper.client.soda;

import java.io.Serializable;
import java.io.StringWriter;

/**
 * Represents a contained-event selection.
 */
public class ContainedEventSelect implements Serializable
{
    private static final long serialVersionUID = 0L;

    private SelectClause selectClause;
    private String propertyName;
    private String propertyAsName;
    private Expression whereClause;

    /**
     * Ctor.
     */
    public ContainedEventSelect() {
    }

    /**
     * Ctor.
     * @param propertyName property expression
     * @param propertyAsName optional alias
     * @param selectClause select clause, also optional
     * @param whereClause where clause, also optional
     */
    public ContainedEventSelect(String propertyName, String propertyAsName, SelectClause selectClause, Expression whereClause)
    {
        this.propertyName = propertyName;
        this.propertyAsName = propertyAsName;
        this.selectClause = selectClause;
        this.whereClause = whereClause;
    }

    /**
     * Returns the property alias.
     * @return alias
     */
    public String getPropertyAsName()
    {
        return propertyAsName;
    }

    /**
     * Sets the property alias
     * @param propertyAsName alias 
     */
    public void setPropertyAsName(String propertyAsName)
    {
        this.propertyAsName = propertyAsName;
    }

    /**
     * Returns the property expression.
     * @return property expression
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    /**
     * Sets the property expression.
     * @param propertyName expression
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    /**
     * Returns the select clause.
     * @return select clause
     */
    public SelectClause getSelectClause()
    {
        return selectClause;
    }

    /**
     * Sets the select clause.
     * @param selectClause select clause
     */
    public void setSelectClause(SelectClause selectClause)
    {
        this.selectClause = selectClause;
    }

    /**
     * Returns the where clause.
     * @return where clause
     */
    public Expression getWhereClause()
    {
        return whereClause;
    }

    /**
     * Sets the where clause.
     * @param whereClause where clause
     */
    public void setWhereClause(Expression whereClause)
    {
        this.whereClause = whereClause;
    }

    /**
     * Returns the EPL.
     * @param writer to write to
     */
    public void toEPL(StringWriter writer)
    {
        if (selectClause != null)
        {
            selectClause.toEPL(writer);
            writer.write("from ");
        }
        writer.write(propertyName);
        if (propertyAsName != null)
        {
            writer.write(" as ");
            writer.write(propertyAsName);
        }
        if (whereClause != null)
        {
            whereClause.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        }
    }
}

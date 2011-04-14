package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.type.RelationalOpEnum;
import com.espertech.esper.util.CoercionException;
import com.espertech.esper.util.JavaClassHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a lesser or greater then (</<=/>/>=) expression in a filter expression tree.
 */
public class ExprRelationalOpAllAnyNode extends ExprNode implements ExprEvaluator
{
    private final RelationalOpEnum relationalOpEnum;
    private final boolean isAll;
    private boolean hasCollectionOrArray;

    private transient RelationalOpEnum.Computer computer;
    private transient ExprEvaluator[] evaluators;

    private static final long serialVersionUID = -9212002972361997109L;

    /**
     * Ctor.
     * @param relationalOpEnum - type of compare, ie. lt, gt, le, ge
     * @param isAll - true if all, false for any
     */
    public ExprRelationalOpAllAnyNode(RelationalOpEnum relationalOpEnum, boolean isAll)
    {
        this.relationalOpEnum = relationalOpEnum;
        this.isAll = isAll;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    /**
     * Returns true for ALL, false for ANY.
     * @return indicator all or any
     */
    public boolean isAll()
    {
        return isAll;
    }

    /**
     * Returns the type of relational op used.
     * @return enum with relational op type
     */
    public RelationalOpEnum getRelationalOpEnum()
    {
        return relationalOpEnum;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        // Must have 2 child nodes
        if (this.getChildNodes().size() < 1)
        {
            throw new IllegalStateException("Group relational op node must have 1 or more child nodes");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        Class typeOne = JavaClassHelper.getBoxedType(evaluators[0].getType());

        // collections, array or map not supported
        if ((typeOne.isArray()) || (JavaClassHelper.isImplementsInterface(typeOne, Collection.class)) || (JavaClassHelper.isImplementsInterface(typeOne, Map.class)))
        {
            throw new ExprValidationException("Collection or array comparison is not allowed for the IN, ANY, SOME or ALL keywords");
        }

        List<Class> comparedTypes = new ArrayList<Class>();
        comparedTypes.add(typeOne);
        hasCollectionOrArray = false;
        for (int i = 0; i < this.getChildNodes().size() - 1; i++)
        {
            Class propType = evaluators[i + 1].getType();
            if (propType.isArray())
            {
                hasCollectionOrArray = true;
                if (propType.getComponentType() != Object.class)
                {
                    comparedTypes.add(propType.getComponentType());
                }
            }
            else if (JavaClassHelper.isImplementsInterface(propType, Collection.class))
            {
                hasCollectionOrArray = true;
            }
            else if (JavaClassHelper.isImplementsInterface(propType, Map.class))
            {
                hasCollectionOrArray = true;
            }
            else
            {
                comparedTypes.add(propType);
            }
        }

        // Determine common denominator type
        Class coercionType;
        try {
            coercionType = JavaClassHelper.getCommonCoercionType(comparedTypes.toArray(new Class[comparedTypes.size()]));
        }
        catch (CoercionException ex)
        {
            throw new ExprValidationException("Implicit conversion not allowed: " + ex.getMessage());
        }

        // Must be either numeric or string
        if (coercionType != String.class)
        {
            if (!JavaClassHelper.isNumeric(coercionType))
            {
                throw new ExprValidationException("Implicit conversion from datatype '" +
                        coercionType.getSimpleName() +
                        "' to numeric is not allowed");
            }
        }

        computer = relationalOpEnum.getComputer(coercionType, coercionType, coercionType);
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (this.getChildNodes().size() == 1)
        {
            return false;
        }

        Object valueLeft = evaluators[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        int len = this.getChildNodes().size() - 1;

        if (hasCollectionOrArray)
        {
            boolean hasNonNullRow = false;
            boolean hasRows = false;
            for (int i = 1; i <= len; i++)
            {
                Object valueRight = evaluators[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

                if (valueRight == null)
                {
                    continue;
                }

                if (valueRight instanceof Collection)
                {
                    Collection coll = (Collection) valueRight;
                    hasRows = true;
                    for (Object item : coll)
                    {
                        if (!(item instanceof Number))
                        {
                            if (isAll && item == null)
                            {
                                return null;
                            }
                            continue;
                        }
                        hasNonNullRow = true;
                        if (valueLeft != null)
                        {
                            if (isAll)
                            {
                                if (!computer.compare(valueLeft, item))
                                {
                                    return false;
                                }
                            }
                            else
                            {
                                if (computer.compare(valueLeft, item))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
                else if (valueRight instanceof Map)
                {
                    Map coll = (Map) valueRight;
                    hasRows = true;
                    for (Object item : coll.keySet())
                    {
                        if (!(item instanceof Number))
                        {
                            if (isAll && item == null)
                            {
                                return null;
                            }
                            continue;
                        }
                        hasNonNullRow = true;
                        if (valueLeft != null)
                        {
                            if (isAll)
                            {
                                if (!computer.compare(valueLeft, item))
                                {
                                    return false;
                                }
                            }
                            else
                            {
                                if (computer.compare(valueLeft, item))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
                else if (valueRight.getClass().isArray())
                {
                    hasRows = true;
                    int arrayLength = Array.getLength(valueRight);
                    for (int index = 0; index < arrayLength; index++)
                    {
                        Object item = Array.get(valueRight, index);
                        if (item == null)
                        {
                            if (isAll)
                            {
                                return null;
                            }
                            continue;
                        }
                        hasNonNullRow = true;
                        if (valueLeft != null)
                        {
                            if (isAll)
                            {
                                if (!computer.compare(valueLeft, item))
                                {
                                    return false;
                                }
                            }
                            else
                            {
                                if (computer.compare(valueLeft, item))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
                else if (!(valueRight instanceof Number))
                {
                    if (isAll)
                    {
                        return null;
                    }
                }
                else
                {
                    hasNonNullRow = true;
                    if (isAll)
                    {
                        if (!computer.compare(valueLeft, valueRight))
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if (computer.compare(valueLeft, valueRight))
                        {
                            return true;
                        }
                    }
                }
            }

            if (isAll)
            {
                if (!hasRows)
                {
                    return true;
                }
                if ((!hasNonNullRow) || (valueLeft == null))
                {
                    return null;
                }
                return true;
            }
            else
            {
                if (!hasRows)
                {
                    return false;
                }
                if ((!hasNonNullRow) || (valueLeft == null))
                {
                    return null;
                }
                return false;
            }
        }
        else
        {
            boolean hasNonNullRow = false;
            boolean hasRows = false;
            for (int i = 1; i <= len; i++)
            {
                Object valueRight = evaluators[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                hasRows = true;

                if (valueRight != null)
                {
                    hasNonNullRow = true;
                }
                else
                {
                    if (isAll)
                    {
                        return null;
                    }
                }

                if ((valueRight != null) && (valueLeft != null))
                {
                    if (isAll)
                    {
                        if (!computer.compare(valueLeft, valueRight))
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if (computer.compare(valueLeft, valueRight))
                        {
                            return true;
                        }
                    }
                }
            }

            if (isAll)
            {
                if (!hasRows)
                {
                    return true;
                }
                if ((!hasNonNullRow) || (valueLeft == null))
                {
                    return null;
                }
                return true;
            }
            else
            {
                if (!hasRows)
                {
                    return false;
                }
                if ((!hasNonNullRow) || (valueLeft == null))
                {
                    return null;
                }
                return false;
            }
        }
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(" ");
        buffer.append(relationalOpEnum.getExpressionText());
        buffer.append(" ");
        if (isAll)
        {
            buffer.append("all");
        }
        else
        {
            buffer.append("any");
        }

        buffer.append("(");
        String delimiter = "";
        
        for (int i = 0; i < this.getChildNodes().size()-1; i++)
        {
            buffer.append(delimiter);
            buffer.append(this.getChildNodes().get(i + 1).toExpressionString());
            delimiter = ",";
        }
        buffer.append(")");
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprRelationalOpAllAnyNode))
        {
            return false;
        }

        ExprRelationalOpAllAnyNode other = (ExprRelationalOpAllAnyNode) node;

        if ((other.relationalOpEnum != this.relationalOpEnum) ||
            (other.isAll != this.isAll))
        {
            return false;
        }

        return true;
    }
}

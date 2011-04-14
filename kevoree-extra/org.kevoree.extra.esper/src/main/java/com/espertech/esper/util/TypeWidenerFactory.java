package com.espertech.esper.util;

import com.espertech.esper.epl.expression.ExprValidationException;

/**
 * Factory for type widening.
 */
public class TypeWidenerFactory
{
    private static TypeWidenerStringToCharCoercer stringToCharCoercer = new TypeWidenerStringToCharCoercer();

    /**
     * Returns the widener.
     * @param columnName name of column
     * @param columnType type of column
     * @param writeablePropertyType property type
     * @param writeablePropertyName propery name
     * @return type widender
     * @throws ExprValidationException if type validation fails
     */
    public static TypeWidener getCheckPropertyAssignType(String columnName, Class columnType, Class writeablePropertyType, String writeablePropertyName)
            throws ExprValidationException
    {
        Class columnClassBoxed = JavaClassHelper.getBoxedType(columnType);
        Class targetClass = writeablePropertyType;
        Class targetClassBoxed = JavaClassHelper.getBoxedType(writeablePropertyType);

        if (columnType == null)
        {
            if (targetClass.isPrimitive())
            {
                String message = "Invalid assignment of column '" + columnName +
                        "' of null type to event property '" + writeablePropertyName +
                        "' typed as '" + writeablePropertyType.getName() +
                        "', nullable type mismatch";
                throw new ExprValidationException(message);
            }
        }
        else if (columnClassBoxed != targetClassBoxed)
        {
            if (columnClassBoxed == String.class && targetClassBoxed == Character.class)
            {
                return stringToCharCoercer;
            }
            else if (!JavaClassHelper.isAssignmentCompatible(columnClassBoxed, targetClassBoxed))
            {
                String writablePropName = writeablePropertyType.getName();
                if (writeablePropertyType.isArray()) {
                    writablePropName = writeablePropertyType.getComponentType().getName() + "[]";
                }

                String columnTypeName = columnType.getName();
                if (columnType.isArray()) {
                    columnTypeName = columnType.getComponentType().getName() + "[]";
                }

                String message = "Invalid assignment of column '" + columnName +
                        "' of type '" + columnTypeName +
                        "' to event property '" + writeablePropertyName +
                        "' typed as '" + writablePropName +
                        "', column and parameter types mismatch";
                throw new ExprValidationException(message);
            }

            if (JavaClassHelper.isNumeric(targetClass))
            {
                return new TypeWidenerBoxedNumeric(SimpleNumberCoercerFactory.getCoercer(columnClassBoxed, targetClassBoxed));
            }
        }

        return null;
    }
}

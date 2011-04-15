package com.espertech.esper.view.stat;

import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.ViewFieldEnum;

import java.util.Map;

public class StatViewAdditionalProps
{
    private final String[] additionalProps;
    private final ExprEvaluator[] additionalExpr;

    private StatViewAdditionalProps(String[] additionalProps, ExprEvaluator[] additionalExpr)
    {
        this.additionalProps = additionalProps;
        this.additionalExpr = additionalExpr;
    }

    public String[] getAdditionalProps()
    {
        return additionalProps;
    }

    public ExprEvaluator[] getAdditionalExpr()
    {
        return additionalExpr;
    }

    public static StatViewAdditionalProps make(ExprNode[] validated, int startIndex) {
        if (validated.length <= startIndex) {
            return null;
        }

        String[] additionalProps = new String[validated.length - startIndex];
        ExprEvaluator[] lastValueExpr = new ExprEvaluator[validated.length - startIndex];
        for (int i = startIndex; i < validated.length; i++) {
            additionalProps[i - startIndex] = validated[i].toExpressionString();
            lastValueExpr[i - startIndex] = validated[i].getExprEvaluator();
        }
        return new StatViewAdditionalProps(additionalProps, lastValueExpr);
    }

    public void addProperties(Map<String, Object> newDataMap, Object[] lastValuesEventNew)
    {
        if (lastValuesEventNew != null) {
            for (int i = 0; i < additionalProps.length; i++) {
                newDataMap.put(additionalProps[i], lastValuesEventNew[i]);
            }
        }
    }

    public static void addCheckDupProperties(Map<String, Object> target, StatViewAdditionalProps addProps, ViewFieldEnum... builtin) {
        if (addProps == null) {
            return;
        }

        for (int i = 0; i < addProps.getAdditionalProps().length; i++) {
            String name = addProps.getAdditionalProps()[i];
            for (int j = 0; j < builtin.length; j++) {
                if ((name.toLowerCase().equals(builtin[j].getName().toLowerCase()))) {
                    throw new IllegalArgumentException("The property by name '" + name + "' overlaps the property name that the view provides");
                }
            }
            target.put(name, addProps.getAdditionalExpr()[i].getType());
        }
    }    
}

package com.espertech.esper.epl.parse;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.core.EngineImportService;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.generated.EsperEPL2Ast;
import com.espertech.esper.epl.spec.AnnotationDesc;
import com.espertech.esper.util.JavaClassHelper;
import org.antlr.runtime.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Walker to annotation stuctures.
 */
public class ASTAnnotationHelper
{
    /**
     * Walk an annotation root name or child node (nested annotations).
     * @param node annotation walk node
     * @param engineImportService for engine imports
     * @return annotation descriptor
     * @throws ASTWalkException if the walk failed
     */
    public static AnnotationDesc walk(Tree node, EngineImportService engineImportService) throws ASTWalkException
    {
        String name = node.getChild(0).getText();
        List<Pair<String, Object>> values = new ArrayList<Pair<String, Object>>();
        Object value;

        for (int i = 1; i < node.getChildCount(); i++)
        {
            if (node.getChild(i).getType() == EsperEPL2Ast.ANNOTATION_VALUE)
            {
                Pair<String, Object> entry = walkValuePair(node.getChild(i), engineImportService);
                values.add(new Pair<String, Object>(entry.getFirst(), entry.getSecond()));
            }
            else if (node.getChild(i).getType() == EsperEPL2Ast.CLASS_IDENT)
            {
                Object enumValue = walkClassIdent(node.getChild(i), engineImportService);
                values.add(new Pair<String, Object>("value", enumValue));
            }
            else
            {
                value = walkValue(node.getChild(i));
                values.add(new Pair<String, Object>("value", value));
            }
        }

        return new AnnotationDesc(name, values);
    }

    private static Object walkValue(Tree child)
    {
        return ASTConstantHelper.parse(child);
    }

    private static Pair<String, Object> walkValuePair(Tree node, EngineImportService engineImportService)
    {
        String name = node.getChild(0).getText();
        if (node.getChild(1).getType() == EsperEPL2Ast.ANNOTATION_ARRAY)
        {
            Object[] values = walkArray(node.getChild(1));
            return new Pair<String, Object>(name, values);
        }
        if (node.getChild(1).getType() == EsperEPL2Ast.ANNOTATION)
        {
            AnnotationDesc anno = walk(node.getChild(1), engineImportService);
            return new Pair<String, Object>(name, anno);
        }
        else if (node.getChild(1).getType() == EsperEPL2Ast.CLASS_IDENT)
        {
            Object enumValue = walkClassIdent(node.getChild(1), engineImportService);
            return new Pair<String, Object>(name, enumValue);
        }
        else
        {
            Object constant = ASTConstantHelper.parse(node.getChild(1));
            return new Pair<String, Object>(name, constant);
        }
    }

    private static Object walkClassIdent(Tree child, EngineImportService engineImportService)
    {
        String enumValueText = child.getText();
        Object enumValue;
        try
        {
            enumValue = JavaClassHelper.resolveIdentAsEnumConst(enumValueText, null, engineImportService);
        }
        catch (ExprValidationException e)
        {
            throw new ASTWalkException("Annotation value '" + enumValueText + "' is not recognized as an enumeration value, please check imports or use a primitive or string type");
        }
        if (enumValue != null)
        {
            return enumValue;
        }
        throw new ASTWalkException("Annotation enumeration value '" + enumValueText + "' not recognized as an enumeration class, please check imports or type used");
    }

    private static Object[] walkArray(Tree node)
    {
        Object[] values = new Object[node.getChildCount()];
        for (int i = 0; i < node.getChildCount(); i++)
        {
            values[i] = walkValue(node.getChild(i));
        }
        return values;
    }
}

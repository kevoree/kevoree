package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.AbstractMap;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class VersionLineExpr {

    public static Map.Entry<String, String> interpret(final IAST<Type> lineNode, final Map<String, String> ctxVars)
            throws KevScriptException {
        String key = StringExpr.interpret(lineNode.getChildren().get(0));

        String value;
        IAST<Type> valueNode = lineNode.getChildren().get(1);

        switch (valueNode.getType()) {
            case RealStringNoNewLine:
                value = RealStringNoNewLineExpr.interpret(valueNode);
                break;

            case CtxVar:
                value = CtxVarExpr.interpret(valueNode, ctxVars);
                break;

            case Latest:
                value = "LATEST";
                break;

            default:
            case Release:
                value = "RELEASE";
                break;
        }

        return new AbstractMap.SimpleEntry<>(key, value);
    }
}

package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.KevScriptError;
import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class CtxVarExpr {

    public static String interpret(final IAST<Type> node, final Map<String, String> ctxVars) {
        IAST<Type> ctxVar = node.getChildren().get(0);
        String key = ctxVar.childrenAsString();
        String value = ctxVars.get(key);
        if (value == null) {
            throw new KevScriptError("Missing value for context variable %"+ctxVar.childrenAsString()+"%");
        }
        return value;
    }
}

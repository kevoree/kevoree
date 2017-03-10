package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class CtxVarExpr {

    public static String interpret(final IAST<Type> node, final Map<String, String> ctxVars) throws KevScriptException {
        IAST<Type> ctxVar = node.getChildren().get(0);
        String key = ctxVar.childrenAsString();
        String value = ctxVars.get(key);
        if (value == null) {
            throw new KevScriptException("Missing value for context variable %"+ctxVar.childrenAsString()+"%");
        }
        return value;
    }
}

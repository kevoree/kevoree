package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.util.ShortId;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class GenCtxVarExpr {

    public static String interpret(final IAST<Type> node, final Map<String, String> ctxVars) {
        IAST<Type> ctxVar = node.getChildren().get(0);
        String key = ctxVar.childrenAsString();
        String value = ctxVars.get(key);
        if (value == null) {
            value = ShortId.gen();
            ctxVars.put(key, value);
        }
        return value;
    }
}

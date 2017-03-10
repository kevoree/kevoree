package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.util.TypeFQN;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class TdefVersionExpr {

    public static String interpret(final IAST<Type> tdefVers, final Map<String, String> ctxVars)
            throws KevScriptException {
        String version = TypeFQN.Version.LATEST;
        // vNode => (Integer | Latest | CtxVar)
        IAST<Type> vNode = tdefVers.getChildren().get(0);
        switch (vNode.getType()) {
            case Integer:
                String intStr = "";
                for (IAST<Type> c : vNode.getChildren()) {
                    intStr += c.toString();
                }
                version = intStr;
                break;

            case Latest:
                version = TypeFQN.Version.LATEST;
                break;

            case CtxVar:
                version = CtxVarExpr.interpret(vNode, ctxVars);
                break;
        }
        return version;
    }
}

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
public class DuVersionExpr {

    public static String interpret(final IAST<Type> node, final Map<String, String> ctxVars) throws KevScriptException {
        String version = TypeFQN.Version.RELEASE;
        // vNode => (Release | Latest | CtxVar)
        IAST<Type> vNode = node.getChildren().get(0);
        switch (vNode.getType()) {
            case Release:
                version = TypeFQN.Version.RELEASE;
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

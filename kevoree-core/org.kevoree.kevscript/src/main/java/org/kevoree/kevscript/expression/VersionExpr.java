package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.util.TypeFQN;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class VersionExpr {

    public static TypeFQN.Version interpret(final IAST<Type> versNode, final Map<String, String> ctxVars) {
        TypeFQN.Version version = new TypeFQN.Version();
        version.tdef = TdefVersionExpr.interpret(versNode.getChildren().get(0), ctxVars);
        if (versNode.getChildren().size() == 1) {
            version.du = "RELEASE";
        } else {
            version.du = DuVersionExpr.interpret(versNode.getChildren().get(1), ctxVars);
        }

        return version;
    }
}

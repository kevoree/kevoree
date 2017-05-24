package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class VersionDeclExpr {

    public static Map<String, String> interpret(final IAST<Type> declNode, final Map<String, String> ctxVars)
            throws KevScriptException {
        Map<String, String> versions = new HashMap<>();
        if (!declNode.getChildren().isEmpty()) {
            versions.putAll(VersionLinesExpr.interpret(declNode.getChildren().get(0), ctxVars));
        }
        return versions;
    }
}

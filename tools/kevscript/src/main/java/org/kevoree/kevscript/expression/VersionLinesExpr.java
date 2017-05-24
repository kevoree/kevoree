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
public class VersionLinesExpr {

    public static Map<String, String> interpret(final IAST<Type> linesNode, final Map<String, String> ctxVars)
            throws KevScriptException {
        Map<String, String> versions = new HashMap<>();
        for (final IAST<Type> lineNode : linesNode.getChildren()) {
            Map.Entry<String, String> entry = VersionLineExpr.interpret(lineNode, ctxVars);
            versions.put(entry.getKey(), entry.getValue());
        }
        return versions;
    }
}

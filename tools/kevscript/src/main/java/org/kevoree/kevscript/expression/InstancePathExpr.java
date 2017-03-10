package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class InstancePathExpr {

    public static List<String> interpret(final IAST<Type> expr, final Map<String, String> ctxVars)
            throws KevScriptException {
        final List<String> instancePath = new ArrayList<>();
        for (IAST<Type> child : expr.getChildren()) {
            instancePath.add(InterpretExpr.interpret(child, ctxVars));
        }
        return instancePath;
    }
}

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
public class NameListExpr {

    public static List<List<String>> interpret(IAST<Type> expr, Map<String, String> ctxVars)
            throws KevScriptException {
        List<List<String>> nameList = new ArrayList<>();
        for (IAST<Type> instancePath : expr.getChildren()) {
            nameList.add(InstancePathExpr.interpret(instancePath, ctxVars));
        }
        return nameList;
    }
}

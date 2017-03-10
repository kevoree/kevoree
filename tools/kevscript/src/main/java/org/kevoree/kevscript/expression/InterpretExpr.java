package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class InterpretExpr {

    public static String interpret(IAST<Type> expr, Map<String, String> ctxVars) throws KevScriptException {
        switch (expr.getType()) {
            case Wildcard:
                return WildcardExpr.interpret(expr);

            case String:
                return StringExpr.interpret(expr);

            case RealString:
                return RealStringExpr.interpret(expr);

            case CtxVar:
                return CtxVarExpr.interpret(expr, ctxVars);

            case GenCtxVar:
                return GenCtxVarExpr.interpret(expr, ctxVars);

            default:
                return null;
        }
    }
}

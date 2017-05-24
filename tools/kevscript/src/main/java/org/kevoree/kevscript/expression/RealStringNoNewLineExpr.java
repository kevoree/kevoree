package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class RealStringNoNewLineExpr {

    public static String interpret(final IAST<Type> expr) {
        return expr.childrenAsString();
    }
}

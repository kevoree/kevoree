package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class WildcardExpr {

    public static String interpret(final IAST<Type> node) {
        return "*";
    }
}

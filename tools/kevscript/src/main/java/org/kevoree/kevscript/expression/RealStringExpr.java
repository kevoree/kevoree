package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class RealStringExpr {

    public static String interpret(final IAST<Type> expr) {
        StringBuilder builder = new StringBuilder();
        for (IAST<Type> child : expr.getChildren()) {
            switch (child.getType()) {
                case SingleQuoteLine:
                case DoubleQuoteLine:
                    builder.append(child.childrenAsString());
                    break;

                case NewLine:
                    builder.append('\n');
                    break;
            }
        }
        return builder.toString();
    }
}

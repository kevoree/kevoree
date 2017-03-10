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
public class TypeDefExpr {

    public static TypeFQN interpret(final IAST<Type> node, final Map<String, String> ctxVars)
            throws KevScriptException {
        final String fqn = parseTypeFQN(node.getChildren().get(0));
        String namespace;
        String name;
        final int dotIndex = fqn.lastIndexOf(".");
        if (dotIndex == -1) {
            namespace = "kevoree";
            name = fqn;
        } else {
            namespace = fqn.substring(0, dotIndex);
            name = fqn.substring(dotIndex + 1);
        }
        TypeFQN.Builder fqnBuilder = new TypeFQN.Builder()
                .namespace(namespace)
                .name(name);
        if (node.getChildren().size() != 1) {
            // version is not default
            fqnBuilder.version(VersionExpr.interpret(node.getChildren().get(1), ctxVars));
        }
        return fqnBuilder.build();
    }

    private static String parseTypeFQN(final IAST<Type> node) {
        String fqn = "";
        for (IAST<Type> child: node.getChildren()) {
            if (child.getChildren().isEmpty()) {
                fqn += child.toString();
            } else {
                fqn += child.childrenAsString();
            }
        }
        return fqn;
    }
}

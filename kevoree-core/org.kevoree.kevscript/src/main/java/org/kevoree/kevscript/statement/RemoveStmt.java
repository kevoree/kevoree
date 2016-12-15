package org.kevoree.kevscript.statement;

import org.kevoree.*;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.util.InstanceResolver;
import org.waxeye.ast.IAST;

import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class RemoveStmt {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars) throws Exception {
        final List<Instance> toRemove = InstanceResolver.resolve(stmt.getChildren().get(0), model, ctxVars);
        for (final Instance toDrop : toRemove) {
            if (toDrop instanceof ComponentInstance) {
                final ComponentInstance ci = (ComponentInstance) toDrop;
                for (final Port p : ci.getProvided()) {
                    for (final MBinding mb : p.getBindings()) {
                        mb.delete();
                    }
                }
                for (final Port p : ci.getRequired()) {
                    for (final MBinding mb : p.getBindings()) {
                        mb.delete();
                    }
                }
            }
            toDrop.delete();
        }
    }
}

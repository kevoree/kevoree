package org.kevoree.kevscript.statement;

import org.kevoree.*;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.util.InstanceResolver;
import org.kevoree.kevscript.util.PortResolver;
import org.waxeye.ast.IAST;

import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class DelBinding {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars) throws Exception {
        final List<Instance> channelsInstance2 = InstanceResolver.resolve(stmt.getChildren().get(1), model, ctxVars);
        final List<Port> ports = PortResolver.resolve(model, stmt.getChildren().get(0));

        for (final Instance instance : channelsInstance2) {
            final Channel channel = (Channel) instance;
            MBinding toDrop = null;
            for (final MBinding mb : channel.getBindings()) {
                for (final Port p : ports) {
                    if (mb.getPort().equals(p)) {
                        toDrop = mb;
                    }
                }

            }
            if (toDrop != null) {
                toDrop.delete();
            }
        }
    }
}

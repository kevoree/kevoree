package org.kevoree.kevscript.statement;

import org.kevoree.*;
import org.kevoree.factory.DefaultKevoreeFactory;
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
public class AddBinding {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars) throws Exception {
        DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
        final List<Instance> channelsInstance = InstanceResolver.resolve(stmt.getChildren().get(1), model, ctxVars);
        for (final Instance instance : channelsInstance) {
            final Channel channel = (Channel) instance;
            final List<Port> ports = PortResolver.resolve(model, stmt.getChildren().get(0));
            for (final Port p : ports) {
                final MBinding mb = factory.createMBinding();
                mb.setPort(p);
                mb.setHub(channel);
                model.addMBindings(mb);
            }
        }
    }
}

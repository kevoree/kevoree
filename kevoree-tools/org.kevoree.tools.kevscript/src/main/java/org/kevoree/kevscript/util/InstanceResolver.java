package org.kevoree.kevscript.util;

import jet.runtime.typeinfo.JetValueParameter;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.kevscript.Type;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.util.ModelVisitor;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 16:43
 */
public class InstanceResolver {

    /* Not the most efficient, but easy to maintain... */

    public static List<Instance> resolve(ContainerRoot model, IAST<Type> node) {
        final List<Instance> resolved = new ArrayList<Instance>();
        if (node.getType().equals(Type.NameList)) {
            for (IAST<Type> child : node.getChildren()) {
                resolved.addAll(resolve(model, child));
            }
        } else {
            final String name = node.childrenAsString();
            model.visit(new ModelVisitor() {
                @Override
                public void visit(@JetValueParameter(name = "elem") KMFContainer kmfContainer, @JetValueParameter(name = "refNameInParent") String s, @JetValueParameter(name = "parent") KMFContainer kmfContainer2) {
                    if (kmfContainer instanceof Instance) {
                        Instance elem = (Instance) kmfContainer;
                        if (elem.getName().equals(name)) {
                            resolved.add(elem);
                        } else {
                            if (name.contains("*")) {
                                if (elem.getName().matches(name.replace("*", ".*"))) {
                                    resolved.add(elem);
                                }
                            }
                        }
                    }
                }
            }, true, true, false);

        }
        return resolved;
    }

}

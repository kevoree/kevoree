package org.kevoree.kevscript.util;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.kevscript.Type;
import org.kevoree.modeling.api.KMFContainer;
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



    public static List<Instance> resolve(ContainerRoot model, IAST<Type> node) throws Exception {
        final List<Instance> resolved = new ArrayList<Instance>();
        if (node.getType().equals(Type.InstancePath) && node.getChildren().size() < 3) {
            if (node.getChildren().size() == 2) {
                //Component case
                String nodeName = node.getChildren().get(0).childrenAsString();
                String childName = node.getChildren().get(1).childrenAsString();
                List<KMFContainer> parentNodes = model.select("nodes[" + nodeName + "]");
                if (parentNodes.isEmpty()) {
                    throw new Exception("No nodes found with name : " + nodeName);
                }
                for (Object loopObj : parentNodes) {
                    ContainerNode parentNode = (ContainerNode) loopObj;
                    List<KMFContainer> comps = parentNode.select("components[" + childName + "]");
                    List<KMFContainer> hosts = parentNode.select("hosts[" + childName + "]");
                    // append components & hosted nodes
                    comps.addAll(hosts);
                    if (comps.isEmpty()) {
                        throw new Exception("No component/node found with name : " + childName + " in node " + parentNode.getName());
                    }
                    for (Object ci : comps) {
                        resolved.add((Instance) ci);
                    }
                }
            } else {
                //group or channel
                String instanceName = node.getChildren().get(0).childrenAsString();
                List<KMFContainer> instancefounds = model.select("groups[" + instanceName + "]");
                if (instancefounds.isEmpty()) {
                    instancefounds = model.select("hubs[" + instanceName + "]");
                }
                if (instancefounds.isEmpty()) {
                    instancefounds = model.select("nodes[" + instanceName + "]");
                }
                if (instancefounds.isEmpty()) {
                    throw new Exception("No group or channel found for name " + instanceName);
                } else {
                    for (Object sub : instancefounds) {
                        resolved.add((Instance) sub);
                    }
                }
            }


        } else {
            if (node.getType().equals(Type.NameList)) {
                for (IAST<Type> child : node.getChildren()) {
                    resolved.addAll(resolve(model, child));
                }
            } else {
                throw new Exception("Bad name to resolve instances : " + node.toString());
            }
        }

         /*
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
                        if (isMatch(elem, name)) {
                            resolved.add(elem);
                        }
                    }
                }
            }, true, true, false);
        }
        */
        if (resolved.isEmpty()) {
            throw new Exception("No instance is resolved from : " + node.toString());
        }

        return resolved;
    }

    private static boolean isMatch(Instance elem, String name) {
        if (elem.getName().equals(name)) {
            return true;
        } else {
            if (name.contains("*")) {
                if (elem.getName().matches(name.replace("*", ".*"))) {
                    return true;
                }
            }
        }
        return false;
    }

}

package org.kevoree.kevscript.util;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Port;
import org.kevoree.kevscript.KevScriptError;
import org.kevoree.kevscript.Type;
import org.kevoree.pmodeling.api.KMFContainer;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 16:43
 */
public class PortResolver {

    public static List<Port> resolve(ContainerRoot model, IAST<Type> node) throws Exception {
        final List<Port> resolved = new ArrayList<Port>();
        if (node.getType().equals(Type.InstancePath) && node.getChildren().size() == 3) {
            //Component case
            String nodeName = node.getChildren().get(0).childrenAsString();
            String componentName = node.getChildren().get(1).childrenAsString();
            String portName = node.getChildren().get(2).childrenAsString();
            List<KMFContainer> parentNodes = model.select("nodes[" + nodeName + "]");
            if (parentNodes.isEmpty()){
                throw new KevScriptError("Unable to find node " + componentName + " in model");
            }
            for (Object loopObj : parentNodes) {
                ContainerNode parentNode = (ContainerNode) loopObj;
                List<KMFContainer> cis = parentNode.select("components[" + componentName + "]");
                if (cis.isEmpty()){
                    throw new KevScriptError("Unable to find component \"" + componentName + "\" in node \"" + parentNode.getName() + "\"");
                }
                for (Object ci : cis) {
                    ComponentInstance cinstance = (ComponentInstance) ci;
                    for (Port p : cinstance.getProvided()) {
                        if (p.getPortTypeRef().getName().equals(portName)) {
                            resolved.add(p);
                        }
                    }
                    for (Port p : cinstance.getRequired()) {
                        if (p.getPortTypeRef().getName().equals(portName)) {
                            resolved.add(p);
                        }
                    }
                }
            }
        } else {
            throw new KevScriptError("Bad name to resolve ports : " + node.toString());
        }
        if (resolved.isEmpty()) {
            throw new KevScriptError("No port resolved from : " + node.toString());
        }

        return resolved;
    }
}

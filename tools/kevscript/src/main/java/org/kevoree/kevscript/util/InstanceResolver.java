package org.kevoree.kevscript.util;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.expression.*;
import org.kevoree.modeling.api.KMFContainer;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class InstanceResolver {

    public static List<Instance> resolve(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars)
            throws KevScriptException {
        final List<Instance> resolved = new ArrayList<>();
        if (stmt.getType().equals(Type.InstancePath) && stmt.getChildren().size() < 3) {
            if (stmt.getChildren().size() == 2) {
                //Component case
                String nodeName = InterpretExpr.interpret(stmt.getChildren().get(0), ctxVars);
                String childName = InterpretExpr.interpret(stmt.getChildren().get(1), ctxVars);
                List<KMFContainer> parentNodes = model.select("nodes[" + nodeName + "]");
                if (parentNodes.isEmpty()) {
                    throw new KevScriptException("No nodes found with name : " + nodeName);
                }
                for (Object loopObj : parentNodes) {
                    ContainerNode parentNode = (ContainerNode) loopObj;
                    List<KMFContainer> comps = parentNode.select("components[" + childName + "]");
                    List<KMFContainer> hosts = parentNode.select("hosts[" + childName + "]");
                    // append components & hosted nodes
                    comps.addAll(hosts);
                    if (comps.isEmpty()) {
                        throw new KevScriptException("No component/node found with name : " + childName + " in node " + parentNode.getName());
                    }
                    for (Object ci : comps) {
                        resolved.add((Instance) ci);
                    }
                }
            } else {
                //group or channel
                String instanceName = InterpretExpr.interpret(stmt.getChildren().get(0), ctxVars);
                List<KMFContainer> instancefounds = model.select("groups[" + instanceName + "]");
                if (instancefounds.isEmpty()) {
                    instancefounds = model.select("hubs[" + instanceName + "]");
                }
                if (instancefounds.isEmpty()) {
                    instancefounds = model.select("nodes[" + instanceName + "]");
                }
                if (instancefounds.isEmpty()) {
                    throw new KevScriptException("No group or channel found for name " + instanceName);
                } else {
                    for (Object sub : instancefounds) {
                        resolved.add((Instance) sub);
                    }
                }
            }
        } else {
            if (stmt.getType().equals(Type.NameList)) {
                for (IAST<Type> child : stmt.getChildren()) {
                    resolved.addAll(resolve(child, model, ctxVars));
                }
            } else {
                throw new KevScriptException("Bad name to resolve instances : " + stmt.toString());
            }
        }

        if (resolved.isEmpty()) {
            throw new KevScriptException("No instance found named \"" + stmt.toString() + "\"");
        }

        return resolved;
    }
}

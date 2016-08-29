package org.kevoree.kevscript.util;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.kevscript.KevScriptError;
import org.kevoree.kevscript.Type;
import org.kevoree.pmodeling.api.KMFContainer;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 16:43
 */
public class InstanceResolver {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static List<Instance> resolve(ContainerRoot model, IAST<Type> stmt, Map<String, String> ctxVars) throws Exception {
        final List<Instance> resolved = new ArrayList<Instance>();
        if (stmt.getType().equals(Type.InstancePath) && stmt.getChildren().size() < 3) {
            if (stmt.getChildren().size() == 2) {
                //Component case
                String nodeName = interpret(stmt.getChildren().get(0), ctxVars);
                String childName = interpret(stmt.getChildren().get(1), ctxVars);
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
                String instanceName = interpret(stmt.getChildren().get(0), ctxVars);
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
            if (stmt.getType().equals(Type.NameList)) {
                for (IAST<Type> child : stmt.getChildren()) {
                    resolved.addAll(resolve(model, child, ctxVars));
                }
            } else {
                throw new Exception("Bad name to resolve instances : " + stmt.toString());
            }
        }

        if (resolved.isEmpty()) {
            throw new Exception("No instance found named \"" + stmt.toString() + "\"");
        }

        return resolved;
    }

    public static String interpret(IAST<Type> stmt, Map<String, String> ctxVars) {
        switch (stmt.getType()) {
            case Wildcard:
                return "*";

            case String:
                return stmt.childrenAsString();

            case CtxVar:
                String ctxVarVal = ctxVars.get(stmt.getChildren().get(0).childrenAsString());
                if (ctxVarVal == null) {
                    throw new KevScriptError("Unable to find a value for context variable \"%"+stmt.getChildren().get(0).childrenAsString()+"%\"");
                }
                return ctxVarVal;

            case GenCtxVar:
                String key = stmt.getChildren().get(0).childrenAsString();
                String value = ctxVars.get(key);
                if (value == null) {
                    value = shortId();
                    ctxVars.put(key, value);
                }
                return value;

            default:
                return null;
        }
    }

    private static String shortId() {
        final StringBuilder builder = new StringBuilder();
        final Random random = new Random();
        for (int i = 0; i < 6; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }
}

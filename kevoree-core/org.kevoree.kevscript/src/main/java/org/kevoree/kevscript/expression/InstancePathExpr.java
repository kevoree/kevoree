package org.kevoree.kevscript.expression;

import org.kevoree.kevscript.Type;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class InstancePathExpr {

    public static List<String> interpret(final IAST<Type> expr, final Map<String, String> ctxVars)
            throws Exception {
        final List<String> instancePath = new ArrayList<>();
        for (IAST<Type> child : expr.getChildren()) {
            instancePath.add(InterpretExpr.interpret(child, ctxVars));
        }
//
//
//        if (expr.getChildren().size() == 2) {
//            //Component case
//            String nodeName = InterpretExpr.interpret(expr.getChildren().get(0), ctxVars);
//            String childName = InterpretExpr.interpret(expr.getChildren().get(1), ctxVars);
//            List<KMFContainer> parentNodes = model.select("nodes[" + nodeName + "]");
//            if (parentNodes.isEmpty()) {
//                throw new KevScriptError("No nodes found with name : " + nodeName);
//            }
//            for (Object loopObj : parentNodes) {
//                ContainerNode parentNode = (ContainerNode) loopObj;
//                List<KMFContainer> comps = parentNode.select("components[" + childName + "]");
//                List<KMFContainer> hosts = parentNode.select("hosts[" + childName + "]");
//                // append components & hosted nodes
//                comps.addAll(hosts);
//                if (comps.isEmpty()) {
//                    throw new KevScriptError("No component/node found with name : " + childName + " in node " + parentNode.getName());
//                }
//                for (Object ci : comps) {
//                    instancePath.add((Instance) ci);
//                }
//            }
//        } else {
//            //group or channel
//            String instanceName = InterpretExpr.interpret(expr.getChildren().get(0), ctxVars);
//            List<KMFContainer> instancefounds = model.select("groups[" + instanceName + "]");
//            if (instancefounds.isEmpty()) {
//                instancefounds = model.select("hubs[" + instanceName + "]");
//            }
//            if (instancefounds.isEmpty()) {
//                instancefounds = model.select("nodes[" + instanceName + "]");
//            }
//            if (instancefounds.isEmpty()) {
//                throw new Exception("No group or channel found for name " + instanceName);
//            } else {
//                for (Object sub : instancefounds) {
//                    instancePath.add((Instance) sub);
//                }
//            }
//        }

        return instancePath;
    }
}

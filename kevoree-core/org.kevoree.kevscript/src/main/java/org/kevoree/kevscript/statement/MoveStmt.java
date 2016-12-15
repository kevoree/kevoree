package org.kevoree.kevscript.statement;

import org.jetbrains.annotations.NotNull;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.kevscript.KevScriptError;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.expression.InstancePathExpr;
import org.kevoree.kevscript.expression.NameListExpr;
import org.waxeye.ast.IAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class MoveStmt {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars) throws Exception {
        List<List<String>> instancesToMove = NameListExpr.interpret(stmt.getChildren().get(0), ctxVars);
        List<String> targetNodes = InstancePathExpr.interpret(stmt.getChildren().get(1), ctxVars);

        List<ContainerNode> targets;
        if (targetNodes.size() == 1) {
            targets = getContainerNodes(model, targetNodes);
        } else {
            throw new KevScriptError("Move target path is invalid (" + targetNodes.toString() + ")");
        }

        for (List<String> instancePath : instancesToMove) {
            if (instancePath.size() == 1) {
                if (instancePath.get(0).equals("*")) {
                    throw new KevScriptError("Wilcard \"*\" cannot be used for nodes. Move failed");
                } else {
                    // specific node instance
                    ContainerNode node = model.findNodesByID(instancePath.get(0));
                    if (node != null) {
                        for (ContainerNode targetNode : targets) {
                            apply(node, targetNode);
                        }
                    } else {
                        throw new KevScriptError("Unable to move node instance \""+ instancePath.get(0)+"\". Instance does not exist");
                    }
                }
            } else if (instancePath.size() == 2) {
                List<ContainerNode> hostNodes = getContainerNodes(model, instancePath);

                List<ComponentInstance> components = new ArrayList<>();
                if (instancePath.get(1).equals("*")) {
                    // all components
                    for (ContainerNode host : hostNodes) {
                        components.addAll(host.getComponents());
                    }
                } else {
                    // specific component
                    for (ContainerNode host : hostNodes) {
                        ComponentInstance comp = host.findComponentsByID(instancePath.get(1));
                        if (comp != null) {
                            components.add(comp);
                        }
                    }
                }

                for (ContainerNode targetNode : targets) {
                    for (ComponentInstance comp : components) {
                        apply(comp, targetNode);
                    }
                }
            } else {
                throw new KevScriptError("\""+instancePath.toString()+"\" is not a valid move path for an instance");
            }
        }
    }

    @NotNull
    private static List<ContainerNode> getContainerNodes(ContainerRoot model, List<String> targetNodes) {
        List<ContainerNode> nodes = new ArrayList<>();
        if (targetNodes.get(0).equals("*")) {
            // select all nodes in the model
            nodes.addAll(model.getNodes());
        } else {
            // specific node
            ContainerNode node = model.findNodesByID(targetNodes.get(0));
            if (node != null) {
                nodes.add(node);
            } else {
                throw new KevScriptError("Unable to find node instance \"" + targetNodes.get(0) + "\". Move failed");
            }
        }
        return nodes;
    }

    private static void apply(Instance leftH, ContainerNode targetNode) {
        if (leftH instanceof ComponentInstance) {
            targetNode.addComponents((ComponentInstance) leftH);
        } else {
            if (leftH instanceof ContainerNode) {
                targetNode.addHosts((ContainerNode) leftH);
            } else {
                throw new KevScriptError("\""+leftH.getName()+"\" is not a node instance nor a component. Move failed");
            }
        }
    }
}

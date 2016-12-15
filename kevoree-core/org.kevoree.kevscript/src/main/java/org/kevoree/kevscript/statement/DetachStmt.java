package org.kevoree.kevscript.statement;

import org.kevoree.*;
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
public class DetachStmt {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars) throws Exception {
        List<List<String>> instancesToAttach = NameListExpr.interpret(stmt.getChildren().get(0), ctxVars);
        List<String> groupList = InstancePathExpr.interpret(stmt.getChildren().get(1), ctxVars);

        List<Group> groups = new ArrayList<>();
        if (groupList.size() == 1) {
            if (groupList.get(0).equals("*")) {
                // target all groups
                groups.addAll(model.getGroups());
            } else {
                // try to find the group
                Group group = model.findGroupsByID(groupList.get(0));
                if (group != null) {
                    groups.add(group);
                } else {
                    throw new KevScriptError("Unable to find group instance \""+groupList.get(0)+"\". Detach failed");
                }
            }
        } else {
            throw new KevScriptError("Detach target path is invalid ("+groupList.toString()+")");
        }

        for (List<String> instancePath : instancesToAttach) {
            if (instancePath.size() == 1) {
                if (instancePath.get(0).equals("*")) {
                    // attach all nodes to target groups
                    for (ContainerNode node : model.getNodes()) {
                        for (Group group : groups) {
                            apply(node, group);
                        }
                    }
                } else {
                    // try to find node in model
                    ContainerNode node = model.findNodesByID(instancePath.get(0));
                    if (node != null) {
                        for (Group group : groups) {
                            apply(node, group);
                        }
                    } else {
                        // unable to find node in model
                        throw new KevScriptError("Unable to detach node instance \""+instancePath.get(0)+"\". Instance does not exist");
                    }
                }
            } else {
                throw new KevScriptError("\""+instancePath.toString()+"\" is not a valid detach path for a node");
            }
        }
    }

    private static void apply(ContainerNode node, Group group) {
        group.removeSubNodes(node);
        node.removeGroups(group);

        FragmentDictionary fDic = group.findFragmentDictionaryByID(node.getName());
        if (fDic != null) {
            group.removeFragmentDictionary(fDic);
        }
    }
}

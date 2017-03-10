package org.kevoree.kevscript.statement;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NetworkInfo;
import org.kevoree.Value;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.expression.CtxVarExpr;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class NetworkStmt {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars)
            throws KevScriptException {
        final IAST<Type> leftHandNetwork = stmt.getChildren().get(0);
        if (leftHandNetwork.getChildren().size() != 3) {
            throw new KevScriptException("Network must be: network nodeName.propertyType.interfaceName IP");
        } else {
            final String nodeName = leftHandNetwork.getChildren().get(0).childrenAsString();
            final String propType = leftHandNetwork.getChildren().get(1).childrenAsString();
            final String interfaceName = leftHandNetwork.getChildren().get(2).childrenAsString();
            final ContainerNode networkTargetNode = model.findNodesByID(nodeName);
            if (networkTargetNode == null) {
                throw new KevScriptException("Node not found for name " + nodeName);
            }
            NetworkInfo info = networkTargetNode.findNetworkInformationByID(propType);
            DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
            if (info == null) {
                info = factory.createNetworkInfo();
                info.setName(propType);
                networkTargetNode.addNetworkInformation(info);
            }
            Value netprop = info.findValuesByID(interfaceName);
            if (netprop == null) {
                netprop = factory.createValue();
                netprop.setName(interfaceName);
                info.addValues(netprop);
            }

            IAST<Type> valuePart = stmt.getChildren().get(1);
            if (valuePart.getType().equals(Type.CtxVar)) {
                netprop.setValue(CtxVarExpr.interpret(valuePart, ctxVars));
            } else {
                // string
                netprop.setValue(valuePart.childrenAsString());
            }
        }
    }
}

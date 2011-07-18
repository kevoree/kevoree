package org.kevoree.library.reasoner.ecj.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kevoree.ComponentInstance;
import org.kevoree.ComponentType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class MoveComponentDPA implements DPA {

    public final static String componentName = "component";
    public final static String sourceNodeName = "node1";
    public final static String targetNodeName = "node2";

    public final static String templateScript = "tblock { \n" +
            " moveComponent ${component}@${node1} => ${node2}\n" +
            " }";
    private static int increment = 0;

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (ContainerNode sourceNode : myModel.getNodes()) {
            for (ContainerNode targetNode : myModel.getNodes()) {
                for (ComponentInstance myComponent : sourceNode.getComponents()) {
                    if (!sourceNode.equals(targetNode)) {
                        Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                        myMap.put(this.componentName, (NamedElement) myComponent);
                        myMap.put(this.sourceNodeName, (NamedElement) sourceNode);
                        myMap.put(this.targetNodeName, (NamedElement) targetNode);
                        results.add(myMap);
                    }
                }
            }
        }
        return results;
    }

    public String getScript(Map<String, NamedElement> myMap) {
        String script = templateScript;
        for (String name : myMap.keySet()) {
            String replacedString = "${" + name + "}";
            script = script.replace(replacedString, myMap.get(name).getName());
        }
        return script;
    }

}

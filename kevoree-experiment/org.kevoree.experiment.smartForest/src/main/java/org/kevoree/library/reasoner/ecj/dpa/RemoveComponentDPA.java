package org.kevoree.library.reasoner.ecj.dpa;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.ast.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveComponentDPA implements DPA {

    public final static String componentName = "component";
    public final static String nodeName = "node";

    public final static String templateScript = "tblock { \n" +
            "  removeComponent ${component}@${node}\n" +
            "}";

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (ContainerNode containerNode : myModel.getNodes()) {
            for (ComponentInstance componentInstance : containerNode.getComponents()) {
                Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                myMap.put(this.componentName,
                        (NamedElement) componentInstance);
                myMap.put(this.nodeName,
                        (NamedElement) containerNode);
                results.add(myMap);
            }
        }
        return results;
    }

    public String getScript(Map<String, NamedElement> myMap) {
        String script = templateScript;
        for (String name : myMap.keySet()) {
            script = script.replace("${" + name + "}", myMap.get(name).getName());
        }
        return script;
    }

    @Override
    public Script getASTScript(Map<String, NamedElement> stringNamedElementMap) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}

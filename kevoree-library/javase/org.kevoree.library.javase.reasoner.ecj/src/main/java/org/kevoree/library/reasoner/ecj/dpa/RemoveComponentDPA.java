package org.kevoree.library.reasoner.ecj.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class RemoveComponentDPA implements DPA {

    public final static String componentName = "component";
    public final static String nodeName = "node";

    public final static String scriptPath = "target/classes/removeComponent.kevs";

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
        String script = ParserUtil.loadFile(scriptPath);
        for (String name : myMap.keySet()) {
            script = script.replace("${" + name + "}", myMap.get(name).getName());
        }
        return script;
    }

}

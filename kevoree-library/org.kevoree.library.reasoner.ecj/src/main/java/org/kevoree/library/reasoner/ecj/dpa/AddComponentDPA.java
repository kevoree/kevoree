package org.kevoree.library.reasoner.ecj.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kevoree.ComponentType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.TypeDefinition;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class AddComponentDPA implements DPA {

    public final static String componentName = "component";
    public final static String typeDefinition = "type";
    public final static String nodeName = "node";

    public final static String scriptPath = "target/classes/addComponent.kevs";
    private static int increment = 0;

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (ContainerNode containerNode : myModel.getNodes()) {
            for (TypeDefinition typeDef : myModel.getTypeDefinitions()) {
                if (typeDef instanceof ComponentType) {
                    Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                    myMap.put(this.typeDefinition, (NamedElement) typeDef);
                    myMap.put(this.nodeName, (NamedElement) containerNode);
                    results.add(myMap);
                }
            }
        }
        return results;
    }

    public String getScript(Map<String, NamedElement> myMap) {
        String script = ParserUtil.loadFile(scriptPath);
        for (String name : myMap.keySet()) {
            String replacedString = "${" + name + "}";
            script = script.replace(replacedString, myMap.get(name).getName());
        }
        // the next line should be removed if we enable script without specifying componentName
        script = script.replace("${" + this.componentName + "}", myMap.get(this.typeDefinition).getName()+increment++ );
        return script;
    }

}

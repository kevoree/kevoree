package org.kevoree.library.reasoner.ecj.dpa;

import org.eclipse.emf.common.util.EList;
import org.kevoree.*;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddForestMonitoringComponentDPA implements DPA {

    public final static String componentName = "component";
    public final static String typeDefinition = "type";
    public final static String nodeName = "node";

   public final static String templateScript = "tblock { \n" +
           "  addComponent ${component}@${node} : ${type}\n" +
           "}";
    private static int increment = 0;
    public static HashMap<String,NamedElement> componentTypes;


    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        if (componentTypes == null){
            componentTypes = new HashMap<String, NamedElement>();
            for (TypeDefinition typeDef : myModel.getTypeDefinitions()) {
                if (typeDef instanceof ComponentType) {
                    componentTypes.put(((NamedElement)typeDef).getName(), (NamedElement)typeDef);
                }
            }
        }
        List<Map<String, NamedElement>> results = new ArrayList();
        for (ContainerNode containerNode : myModel.getNodes()) {
            boolean existTempSensor = false, existSmokeSensor = false, existHumiditySensor = false;
            for (ComponentInstance myInstance : containerNode.getComponents()){
                if (((NamedElement)myInstance.getTypeDefinition()).getName().equalsIgnoreCase("TempSensor")){
                    existTempSensor = true;
                }
                if (((NamedElement)myInstance.getTypeDefinition()).getName().equalsIgnoreCase("SmokeSensor")){
                    existSmokeSensor = true;
                }
                if (((NamedElement)myInstance.getTypeDefinition()).getName().equalsIgnoreCase("HumiditySensor")){
                    existHumiditySensor = true;
                }
            }
            if (!existHumiditySensor) {
                Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                myMap.put(this.typeDefinition, componentTypes.get("HumiditySensor"));
                myMap.put(this.nodeName, (NamedElement) containerNode);
                results.add(myMap);
            }
            if (!existSmokeSensor) {
                Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                myMap.put(this.typeDefinition, componentTypes.get("SmokeSensor"));
                myMap.put(this.nodeName, (NamedElement) containerNode);
                results.add(myMap);
            }
            if (!existTempSensor) {
                Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                myMap.put(this.typeDefinition, componentTypes.get("TempSensor"));
                myMap.put(this.nodeName, (NamedElement) containerNode);
                results.add(myMap);
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
        // the next line should be removed if we enable script without specifying componentName
        script = script.replace("${" + this.componentName + "}", myMap.get(this.typeDefinition).getName()+increment++ );
        return script;
    }

}

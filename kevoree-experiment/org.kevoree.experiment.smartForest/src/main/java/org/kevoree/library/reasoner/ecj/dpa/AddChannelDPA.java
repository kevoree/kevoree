package org.kevoree.library.reasoner.ecj.dpa;

import org.kevoree.ChannelType;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.TypeDefinition;
import org.kevoree.library.tools.dpa.DPA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddChannelDPA implements DPA {

    public final static String channelName = "channel";
    public final static String typeDefinition = "type";

    public final static String templateScript = "tblock { \n" +
            "  addChannel ${channel} : ${type}\n" +
            "}";
    private static int increment = 0;

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (TypeDefinition typeDef : myModel.getTypeDefinitions()) {
            if (typeDef instanceof ChannelType){
                Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                myMap.put(this.typeDefinition, (NamedElement) typeDef);
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
        // the next line should be removed if we enable script without specifying componentName
        script = script.replace("${" + this.channelName + "}", myMap.get(this.typeDefinition).getName()+increment++ );
        return script;
    }

    @Override
    public org.kevoree.tools.marShell.ast.Script getASTScript(Map<String, NamedElement> stringNamedElementMap) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}

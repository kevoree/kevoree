package org.kevoree.library.reasoner.ecj.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kevoree.Channel;
import org.kevoree.ChannelType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.MBinding;
import org.kevoree.NamedElement;
import org.kevoree.Port;
import org.kevoree.TypeDefinition;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class AddChannelDPA implements DPA {

    public final static String channelName = "channel";
    public final static String typeDefinition = "type";

    public final static String scriptPath = "target/classes/addChannel.kevs";
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
        String script = ParserUtil.loadFile(scriptPath);
        for (String name : myMap.keySet()) {
            script = script.replace("${" + name + "}", myMap.get(name).getName());
        }
        // the next line should be removed if we enable script without specifying componentName
        script = script.replace("${" + this.channelName + "}", myMap.get(this.typeDefinition).getName()+increment++ );
        return script;
    }

}

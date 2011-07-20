package org.kevoree.library.reasoner.ecj.dpa;

import org.kevoree.Channel;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.library.tools.dpa.DPA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveChannelDPA implements DPA {

    public final static String channelName = "channel";

    public final static String templateScript = "tblock { \n" +
            "  removeChannel ${channel}\n" +
            "}";

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (Channel channel : myModel.getHubs()) {
            Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
            myMap.put(this.channelName, (NamedElement) channel);
            results.add(myMap);

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
    public org.kevoree.tools.marShell.ast.Script getASTScript(Map<String, NamedElement> stringNamedElementMap) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}

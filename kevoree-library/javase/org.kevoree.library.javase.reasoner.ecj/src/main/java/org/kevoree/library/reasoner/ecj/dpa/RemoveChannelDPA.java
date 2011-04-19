package org.kevoree.library.reasoner.ecj.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kevoree.Channel;
import org.kevoree.ChannelType;
import org.kevoree.ContainerRoot;
import org.kevoree.NamedElement;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class RemoveChannelDPA implements DPA {

    public final static String channelName = "channel";

    public final static String scriptPath = "removeChannel.kevs";

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
        String script = ParserUtil.loadFile(this.getClass().getClassLoader().getResource(scriptPath).getFile());
        for (String name : myMap.keySet()) {
            script = script.replace("${" + name + "}", myMap.get(name).getName());
        }
        return script;
    }

}

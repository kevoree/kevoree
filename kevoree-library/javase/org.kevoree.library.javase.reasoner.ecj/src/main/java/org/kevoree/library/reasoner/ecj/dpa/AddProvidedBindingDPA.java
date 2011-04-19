package org.kevoree.library.reasoner.ecj.dpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.MBinding;
import org.kevoree.NamedElement;
import org.kevoree.Port;
import org.kevoree.library.tools.dpa.DPA;
import org.kevoree.tools.marShell.parser.ParserUtil;

public class AddProvidedBindingDPA implements DPA {

    public final static String portName = "port";
    public final static String componentName = "component";
    public final static String channelName = "channel";
    public final static String nodeName = "node";

    public final static String scriptPath = "addBinding.kevs";

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (ContainerNode containerNode : myModel.getNodes()) {
            for (ComponentInstance componentInstance : containerNode
                    .getComponents()) {
                for (Port myPort : componentInstance.getProvided()) {
                    for (Channel myChannel : myModel.getHubs()) {
                        if (!existBinding(myModel, myPort, myChannel)) {
                            Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                            myMap.put(AddProvidedBindingDPA.portName,
                                    (NamedElement) myPort.getPortTypeRef());
                            myMap.put(AddProvidedBindingDPA.channelName,
                                    (NamedElement) myChannel);
                            myMap.put(AddProvidedBindingDPA.componentName,
                                    (NamedElement) componentInstance);
                            myMap.put(AddProvidedBindingDPA.nodeName,
                                    (NamedElement) containerNode);
                            results.add(myMap);
                        }
                    }
                }
            }

        }
        return results;
    }

    private boolean existBinding(ContainerRoot myModel, Port myPort,
            Channel myChannel) {
        for (MBinding binding : myModel.getMBindings()) {
            if (binding.getHub().equals(myChannel)
                    && binding.getPort().equals(myPort)) {
                return true;
            }
        }
        return false;
    }

    public String getScript(Map<String, NamedElement> myMap) {
        String script = ParserUtil.loadFile(this.getClass().getClassLoader().getResource(scriptPath).getFile());
        for (String name : myMap.keySet()) {
            script = script.replace("${" + name + "}", myMap.get(name).getName());
        }
        return script;
    }

}

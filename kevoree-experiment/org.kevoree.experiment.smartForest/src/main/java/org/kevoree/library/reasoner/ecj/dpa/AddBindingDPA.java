package org.kevoree.library.reasoner.ecj.dpa;

import org.kevoree.*;
import org.kevoree.library.tools.dpa.DPA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBindingDPA implements DPA {

    public final static String portName = "port";
    public final static String componentName = "component";
    public final static String channelName = "channel";
    public final static String nodeName = "node";

    public final static String templateScript = "tblock { \n" +
            " bind ${component}.${port}@${node} => ${channel}\n" +
            " }";

    public List<Map<String, NamedElement>> applyPointcut(ContainerRoot myModel) {
        List<Map<String, NamedElement>> results = new ArrayList();
        for (ContainerNode containerNode : myModel.getNodes()) {
            for (ComponentInstance componentInstance : containerNode
                    .getComponents()) {
                for (Port myPort : componentInstance.getRequired()) {
                    for (Channel myChannel : myModel.getHubs()) {
                        if (!existBinding(myModel, myPort, myChannel)) {
                            Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                            myMap.put(AddBindingDPA.portName,
                                    (NamedElement) myPort.getPortTypeRef());
                            myMap.put(AddBindingDPA.channelName,
                                    (NamedElement) myChannel);
                            myMap.put(AddBindingDPA.componentName,
                                    (NamedElement) componentInstance);
                            myMap.put(AddBindingDPA.nodeName,
                                    (NamedElement) containerNode);
                            results.add(myMap);
                        }
                    }
                }
                for (Port myPort : componentInstance.getProvided()) {
                    for (Channel myChannel : myModel.getHubs()) {
                        if (!existBinding(myModel, myPort, myChannel)) {
                            Map<String, NamedElement> myMap = new HashMap<String, NamedElement>();
                            myMap.put(AddBindingDPA.portName,
                                    (NamedElement) myPort.getPortTypeRef());
                            myMap.put(AddBindingDPA.channelName,
                                    (NamedElement) myChannel);
                            myMap.put(AddBindingDPA.componentName,
                                    (NamedElement) componentInstance);
                            myMap.put(AddBindingDPA.nodeName,
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

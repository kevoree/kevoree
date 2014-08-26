package org.kevoree.kevscript;

import org.kevoree.*;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.util.ModelVisitor;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/12/2013
 * Time: 10:29
 */
public class KevScriptExporter {

    public static String export(ContainerRoot model) {
        final StringBuilder buffer = new StringBuilder();
        for (Repository repo : model.getRepositories()) {
            buffer.append("repo \"" + repo.getUrl() + "\"\n");
        }
        //process deploy unit
        model.visit(new ModelVisitor() {
            @Override
            public void visit(KMFContainer kmfContainer, String s, KMFContainer kmfContainer2) {
                if (kmfContainer instanceof DeployUnit) {
                    DeployUnit currentDU = (DeployUnit) kmfContainer;
                    buffer.append("include mvn:" + KModelHelper.fqnGroup(currentDU) + ":" + currentDU.getName() + ":" + currentDU.getVersion() + "\n");
                }
            }
        }, true, true, false);
        //process instance creation
        model.visit(new ModelVisitor() {
            @Override
            public void visit(KMFContainer kmfContainer, String s, KMFContainer kmfContainer2) {
                if (kmfContainer instanceof Instance) {
                    Instance currentInstance = (Instance) kmfContainer;
                    String instanceID = null;

                    if (currentInstance instanceof ComponentInstance) {
                        Instance nodeParent = (Instance) kmfContainer.eContainer();
                        instanceID = nodeParent.getName() + "." + currentInstance.getName();
                        buffer.append("add " + instanceID + " : " + currentInstance.getTypeDefinition().getName() + "/" + currentInstance.getTypeDefinition().getVersion() + "\n");
                    } else {
                        instanceID = currentInstance.getName();
                        buffer.append("add " + instanceID + " : " + currentInstance.getTypeDefinition().getName() + "/" + currentInstance.getTypeDefinition().getVersion() + "\n");
                    }
                    //output all the dictionary
                    Dictionary dico = currentInstance.getDictionary();
                    if (dico != null) {
                        for (Value value : dico.getValues()) {
                            buffer.append("set " + instanceID + "." + value.getName() + " = \"" + value.getValue() + "\"\n");
                        }
                    }
                    for (FragmentDictionary fdic : currentInstance.getFragmentDictionary()) {
                        for (Value value : fdic.getValues()) {
                            buffer.append("set " + instanceID + "." + value.getName() + "/" + fdic.getName() + " = \"" + value.getValue() + "\"\n");
                        }
                    }
                    buffer.append("set " + instanceID + ".started = \"" + currentInstance.getStarted() + "\"\n");
                }
            }
        }, true, true, false);
        //process binding
        for (MBinding mb : model.getmBindings()) {
            Port p = mb.getPort();
            ComponentInstance comp = (ComponentInstance) p.eContainer();
            ContainerNode node = (ContainerNode) comp.eContainer();
            buffer.append("bind " + node.getName() + "." + comp.getName() + "." + p.getPortTypeRef().getName() + " " + mb.getHub().getName() + "\n");
        }
        //process group subscription
        for (Group group : model.getGroups()) {
            for (ContainerNode child : group.getSubNodes()) {
                buffer.append("attach " + child.getName() + " " + group.getName() + "\n");
            }
        }
        return buffer.toString();
    }

}

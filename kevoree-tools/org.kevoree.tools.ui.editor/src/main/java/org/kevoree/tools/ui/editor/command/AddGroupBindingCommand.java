package org.kevoree.tools.ui.editor.command;


import org.kevoree.*;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.*;

public class AddGroupBindingCommand implements Command {

    private KevoreeUIKernel kernel;
    private NodePanel target;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    public void setTarget(NodePanel target) {
        this.target = target;
    }


    @Override
    public void execute(Object p) {

        if (p instanceof GroupAnchorPanel) {
            GroupAnchorPanel fromPanel = (GroupAnchorPanel) p;
            // if (fromPanel.getNature().equals(PortNature.MESSAGE)) {
            Group fromPort = (Group) kernel.getUifactory().getMapping().get(fromPanel.getParentPanel());
            ContainerNode targetNode = (ContainerNode) kernel.getUifactory().getMapping().get(target);

            //ADD ContainerNode to Group
            fromPort.getSubNodes().add(targetNode);

            org.kevoree.tools.ui.framework.elements.Binding uib = new Binding(Binding.Type.groupLink);
            uib.setFrom(fromPanel);
            uib.setTo(target);

            kernel.getModelPanel().addBinding(uib);

        }


    }
}

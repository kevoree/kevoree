/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* $Id: Art2UIFactory.java 13282 2010-11-03 09:52:16Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor;

import org.kevoree.*;
import org.kevoree.framework.aspects.PortAspect;
import org.kevoree.tools.ui.editor.command.*;
import org.kevoree.tools.ui.editor.listener.*;
import org.kevoree.tools.ui.editor.widget.TempGroupBinding;
import org.kevoree.tools.ui.framework.elements.*;
import org.kevoree.tools.ui.framework.elements.PortPanel.PortType;

import java.awt.*;

/**
 * @author ffouquet
 */
public class KevoreeUIFactory {

    private KevoreeUIKernel kernel;

    public KevoreeUIFactory(KevoreeUIKernel _kernel) {
        kernel = _kernel;
    }

    private MappingRepository mapping = new MappingRepository();

    public MappingRepository getMapping() {
        return mapping;
    }

    public ModelPanel createModelPanelUI(ContainerRoot ct) {
        ModelPanel mui = new ModelPanel();
        ((Component) mui).setDropTarget(new ModelDragTargetListener(mui, kernel));

        CommandMouseListener listener = new CommandMouseListener();
        UnSelectPropertyEditor command = new UnSelectPropertyEditor();
        command.setKernel(kernel);
        listener.setLeftClickCommand(command);
        mui.addMouseListener(listener);

        mapping.bind(mui, ct);
        return mui;
    }

    public ComponentTypePanel createComponentTypeUI(ComponentType ct) {
        ComponentTypePanel ctui = new ComponentTypePanel();
        ctui.setTypeName(ct.getName());
        ctui.setName(" ");

        for (PortTypeRef p : ct.getProvidedForJ()) {
            PortTypePanel portPanel = kernel.getUifactory().createPortType(p, true);
            ctui.addLeft(portPanel);
        }
        for (PortTypeRef p : ct.getRequiredForJ()) {
            PortTypePanel portPanel = kernel.getUifactory().createPortType(p, false);
            ctui.addRight(portPanel);
        }

        ComponentTypeDragSourceListener listener = new ComponentTypeDragSourceListener(ctui, kernel);
        mapping.bind(ctui, ct);
        return ctui;
    }

    public ChannelTypePanel createChannelTypeUI(ChannelType ct) {
        ChannelTypePanel ctui = new ChannelTypePanel();
        ctui.setTitle(ct.getName());
        ChannelTypeDragSourceListener listener = new ChannelTypeDragSourceListener(ctui, kernel);
        mapping.bind(ctui, ct);
        return ctui;
    }

    public GroupTypePanel createGroupTypeUI(GroupType ct) {
        GroupTypePanel ctui = new GroupTypePanel();
        ctui.setTitle(ct.getName());
        GroupTypeDragSourceListener listener = new GroupTypeDragSourceListener(ctui, kernel);
        mapping.bind(ctui, ct);
        return ctui;
    }

    public NodeTypePanel createNodeTypeUI(NodeType ct) {
        NodeTypePanel ctui = new NodeTypePanel();
        ctui.setTitle(ct.getName());
        NodeTypeDragSourceListener listener = new NodeTypeDragSourceListener(ctui, kernel);
        mapping.bind(ctui, ct);
        return ctui;
    }


    public ComponentPanel createComponentInstance(ComponentInstance ci) {
        ComponentPanel cui = new ComponentPanel();
        ComponentDragSourceListener draglistener = new ComponentDragSourceListener(cui, kernel);
        cui.setTitle(ci.getName());
        cui.setTypeName(ci.getTypeDefinition().getName());

        CommandMouseListener listener = new CommandMouseListener();
        SelectInstanceCommand command = new SelectInstanceCommand();
        command.setKernel(kernel);
        listener.setLeftClickCommand(command);
        cui.addMouseListener(listener);
        mapping.bind(cui, ci);
        return cui;
    }

    public NodePanel createComponentNode(org.kevoree.ContainerNode node) {
        NodePanel nui = new NodePanel();
        ((Component) nui).setDropTarget(new NodeDragTargetListener(nui, kernel));
        if (node.getTypeDefinition() != null) {
            nui.setTitle(node.getName(), node.getTypeDefinition().getName());
        } else {
            nui.setTitle(node.getName() + " : Node");
        }

        CommandMouseListener listener = new CommandMouseListener();
        SelectInstanceCommand command = new SelectInstanceCommand();
        command.setKernel(kernel);
        listener.setLeftClickCommand(command);
        ContextualMenuCommand rightClicCommand = new ContextualMenuCommand();
        rightClicCommand.setKernel(kernel);
        listener.setRightClickCommand(rightClicCommand);
        nui.addMouseListener(listener);


        mapping.bind(nui, node);
        return nui;
    }

    public ChannelPanel createHub(org.kevoree.Channel hub) {
        ChannelPanel hui = new ChannelPanel();
        ((Component) hui).setDropTarget(new HubDragTargetListener(hui, kernel));
        hui.setTitle(hub.getName() + " : \n" + hub.getTypeDefinition().getName());


        /* ADD SELECT COMMAND */
        CommandMouseListener mouse_listener = new CommandMouseListener();
        SelectInstanceCommand command = new SelectInstanceCommand();
        command.setKernel(kernel);
        mouse_listener.setLeftClickCommand(command);
        hui.addMouseListener(mouse_listener);

        mapping.bind(hui, hub);
        return hui;
    }

    public GroupPanel createGroup(org.kevoree.Group group) {
        GroupPanel hui = new GroupPanel();
        hui.setTitle(group.getName() + " : \n" + group.getTypeDefinition().getName());
        GroupAnchorDragSourceListener draglistener = new GroupAnchorDragSourceListener(hui.getAnchor(), kernel);

        /* ADD SELECT COMMAND */
        CommandMouseListener mouse_listener = new CommandMouseListener();
        SelectInstanceCommand command = new SelectInstanceCommand();
        command.setKernel(kernel);
        mouse_listener.setLeftClickCommand(command);
        hui.addMouseListener(mouse_listener);

        mapping.bind(hui, group);
        return hui;
    }


    public PortPanel createPort(org.kevoree.Port port) {
        PortPanel pui = new PortPanel();
        if (port.getPortTypeRef().getRef() instanceof org.kevoree.MessagePortType) {
            pui.setNature(PortPanel.PortNature.MESSAGE);
        }
        if (port.getPortTypeRef().getRef() instanceof org.kevoree.ServicePortType) {
            pui.setNature(PortPanel.PortNature.SERVICE);
        }
        PortAspect pa = new PortAspect(port);
        if (pa.isProvidedPort()) {
            pui.setType(PortType.PROVIDED);
        } else {
            if (pa.isRequiredPort()) {
                pui.setType(PortType.REQUIRED);
            }
        }

        pui.setTitle(port.getPortTypeRef().getName());
        new PortDragSourceListener(pui, kernel);
        ((Component) pui).setDropTarget(new PortDragTargetListener(pui, kernel));
        mapping.bind(pui, port);
        return pui;
    }

    public PortTypePanel createPortType(PortTypeRef portTypeRef, Boolean providedPort) {
        PortTypePanel pui = new PortTypePanel();
        if (portTypeRef.getRef() instanceof org.kevoree.MessagePortType) {
            pui.setNature(PortPanel.PortNature.MESSAGE);
        }
        if (portTypeRef.getRef() instanceof org.kevoree.ServicePortType) {
            pui.setNature(PortPanel.PortNature.SERVICE);
        }
        if (providedPort) {
            pui.setType(PortType.PROVIDED);
        } else {
            pui.setType(PortType.REQUIRED);
        }
        pui.setTitle(portTypeRef.getName());
        mapping.bind(pui, portTypeRef);
        return pui;
    }


    /*
    public Binding createBinding(org.kevoree.Binding mb) {
    Binding bui = new Binding(Binding.Type.simple);
    PortPanel fromPortPanel = (PortPanel) kernel.getUifactory().getMapping().get(mb.getPorts().get(0));
    PortPanel toPortPanel = (PortPanel) kernel.getUifactory().getMapping().get(mb.getPorts().get(1));
    bui.setFrom(fromPortPanel);
    bui.setTo(toPortPanel);
    mapping.bind(bui, mb);
    return bui;
    }*/

    public Binding createGroupBinding(TempGroupBinding binding) {
        final org.kevoree.tools.ui.framework.elements.Binding uib = new Binding(Binding.Type.groupLink);
        uib.setFrom(binding.getGroupPanel());
        uib.setTo(binding.getNodePanel());
        mapping.bind(uib, binding);
        final SelectGroupBindingCommand command = new SelectGroupBindingCommand();
        command.setKernel(kernel);
        uib.addListener(new BindingListener() {
            @Override
            public void clicked() {
                command.execute(uib);
            }
        });
        
        return uib;
    }


    public Binding createMBinding(org.kevoree.MBinding mb) {
        Binding bui = null;
        PortAspect pa = new PortAspect(mb.getPort());
        if (pa.isProvidedPort()) {
            bui = new Binding(Binding.Type.input);
        } else {
            if (pa.isRequiredPort()) {
                bui = new Binding(Binding.Type.ouput);
            }
        }

        PortPanel fromPortPanel = (PortPanel) kernel.getUifactory().getMapping().get(mb.getPort());
        ChannelPanel toPortPanel = (ChannelPanel) kernel.getUifactory().getMapping().get(mb.getHub());
        bui.setFrom(fromPortPanel);
        bui.setTo(toPortPanel);
        mapping.bind(bui, mb);


        /* ADD SELECT COMMAND */
        final SelectBindingCommand command = new SelectBindingCommand();
        command.setKernel(kernel);
        final Binding buiFinal = bui;
        bui.addListener(new BindingListener() {
            @Override
            public void clicked() {
                command.execute(buiFinal);
            }
        });

        return bui;
    }
}

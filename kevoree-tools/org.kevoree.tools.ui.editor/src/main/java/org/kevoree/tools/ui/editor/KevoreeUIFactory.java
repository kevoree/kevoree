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

import org.kevoree.ComponentInstance;
import org.kevoree.ComponentType;
import java.awt.Component;

import org.kevoree.ChannelType;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.aspects.PortAspect;
import org.kevoree.tools.ui.editor.command.ContextualMenuCommand;
import org.kevoree.tools.ui.editor.command.SelectInstanceCommand;
import org.kevoree.tools.ui.editor.listener.ChannelTypeDragSourceListener;
import org.kevoree.tools.ui.editor.listener.CommandMouseListener;
import org.kevoree.tools.ui.editor.listener.ComponentDragSourceListener;
import org.kevoree.tools.ui.editor.listener.ComponentTypeDragSourceListener;
import org.kevoree.tools.ui.editor.listener.HubDragTargetListener;
import org.kevoree.tools.ui.editor.listener.ModelDragTargetListener;
import org.kevoree.tools.ui.editor.listener.NodeDragTargetListener;
import org.kevoree.tools.ui.editor.listener.PortDragSourceListener;
import org.kevoree.tools.ui.editor.listener.PortDragTargetListener;
import org.kevoree.tools.ui.framework.elements.*;
import org.kevoree.tools.ui.framework.elements.PortPanel.PortType;

/**
 *
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
        ContextualMenuCommand command = new ContextualMenuCommand();
        command.setKernel(kernel);
        listener.setRightClickCommand(command);
        mui.addMouseListener(listener);

        mapping.bind(mui, ct);
        return mui;
    }

    public ComponentTypePanel createComponentTypeUI(ComponentType ct) {
        ComponentTypePanel ctui = new ComponentTypePanel(ct.getName());
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
        nui.setTitle(node.getName() + " : Node");


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
        return bui;
    }
}

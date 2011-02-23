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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.Channel;
import org.kevoree.ChannelType;
import org.kevoree.ComponentInstance;
import org.kevoree.ComponentType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.MBinding;
import org.kevoree.Port;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.*;
import org.kevoree.tools.ui.framework.elements.PortPanel.PortType;

import java.util.List;


/**
 *
 * @author ffouquet
 */
public class LoadModelCommand implements Command {

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }
    private KevoreeUIKernel kernel;

    /* Input expected : Model URI */
    @Override
    public void execute(Object p) {

        ContainerRoot previousModel = KevoreeXmiHelper.load(p.toString());
        kernel.getModelHandler().setActualModel(previousModel);
        kernel.getModelPanel().clear();

        //HACK :-) TODO REMOVE
        kernel.getUifactory().getMapping().bind(kernel.getModelPanel(), previousModel);


        /* Synch every UI Component */
        //LOAD COMPONENT TYPE
        kernel.getEditorPanel().getPalette().clear();
        for (org.kevoree.TypeLibrary ctl : kernel.getModelHandler().getActualModel().getLibraries()) {
            //System.out.println(ctl.getName());
            for (org.kevoree.TypeDefinition ct : ctl.getSubTypes()) {
                //System.out.println(ct.getName());
                if (ct instanceof ComponentType) {
                    ComponentTypePanel ctp = kernel.getUifactory().createComponentTypeUI((ComponentType) ct);
                    kernel.getEditorPanel().getPalette().addTypeDefinitionPanel(ctp, ctl.getName());
                }
                if (ct instanceof ChannelType) {
                    ChannelTypePanel ctp = kernel.getUifactory().createChannelTypeUI((ChannelType) ct);
                    kernel.getEditorPanel().getPalette().addTypeDefinitionPanel(ctp, ctl.getName());
                }
            }
        }

        //LOAD NODE
        for (ContainerNode newnode : kernel.getModelHandler().getActualModel().getNodes()) {
            NodePanel newnodepanel = kernel.getUifactory().createComponentNode(newnode);
            kernel.getModelPanel().addNode(newnodepanel);
            for (ComponentInstance ci : newnode.getComponents()) {
                ComponentPanel insPanel = kernel.getUifactory().createComponentInstance(ci);
                for (Port portP : ci.getProvided()) {
                    //ADDING NEW PORT TO UI
                    PortPanel portPanel = kernel.getUifactory().createPort(portP);
                    portPanel.setType(PortType.PROVIDED);
                    insPanel.addLeft(portPanel);
                }
                for (Port portR : ci.getRequired()) {
                    //ADDING NEW PORT TO UI
                    PortPanel portPanel = kernel.getUifactory().createPort(portR);
                    portPanel.setType(PortType.REQUIRED);
                    insPanel.addRight(portPanel);
                }
                newnodepanel.add(insPanel);
                //kernel.getModelPanel().add(insPanel);
            }
        }
        //LOAD HUB
        for (Channel hub : kernel.getModelHandler().getActualModel().getHubs()) {
            ChannelPanel newhubpanel = kernel.getUifactory().createHub(hub);
            kernel.getModelPanel().addHub(newhubpanel);
        }


        //LOAD BINDING
        /*
        for (Binding binding : kernel.getModelHandler().getActualModel().getBindings()) {
        org.kevoree.ui.framework.elements.Binding uib = kernel.getUifactory().createBinding(binding);
        kernel.getModelPanel().addBinding(uib);
        }*/

        //LOAD MBINDING
        for (MBinding binding : kernel.getModelHandler().getActualModel().getMBindings()) {
            Binding uib = kernel.getUifactory().createMBinding(binding);
            kernel.getModelPanel().addBinding(uib);
        }


        //REFRESH UI
        kernel.getEditorPanel().doLayout();
        kernel.getEditorPanel().repaint();
        kernel.getEditorPanel().revalidate();

    }
}

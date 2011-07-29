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

import org.kevoree.*;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.ui.editor.*;
import org.kevoree.tools.ui.framework.elements.*;
import org.kevoree.tools.ui.framework.elements.PortPanel.PortType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * @author ffouquet
 */
public class LoadModelCommand implements Command {


    ReloadTypePalette subCommand = new ReloadTypePalette();

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
        subCommand.setKernel(this.kernel);
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
        subCommand.execute(null);


        //LOAD NODE
        for (ContainerNode newnode : kernel.getModelHandler().getActualModel().getNodes()) {
            NodePanel newnodepanel = kernel.getUifactory().createComponentNode(newnode);
            kernel.getModelPanel().addNode(newnodepanel);
            //UI
            HashMap<String, String> metaData = MetaDataHelper.getMetaDataFromInstance(newnode);
            if (MetaDataHelper.containKeys(Arrays.asList("x", "y"), metaData)) {
                newnodepanel.setLocation(new Point(Integer.parseInt(metaData.get("x").toString()), Integer.parseInt(metaData.get("y").toString())));
            }

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

            HashMap<String, String> metaData = MetaDataHelper.getMetaDataFromInstance(hub);
            if (MetaDataHelper.containKeys(Arrays.asList("x", "y"), metaData)) {
                newhubpanel.setLocation(new Point(Integer.parseInt(metaData.get("x").toString()), Integer.parseInt(metaData.get("y").toString())));
            }

        }

        //LOAD GROUP
        for (Group group : kernel.getModelHandler().getActualModel().getGroups()) {
            GroupPanel newgrouppanel = kernel.getUifactory().createGroup(group);
            kernel.getModelPanel().addGroup(newgrouppanel);
            //LOAD GROUP BINDINGS
            for (ContainerNode subNode : group.getSubNodes()) {
                NodePanel nodePanel = (NodePanel) kernel.getUifactory().getMapping().get(subNode);
                org.kevoree.tools.ui.framework.elements.Binding uib = new Binding(Binding.Type.groupLink);
                uib.setFrom(newgrouppanel.getAnchor());
                uib.setTo(nodePanel);
                kernel.getModelPanel().addBinding(uib);
            }
            HashMap<String, String> metaData = MetaDataHelper.getMetaDataFromInstance(group);
            if (MetaDataHelper.containKeys(Arrays.asList("x", "y"), metaData)) {
                newgrouppanel.setLocation(new Point(Integer.parseInt(metaData.get("x").toString()), Integer.parseInt(metaData.get("y").toString())));
            }

        }


        //LOAD BINDING
        /*
        for (Binding binding : kernel.getModelHandler().getActualModel().getBindings()) {
        org.kevoree.ui.framework.elements.Binding uib = kernel.getUifactory().createBinding(binding);
        kernel.getModelPanel().removeBinding(uib);
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

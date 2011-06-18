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
/* $Id: AddComponentCommand.java 12431 2010-09-15 14:52:41Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.KevoreeFactory;
import org.kevoree.ComponentInstance;
import org.kevoree.ComponentType;
import org.kevoree.ContainerNode;
import org.kevoree.Port;
import org.kevoree.PortTypeRef;
import java.util.Random;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.ModelHelper;
import org.kevoree.tools.ui.framework.elements.*;
import org.kevoree.tools.ui.framework.elements.PortPanel.PortType;

/**
 *
 * @author ffouquet
 */
public class AddComponentCommand implements Command {

    private NodePanel nodepanel;
    private KevoreeUIKernel kernel;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    public void setNodepanel(NodePanel nodepanel) {
        this.nodepanel = nodepanel;
    }

    @Override
    public void execute(Object p) {
        if(p instanceof ComponentTypePanel){
            ComponentInstance instance = KevoreeFactory.eINSTANCE.createComponentInstance();
            
            ContainerNode node = (ContainerNode) kernel.getUifactory().getMapping().get(nodepanel);
            ComponentType type = (ComponentType) kernel.getUifactory().getMapping().get(p);
            instance.setTypeDefinition(type);
            instance.setName(type.getName().substring(0,Math.min(type.getName().length(),9))+""+Math.abs(new Random().nextInt(999)));
            ComponentPanel insPanel = kernel.getUifactory().createComponentInstance(instance);

            for(PortTypeRef ref : type.getProvided()){
                //INSTANCIATE MODEL ELEMENTS
                Port port = KevoreeFactory.eINSTANCE.createPort();
                instance.getProvided().add(port);
                //port.setName(ref.getName());
                port.setPortTypeRef(ref);
                
                //ADDING NEW PORT TO UI
                PortPanel portPanel = kernel.getUifactory().createPort(port);
                portPanel.setType(PortType.PROVIDED);
                insPanel.addLeft(portPanel);
            }
            for(PortTypeRef ref : type.getRequired()){
                //INSTANCIATE MODEL ELEMENTS
                Port port = KevoreeFactory.eINSTANCE.createPort();
                instance.getRequired().add(port);
                //port.setName(ref.getName());
                port.setPortTypeRef(ref);

                //ADDING NEW PORT TO UI
                PortPanel portPanel = kernel.getUifactory().createPort(port);
                portPanel.setType(PortType.REQUIRED);
                insPanel.addRight(portPanel);
            }

            nodepanel.add(insPanel);
            node.getComponents().add(instance);
                    kernel.getEditorPanel().getPalette().updateTypeValue(ModelHelper.getTypeNbInstance(kernel.getModelHandler().getActualModel(), type),type.getName());
        } 
    }

}

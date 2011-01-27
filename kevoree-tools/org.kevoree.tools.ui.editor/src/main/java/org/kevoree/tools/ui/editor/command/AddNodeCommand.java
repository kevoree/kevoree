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
/* $Id: AddNodeCommand.java 11975 2010-08-02 16:02:55Z dvojtise $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.ContainerNode;
import org.kevoree.KevoreeFactory;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.NodePanel;

/**
 *
 * @author ffouquet
 */
public class AddNodeCommand implements Command {

    private KevoreeUIKernel kernel;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void execute(Object p) {
        ContainerNode newnode = KevoreeFactory.eINSTANCE.createContainerNode();
        //CREATE NEW NAME
        //TODO CHECK EXISTING NAME
        if(kernel.getModelHandler().getActualModel().getNodes().size()>0){
            newnode.setName("node-"+kernel.getModelHandler().getActualModel().getNodes().size());
        }else {
            newnode.setName("KEVOREEDefaultNodeName");
        }

        NodePanel newnodepanel = kernel.getUifactory().createComponentNode(newnode);
        kernel.getModelHandler().getActualModel().getNodes().add(newnode);
        kernel.getModelPanel().addNode(newnodepanel);
    }

    

}

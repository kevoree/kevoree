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
/* $Id: MoveComponentCommand.java 11975 2010-08-02 16:02:55Z dvojtise $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.ComponentPanel;
import org.kevoree.tools.ui.framework.elements.ComponentTypePanel;
import org.kevoree.tools.ui.framework.elements.NodePanel;

/**
 *
 * @author ffouquet
 */
public class MoveComponentCommand implements Command {

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
        if (p instanceof ComponentPanel && !(p instanceof ComponentTypePanel)) {

            ComponentPanel panel = (ComponentPanel) p ;
            ContainerNode node = (ContainerNode) kernel.getUifactory().getMapping().get(nodepanel);
            ComponentInstance type = (ComponentInstance) kernel.getUifactory().getMapping().get(p);
            
            if(!node.getComponentsForJ().contains(type)){
                panel.getParent().remove(panel);
                nodepanel.add(panel);
                node.addComponents(type);
            }
        }
    }
}

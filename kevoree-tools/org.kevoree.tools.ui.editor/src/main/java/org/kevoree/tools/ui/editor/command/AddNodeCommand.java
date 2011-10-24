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
import org.kevoree.NodeType;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.ModelHelper;
import org.kevoree.tools.ui.framework.elements.NodePanel;

import java.awt.*;

/**
 * @author ffouquet
 */
public class AddNodeCommand implements Command {

    private KevoreeUIKernel kernel;

    private Point point = null;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    public void setPoint(Point p) {
        this.point = p;
    }

    @Override
    public void execute(Object p) {

        ContainerNode newnode = KevoreeFactory.createContainerNode();
            newnode.setName(ModelHelper.getNextAvailableNodeName(kernel.getModelHandler().getActualModel()));
        //} else {
        //    newnode.setName("node1");
        //}

        NodeType type = (NodeType) kernel.getUifactory().getMapping().get(p);
        if (type != null) {
            newnode.setTypeDefinition((NodeType) type);
        }

        NodePanel newnodepanel = kernel.getUifactory().createComponentNode(newnode);
        kernel.getModelHandler().getActualModel().addNodes(newnode);
        kernel.getModelPanel().addNode(newnodepanel);

        if ((point.x - newnodepanel.getPreferredSize().getHeight() / 2 > 0) && (point.y - newnodepanel.getPreferredSize().getHeight() / 2 > 0)) {
            newnodepanel.setLocation((int) (point.x - newnodepanel.getPreferredSize().getWidth() / 2), (int) (point.y - newnodepanel.getPreferredSize().getHeight() / 2));
        } else {
            newnodepanel.setLocation(point.x, point.y);
        }

        kernel.getEditorPanel().getPalette().updateTypeValue(ModelHelper.getTypeNbInstance(kernel.getModelHandler().getActualModel(), type),type.getName());

        kernel.getModelHandler().notifyChanged();


    }


}

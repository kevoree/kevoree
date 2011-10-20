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
package org.kevoree.tools.ui.editor.command;


import org.kevoree.*;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.widget.TempGroupBinding;
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
            fromPort.addSubNodes(targetNode);


            TempGroupBinding groupB = new TempGroupBinding();
            groupB.setOriginGroup(fromPort);
            groupB.setTargetNode(targetNode);
            groupB.setGroupPanel(fromPanel);
            groupB.setNodePanel(target);
            Binding uib = kernel.getUifactory().createGroupBinding(groupB);
            kernel.getModelPanel().addBinding(uib);


            kernel.getModelHandler().notifyChanged();

        }


    }
}

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
package org.kevoree.tools.ui.editor.widget;

import org.kevoree.ContainerNode;
import org.kevoree.Group;
import org.kevoree.tools.ui.framework.elements.GroupAnchorPanel;
import org.kevoree.tools.ui.framework.elements.GroupPanel;
import org.kevoree.tools.ui.framework.elements.NodePanel;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/10/11
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class TempGroupBinding {
    
    private Group originGroup;

    private GroupAnchorPanel groupPanel;
    
    private ContainerNode targetNode;

    public GroupAnchorPanel getGroupPanel() {
        return groupPanel;
    }

    public void setGroupPanel(GroupAnchorPanel groupPanel) {
        this.groupPanel = groupPanel;
    }

    public NodePanel getNodePanel() {
        return nodePanel;
    }

    public void setNodePanel(NodePanel nodePanel) {
        this.nodePanel = nodePanel;
    }

    private NodePanel nodePanel;

    public Group getOriginGroup() {
        return originGroup;
    }

    public void setOriginGroup(Group originGroup) {
        this.originGroup = originGroup;
    }

    public ContainerNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(ContainerNode targetNode) {
        this.targetNode = targetNode;
    }
}

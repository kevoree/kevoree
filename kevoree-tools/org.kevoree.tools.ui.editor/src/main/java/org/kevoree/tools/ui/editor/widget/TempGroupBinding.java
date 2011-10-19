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

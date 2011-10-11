package org.kevoree.library.sky.minicloud.group;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.library.sky.minicloud.KevoreeNodeManager;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 11/10/11
 * Time: 18:28
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@GroupType
public class MiniCloudGroup  extends AbstractGroupType {

	@Start
	@Stop
	public void lifeCycleOperation() {

	}

	@Override
	public void triggerModelUpdate () {
		for (Group g : this.getModelService().getLastModel().getGroupsForJ()) {
			if (g.getName().equals(this.getName())) {
				for (ContainerNode n : g.getSubNodesForJ()) {
					KevoreeNodeManager.updateNode(n, this.getModelService().getLastModel());
				}
			}
		}
	}

	@Override
	public void push (ContainerRoot containerRoot, String s) {
	}

	@Override
	public ContainerRoot pull (String s) {
		return null;
	}
}

package org.kevoree.library.sky.minicloud.nodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.sky.manager.KevoreeNodeRunner;
import org.kevoree.library.sky.manager.nodeType.IaaSNode;
import org.kevoree.library.sky.minicloud.MiniCloudKevoreeNodeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "SKY")
@NodeType
public class MiniCloudNode extends IaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(MiniCloudNode.class);

	@Override
	public KevoreeNodeRunner createKevoreeNodeRunner (String nodeName, String bootStrapModel, ContainerRoot model) {
		return new MiniCloudKevoreeNodeRunner(nodeName, bootStrapModel);
	}

	@Start
	@Override
	public void startNode () {

		super.startNode();
	}

	@Stop
	@Override
	public void stopNode () {
		super.stopNode();
	}

}

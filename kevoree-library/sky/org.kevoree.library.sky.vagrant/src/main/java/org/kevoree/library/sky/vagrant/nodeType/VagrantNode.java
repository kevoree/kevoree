package org.kevoree.library.sky.vagrant.nodeType;

import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.manager.KevoreeNodeRunner;
import org.kevoree.library.sky.manager.nodeType.SkyNode;
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
public class VagrantNode extends SkyNode {
	private static final Logger logger = LoggerFactory.getLogger(VagrantNode.class);

	protected static final String REMOVE_NODE = "RemoveNode";
	protected static final String ADD_NODE = "AddNode";


	@Override
	protected KevoreeNodeRunner createKevoreeNodeRunner () {
		return null;
	}
}

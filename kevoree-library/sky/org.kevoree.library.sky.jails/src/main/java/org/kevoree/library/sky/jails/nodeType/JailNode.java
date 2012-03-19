package org.kevoree.library.sky.jails.nodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.sky.jails.JailKevoreeNodeRunner;
import org.kevoree.library.sky.manager.KevoreeNodeRunner;
import org.kevoree.library.sky.manager.nodeType.IaaSNode;
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
@DictionaryType({
		@DictionaryAttribute(name = "inet", defaultValue = "alc0", optional = false),
		@DictionaryAttribute(name = "subnet", defaultValue = "10.0.0.0", optional = false),
		@DictionaryAttribute(name = "mask", defaultValue = "24", vals={"8", "16", "24"}, optional = false),
		@DictionaryAttribute(name = "flavor", defaultValue = "kevjail", optional = false)
})
@NodeType
public class JailNode extends IaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(JailNode.class);

	/*@Start
	@Override
	public void startNode () {
		super.startNode();
	}

	@Stop
	@Override
	public void stopNode () {
		logger.debug("stopping node {}", this.getNodeName());
		super.stopNode();
	}*/

    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new JailKevoreeNodeRunner(nodeName, this.getDictionary().get("inet").toString(),
      				this.getDictionary().get("subnet").toString(), this.getDictionary().get("mask").toString(), this.getDictionary().get("flavor").toString(),this);
    }
}
package org.kevoree.library.sky.jails.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
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
		@DictionaryAttribute(name = "mask", defaultValue = "24", vals = {"8", "16", "24"}, optional = false),
		@DictionaryAttribute(name = "flavor", defaultValue = "kevjail", optional = false)
})
@NodeType
public class JailNode extends IaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(JailNode.class);

	@Override
	public KevoreeNodeRunner createKevoreeNodeRunner (String nodeName) {
		return new JailKevoreeNodeRunner(this);
	}

	public String getNetworkInterface () {
		return this.getDictionary().get("inet").toString();
	}

	public String getNetwork () {
		return this.getDictionary().get("subnet").toString();
	}

	public String getMask () {
		return this.getDictionary().get("mask").toString();
	}

	public String getFlavor () {
		return this.getDictionary().get("flavor").toString();
	}
}

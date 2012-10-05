package org.kevoree.library.sky.jails.nodeType;

import org.kevoree.annotation.*;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.nodeType.AbstractIaaSNode;
import org.kevoree.library.sky.jails.JailKevoreeNodeRunner;
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
//		@DictionaryAttribute(name = "inet", defaultValue = "alc0", optional = false),
//		@DictionaryAttribute(name = "subnet", defaultValue = "10.0.0.0", optional = true),
//		@DictionaryAttribute(name = "mask", defaultValue = "24", vals = {"8", "16", "24"}, optional = false),
		@DictionaryAttribute(name = "flavor", optional = true),
		@DictionaryAttribute(name = "jailCreationTimeout", defaultValue = "240000", optional = true), // TODO check with Timeout on adaptation primitive
		@DictionaryAttribute(name = "jailStartTimeout", defaultValue = "10000", optional = true)/*,
		@DictionaryAttribute(name = "useArchive", defaultValue = "false", vals= {"true", "false"}, optional = true),
		@DictionaryAttribute(name = "archives", defaultValue = "http://localhost:8080/archives/", optional = true)*/
})
@NodeType
public class JailNode extends AbstractIaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(JailNode.class);

	private String inet;
	private String subnet;
	private String mask;

	@Start
	public void startNode () {
		inet = this.getDictionary().get("inet").toString();
		subnet = this.getDictionary().get("subnet").toString();
		mask = this.getDictionary().get("mask").toString();
		super.startNode();
	}

	@Update
	public void updateNode () {
		if (!inet.equals(this.getDictionary().get("inet").toString())
				|| !subnet.equals(this.getDictionary().get("subnet").toString())
				|| !mask.equals(this.getDictionary().get("mask").toString())) {
			stopNode();
			startNode();
		}
		super.updateNode();
	}

	protected boolean isDaemon () {
		return true;
	}

	@Override
	public KevoreeNodeRunner createKevoreeNodeRunner (String nodeName) {
		return new JailKevoreeNodeRunner(nodeName, this);
	}

	public String getNetworkInterface () {
		return inet;
	}

	public String getNetwork () {
		return subnet;
	}

	public String getMask () {
		return mask;
	}

	public String getFlavor () {
		if (this.getDictionary().get("flavor") != null) {
			return this.getDictionary().get("flavor").toString();
		}
		return null;
	}

	public boolean useArchive () {
		return "true".equals(this.getDictionary().get("useArchive").toString());
	}

	public String getArchives () {
		return this.getDictionary().get("archives").toString();
	}
}

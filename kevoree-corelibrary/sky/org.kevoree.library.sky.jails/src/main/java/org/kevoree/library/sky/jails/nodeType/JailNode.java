package org.kevoree.library.sky.jails.nodeType;

import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.nodeType.AbstractIaaSNode;
import org.kevoree.library.sky.jails.JailKevoreeNodeRunner;
import org.kevoree.library.sky.jails.JailsReasoner;
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
		@DictionaryAttribute(name = "flavor", optional = true),
		@DictionaryAttribute(name = "jailCreationTimeout", defaultValue = "240000", optional = true), // TODO check with Timeout on adaptation primitive
		@DictionaryAttribute(name = "jailStartTimeout", defaultValue = "10000", optional = true),/*
		@DictionaryAttribute(name = "useArchive", defaultValue = "false", vals= {"true", "false"}, optional = true),
		@DictionaryAttribute(name = "archives", defaultValue = "http://localhost:8080/archives/", optional = true)*/
		@DictionaryAttribute(name = "MODE", defaultValue = "RELAX", vals = {"STRICT", "RELAX", "AVOID"}, optional = true)
		// how the restrictions are manage : STRICT = the jail is stopped, RELAX = the jail continue to execute, AVOID means to refused to execute something that break the limitation
})
@NodeType
public class JailNode extends AbstractIaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(JailNode.class);

	private String inet;
	private String subnet;
	private String mask;
	boolean initialization;

	@Start
	public void startNode () {
		inet = this.getDictionary().get("inet").toString();
		subnet = this.getDictionary().get("subnet").toString();
		mask = this.getDictionary().get("mask").toString();
		super.startNode();
		initialization = true;
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

	public void modelUpdated () {
		if (initialization) {
			KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
			// look at all the vms that are already defined and add them on the model
			JailsReasoner.createNodes(kengine, this);
			updateModel(kengine);
			initialization = false;
		}
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

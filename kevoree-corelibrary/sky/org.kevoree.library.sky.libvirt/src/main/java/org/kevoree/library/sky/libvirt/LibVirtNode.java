package org.kevoree.library.sky.libvirt;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeFragment;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.library.sky.api.nodeType.AbstractIaaSNode;
import org.kevoree.library.sky.api.nodeType.helper.SubnetUtils;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/11/12
 * Time: 19:03
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@NodeFragment
@DictionaryType({
		@DictionaryAttribute(name = "default_DISK", optional = false),
		@DictionaryAttribute(name = "default_COPY_MODE", vals = {"base", "clone", "as_is"}, optional = false)
})
public abstract class LibVirtNode extends AbstractIaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(LibVirtKvmNode.class);
	Connect connection;

	boolean initialization;

	@Override
	public void startNode () {
		super.startNode();
		try {
			connection = new Connect("qemu:///system", false);
		} catch (LibvirtException e) {
			logger.error("Unable to find the hypervisor!", e);
		}
		initialization = true;
	}

	public void modelUpdated () {
		if (initialization) {
			KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
			// look at the current libVirt configuration to find the available network properties
			try {
				if (connection != null) {
					Network network = connection.networkLookupByName(getDictionary().get("inet").toString());
					Builder parser = new Builder();
					Document doc = parser.build(network.getXMLDesc(0), null);
					Nodes ips = doc.query("/network/ip");
					for (int i = 0; i < ips.size(); i++) {
						String subnet = ((Element) ips.get(i)).getAttribute("address").getValue();
						String mask = ((Element) ips.get(i)).getAttribute("netmask").getValue();
						// convert mask such as 255.255.255.0 to something like 24
						SubnetUtils utils = new SubnetUtils();
						String[] net = utils.toCidrNotation(subnet, mask).split("/");

						kengine.addVariable("subnet", net[0]);
						kengine.addVariable("mask", net[1]);
						kengine.addVariable("nodeName", getName());
						kengine.append("updateDictionary {nodeName} { subnet = '{subnet}', mask = '{mask}' }");
					}
				}
			} catch (Throwable e) {
				logger.debug("Unable to get the network configuration of the libvirt system");
			}
			// TODO look at all the vms that are already defined and add them on the model
			updateModel(kengine);
			initialization = false;
		}
	}

	private void updateModel (KevScriptEngine kengine) {
		Boolean created = false;
		for (int i = 0; i < 20; i++) {
			try {
				kengine.atomicInterpretDeploy();
				created = true;
				break;
			} catch (Exception e) {
				logger.warn("Error while try to update the configuration of node {}, try number {}", new String[]{getName(), i + ""});
			}
		}
		if (!created) {
			logger.error("After 20 attempt, it was not able to update the configuration of {}", getName());
		}
	}
}

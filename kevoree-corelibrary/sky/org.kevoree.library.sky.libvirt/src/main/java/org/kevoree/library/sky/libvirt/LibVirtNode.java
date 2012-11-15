package org.kevoree.library.sky.libvirt;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeFragment;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.library.sky.api.nodeType.AbstractIaaSNode;
import org.kevoree.library.sky.api.nodeType.helper.SubnetUtils;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

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
public abstract class LibVirtNode extends AbstractIaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(LibVirtNode.class);
	Connect connection;

	private Timer timer;
	boolean initialization;

	@Override
	public void startNode () {
		super.startNode();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run () {
				synchronizeWithLibVirt();
			}
		}, 60000);
		initialization = true;
	}

	@Override
	public void stopNode () {
		super.stopNode();
		timer.cancel();
		timer.purge();
	}

	@Override
	public void modelUpdated () {
		super.modelUpdated();
		if (initialization) {
			try {
				// look at the current libVirt configuration to find the available network properties
				KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
				Network network = connection.networkLookupByName(getDictionary().get("inet").toString());
				Builder parser = new Builder();
				Document doc = parser.build(network.getXMLDesc(0), null);
				Nodes ips = doc.query("/network/ip");
				for (int i = 0; i < ips.size(); i++) {
					String subnet = ((Element) ips.get(i)).getAttribute("address").getValue();
					String mask = ((Element) ips.get(i)).getAttribute("netmask").getValue();
					SubnetUtils utils = new SubnetUtils();
					String[] net = utils.toCidrNotation(subnet, mask).split("/");

					kengine.addVariable("subnet", net[0]);
					kengine.addVariable("mask", net[1]);
					kengine.addVariable("nodeName", getName());
					kengine.append("updateDictionary {nodeName} { subnet = '{subnet}', mask = '{mask}' }");
				}
				updateModel(kengine);
			} catch (Throwable e) {
				logger.debug("Unable to get the domain's configuration or the network configuration of the libvirt node {}", getName(), e);
			}
			synchronizeWithLibVirt();
			initialization = false;
		}
	}


	public void synchronizeWithLibVirt () {
		try {
			if (connection != null) {
				logger.debug("Try to update the model according to the current libvirt configuration");
				KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
				Network network = connection.networkLookupByName(getDictionary().get("inet").toString());
				// look at all the vms that are already defined and add them on the model
				logger.debug("domains: {}", connection.listDomains());
				for (int domainId : connection.listDomains()) {
					Domain domain = connection.domainLookupByID(domainId);
					LibVirtReasoner.createNode(domain, kengine, LibVirtNode.this);
				}
				LibVirtReasoner.updateNetwork(network, kengine, LibVirtNode.this);
				updateModel(kengine);
			} else {
				logger.error("Unable to get the connection through the libvirt instance");
			}
		} catch (Throwable e) {
			logger.debug("Unable to get the domain's configuration or the network configuration of the libvirt node {}", getName(), e);
		}
	}
}

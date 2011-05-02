/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiperNetty.group;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.library.gossiperNetty.DataManager;
import org.kevoree.library.gossiperNetty.GossiperActor;
import org.kevoree.library.gossiperNetty.NettyGossipAbstractElement;
import org.kevoree.library.gossiperNetty.PeerSelector;
import org.kevoree.library.gossiperNetty.Serializer;
import org.kevoree.library.gossiperNetty.channel.NettyGossiperChannel;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Erwan Daubert
 */
@Library(name = "Kevoree-Android-JavaSE")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
		@DictionaryAttribute(name = "port", defaultValue = "9010", optional = true),
		@DictionaryAttribute(name = "FullUDP", defaultValue = "false", optional = true)
})
public class NettyGossiperGroup extends AbstractGroupType implements NettyGossipAbstractElement {

	protected DataManager dataManager = null;//new DataManager();
	protected GossiperActor actor = null;
	protected ServiceReference sr;
	protected KevoreeModelHandlerService modelHandlerService = null;
	protected PeerSelector selector = null;
	protected Logger logger = LoggerFactory.getLogger(NettyGossiperChannel.class);

	@Start
	public void startGossiperGroup() {
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
		modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);

		dataManager = new DataManagerForGroup(this.getName(), this.getNodeName(), modelHandlerService);

		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
		Serializer serializer = new GroupSerializer();
		selector = new GroupPeerSelector(timeoutLong, modelHandlerService, this.getName());
		actor = new GossiperActor(timeoutLong, this, dataManager, parsePortNumber(getNodeName()), parseFullUDPParameter(), false, serializer, selector);
	}

	@Stop
	public void stopGossiperGroup() {
		if (actor != null) {
			actor.stop();
			actor = null;
		}
		if (dataManager != null) {
			dataManager.stop();
		}
		if (modelHandlerService != null) {
			Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
			bundle.getBundleContext().ungetService(sr);
			modelHandlerService = null;
		}
	}

	@Update
	public void updateGossiperGroup() {
		stopGossiperGroup();
		startGossiperGroup();
	}

	@Override
	public List<String> getAllPeers() {
		ContainerRoot model = this.getModelService().getLastModel();
		//Group selfGroup = null;
		for (Object o : model.getGroups()) {
			Group g = (Group) o;
			if (g.getName().equals(this.getName())) {
				List<String> peers = new ArrayList<String>(g.getSubNodes().size());
				for (ContainerNode node : g.getSubNodes()) {
					peers.add(node.getName());
				}
				return peers;
			}
		}
		return new ArrayList<String>();
	}

	@Override
	public String getAddress(String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	private String name = "[A-Za-z0-9_]*";
	private String portNumber = "(65535|5[0-9]{4}|4[0-9]{4}|3[0-9]{4}|2[0-9]{4}|1[0-9]{4}|[0-9]{0,4})";
	private String separator = ",";
	private String affectation = "=";
	private String portPropertyRegex = "((" + name + affectation + portNumber + ")" + separator + ")*(" + name + affectation + portNumber + ")";

	@Override
	public int parsePortNumber(String nodeName) {
		String portProperty = this.getDictionary().get("port").toString();
		if (portProperty.matches(portPropertyRegex)) {
			String[] definitionParts = portProperty.split(separator);
			for (String part : definitionParts) {
				if (part.contains(nodeName + affectation)) {
					//System.out.println(Integer.parseInt(part.substring((nodeName + affectation).length(), part.length())));
					return Integer.parseInt(part.substring((nodeName + affectation).length(), part.length()));
				}
			}
		} else {
			return Integer.parseInt(portProperty);
		}
		return 0;
	}

	@Override
	public Boolean parseFullUDPParameter() {
		return this.getDictionary().get("FullUDP").toString().equals("true");
	}

	/*@Override
	public String selectPeer() {
		ContainerRoot model = this.getModelService().getLastModel();
		//Group selfGroup = null;
		for (Object o : model.getGroups()) {
			Group g = (Group) o;
			if (g.getName().equals(this.getName())) {
				int othersSize = g.getSubNodes().size();
				if (othersSize > 0) {
					SecureRandom diceRoller = new SecureRandom();
					int peerIndex = diceRoller.nextInt(othersSize);
					if (!g.getSubNodes().get(peerIndex).getName().equals(this.getNodeName())) {
						return g.getSubNodes().get(peerIndex).getName();
					} else if (peerIndex < othersSize -1) {
						return g.getSubNodes().get(peerIndex + 1).getName();
					} else if (peerIndex > 0) {
						return g.getSubNodes().get(peerIndex - 1).getName();
					}
				}
			}
		}
		return "";
	}*/

	@Override
	public void localNotification(Object data) {
		// NO OP
	}

	@Override
	public void triggerModelUpdate() {
		actor.notifyPeers();
	}
}

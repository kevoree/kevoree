/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.javase.gossiperNetty.group;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.library.javase.gossiperNetty.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erwan Daubert
 */
@Library(name = "Kevoree-Android-JavaSE")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
		@DictionaryAttribute(name = "port", defaultValue = "9010", optional = true),
		@DictionaryAttribute(name = "FullUDP", defaultValue = "false", optional = true),
		@DictionaryAttribute(name = "sendNotification", defaultValue = "true", optional = true),
		@DictionaryAttribute(name = "alwaysAskModel", defaultValue = "false", optional = true)
})
public class NettyGossiperGroup extends AbstractGroupType implements GossiperComponent {

	protected DataManagerForGroup dataManager;
	private Serializer serializer;
	protected GossiperActor actor;
	protected GroupScorePeerSelector selector;
	private ProcessValue processValue;
	private ProcessRequest processRequest;
	private NetworkProtocolSelector protocolSelector;
	private UDPActor udpActor;
	private TCPActor tcpActor;
	protected Logger logger = LoggerFactory.getLogger(NettyGossiperGroup.class);

	protected boolean sendNotification;

	@Start
	public void startGossiperGroup () {

		sendNotification = parseBooleanProperty("sendNotification");

		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));

		protocolSelector = new NetworkProtocolSelector();

		serializer = new GroupSerializer(this.getModelService());
		dataManager = new DataManagerForGroup(this.getName(), this.getNodeName(), this.getModelService());
		processValue = new ProcessValue(this, parseBooleanProperty("alwaysAskModel"), protocolSelector, dataManager,
				serializer, false);
		processRequest = new ProcessRequest(this, dataManager, serializer, processValue, protocolSelector);

		selector = new GroupScorePeerSelector(timeoutLong, this.getModelService(), this.getName());

		udpActor = new UDPActor(parsePortNumber(this.getNodeName()), processValue, processRequest);
		tcpActor = new TCPActor(parsePortNumber(this.getNodeName()), processValue, processRequest);

		protocolSelector.setProtocolForMetadata(udpActor);
		if (parseBooleanProperty("FullUDP")) {
			protocolSelector.setProtocolForData(udpActor);
		} else {
			protocolSelector.setProtocolForData(tcpActor);
		}

		logger.debug(this.getName() + ": initialize GossiperActor");

		actor = new GossiperActor(this, timeoutLong, selector, processValue);

		dataManager.start();
		processValue.start();
		processRequest.start();
		udpActor.start();
		tcpActor.start();
		selector.start();
		actor.start();
	}

	@Stop
	public void stopGossiperGroup () {
		if (actor != null) {
			actor.stop();
			actor = null;
		}
		if (selector != null) {
			selector.stop();
			selector = null;
		}
		if (udpActor != null) {
			udpActor.stop();
			udpActor = null;
		}
		if (tcpActor != null) {
			tcpActor.stop();
			tcpActor = null;
		}
		if (processValue != null) {
			processValue.stop();
			processValue = null;
		}
		if (processRequest != null) {
			processRequest.stop();
			processRequest = null;
		}
		if (dataManager != null) {
			dataManager.stop();
			dataManager = null;
		}
	}

	@Update
	public void updateGossiperGroup () {
		stopGossiperGroup();
		startGossiperGroup();
	}

	@Override
	public List<String> getAllPeers () {
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
	public String getAddress (String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	private String name = "[A-Za-z0-9_]*";
	private String portNumber = "(65535|5[0-9]{4}|4[0-9]{4}|3[0-9]{4}|2[0-9]{4}|1[0-9]{4}|[0-9]{0,4})";
	private String separator = ",";
	private String affectation = "=";
	private String portPropertyRegex =
			"((" + name + affectation + portNumber + ")" + separator + ")*(" + name + affectation + portNumber + ")";

	@Override
	public int parsePortNumber (String nodeName) {
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
	public Boolean parseBooleanProperty (String name) {
		return this.getDictionary().get(name) != null && this.getDictionary().get(name).toString().equals("true");
	}

	@Override
	public void localNotification (Object data) {
		// NO OP
	}

	@Override
	public void triggerModelUpdate () {
		if (sendNotification) {
			actor.notifyPeers();
		}
	}
}

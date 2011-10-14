package org.kevoree.library.javase.gossiperNetty.group;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeFragmentPropertyHelper;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.library.javase.gossiperNetty.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erwan Daubert
 */
@Library(name = "JavaSE")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
		@DictionaryAttribute(name = "port", defaultValue = "9010", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "FullUDP", defaultValue = "false", optional = true, vals= {"true", "false"}),
		@DictionaryAttribute(name = "sendNotification", defaultValue = "true", optional = true, vals= {"true", "false"}),
		@DictionaryAttribute(name = "alwaysAskModel", defaultValue = "false", optional = true, vals= {"true", "false"})
})
public class NettyGossiperGroup extends AbstractGroupType implements GossiperComponent {

	protected DataManagerForGroup dataManager;
	protected GossiperActor actor;
	protected GroupScorePeerSelector selector;
	private ProcessValue processValue;
	private ProcessRequest processRequest;
	private UDPActor udpActor;
	private TCPActor tcpActor;
	protected Logger logger = LoggerFactory.getLogger(NettyGossiperGroup.class);

	protected boolean sendNotification;

	@Start
	public void startGossiperGroup () {

		sendNotification = parseBooleanProperty("sendNotification");

		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));

		NetworkProtocolSelector protocolSelector = new NetworkProtocolSelector();

		Serializer serializer = new GroupSerializer(this.getModelService());
		dataManager = new DataManagerForGroup(this.getName(), this.getNodeName(), this.getModelService());
		processValue = new ProcessValue(this, parseBooleanProperty("alwaysAskModel"), protocolSelector, dataManager,
				serializer, false);
		processRequest = new ProcessRequest(this, dataManager, serializer, processValue, protocolSelector);

		selector = new GroupScorePeerSelector(timeoutLong, this.getModelService(), this.getName());

		udpActor = new UDPActor(parsePortNumber(), processValue, processRequest);
		tcpActor = new TCPActor(parsePortNumber(), processValue, processRequest);

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
		if (processRequest != null) {
			processRequest.stop();
			processRequest = null;
		}
		if (processValue != null) {
			processValue.stop();
			processValue = null;
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
		for (Object o : model.getGroupsForJ()) {
			Group g = (Group) o;
			if (g.getName().equals(this.getName())) {
				List<String> peers = new ArrayList<String>(g.getSubNodes().size());
				for (ContainerNode node : g.getSubNodesForJ()) {
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

	@Override
	public int parsePortNumber (String nodeName) {
		logger.debug("look for port on " + nodeName);
		return KevoreeFragmentPropertyHelper
				.getIntPropertyFromFragmentGroup(this.getModelService().getLastModel(), this.getName(), "port",
						nodeName);
	}

	private int parsePortNumber () {
		String portProperty = this.getDictionary().get("port").toString();
		try {
			return Integer.parseInt(portProperty);
		} catch (NumberFormatException e) {
			logger.warn("Invalid value for port parameter for " + this.getName() + " on " + this.getNodeName());
			return 0;
		}
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

	@Override
	public void push (ContainerRoot containerRoot, String s) {
	}

	@Override
	public ContainerRoot pull (String s) {
		return null;
	}
}

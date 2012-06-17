package org.kevoree.library.javase.gossiperNetty.group;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.library.javase.gossiperNetty.*;
import org.kevoree.library.javase.network.NodeNetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erwan Daubert
 */
@Library(name = "JavaSE")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
		@DictionaryAttribute(name = "gossip_port", defaultValue = "9010", optional = true, fragmentDependant = true),
				@DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "FullUDP", defaultValue = "false", optional = true, vals = {"true", "false"}),
		@DictionaryAttribute(name = "sendNotification", defaultValue = "true", optional = true, vals = {"true", "false"}),
		@DictionaryAttribute(name = "alwaysAskModel", defaultValue = "false", optional = true, vals = {"true", "false"}),
		@DictionaryAttribute(name = "mergeModel", defaultValue = "false", optional = true, vals = {"true", "false"})
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
	private boolean starting;

	private HTTPServer httpServer;
	private HTTPClient httpClient;

	@Start
	public void startGossiperGroup () {
		sendNotification = parseBooleanProperty("sendNotification");

		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
		boolean merge = "true".equalsIgnoreCase(this.getDictionary().get("mergeModel").toString());

		NetworkProtocolSelector protocolSelector = new NetworkProtocolSelector();

		Serializer serializer = new GroupSerializer(this.getModelService());
		dataManager = new DataManagerForGroup(this.getName(), this.getNodeName(), this.getModelService(), merge);
		processValue = new ProcessValue(this, parseBooleanProperty("alwaysAskModel"), protocolSelector, dataManager,
				serializer, false);
		processRequest = new ProcessRequest(this, dataManager, serializer, processValue, protocolSelector);

		selector = new GroupScorePeerSelector(timeoutLong, this.getModelService(), this.getNodeName());

		udpActor = new UDPActor(parsePortNumber(), processValue, processRequest);
		tcpActor = new TCPActor(parsePortNumber(), processValue, processRequest);

		protocolSelector.setProtocolForMetadata(udpActor);
		if (parseBooleanProperty("FullUDP")) {
			protocolSelector.setProtocolForData(udpActor);
		} else {
			protocolSelector.setProtocolForData(tcpActor);
		}

		logger.debug("{}: initialize GossiperActor", this.getName());

		actor = new GossiperActor(this, timeoutLong, selector, processValue);

		dataManager.start();
		processValue.start();
		processRequest.start();
		udpActor.start();
		tcpActor.start();
		selector.start();
		actor.start();
		starting = true;

		logger.debug("{}: starting HTTP server", this.getName());
		// starting HTTP server to accept http model update coming from the editor for example
		httpServer = new HTTPServer(this, Integer.parseInt(this.getDictionary().get("port").toString()));
		httpClient = new HTTPClient(this.getName());
		logger.debug("{}: HTTP server started", this.getName());

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
		if (httpServer != null) {
			httpServer.stop();
			httpServer = null;
		}

	}

	@Update
	public void updateGossiperGroup () {
		logger.info("try to update configuration of {}", this.getName());
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
		Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper
				.getStringNetworkProperties(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
		if (ipOption.isDefined()) {
			return ipOption.get();
		} else {
			return "127.0.0.1";
		}
	}

	@Override
	public int parsePortNumber (String nodeName) {
		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(this.getModelService().getLastModel(), this.getName(), "port", true, nodeName);
		if (portOption.isDefined()) {
			return portOption.get();
		} else {
			return 8000;
		}
	}

	private int parsePortNumber () {
		String portProperty = this.getDictionary().get("gossip_port").toString();
		try {
			return Integer.parseInt(portProperty);
		} catch (NumberFormatException e) {
			logger.warn("Invalid value for port parameter for {} on {}", this.getName(), this.getNodeName());
			return 0;
		}
	}

	@Override
	public Boolean parseBooleanProperty (String name) {
		return this.getDictionary().get(name) != null && "true".equals(this.getDictionary().get(name).toString());
	}

	@Override
	public void localNotification (Object data) {
		// NO OP
	}

	@Override
	public void triggerModelUpdate () {
		if (starting) {
			final Option<ContainerRoot> modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
			if (modelOption.isDefined()) {
				/*new Thread() {
					public void run () {*/
				getModelService().unregisterModelListener(getModelListener());
				getModelService().atomicUpdateModel(modelOption.get());
				getModelService().registerModelListener(getModelListener());
				/*	}
				}.start();*/
			}
			starting = false;
		} else {
			if (sendNotification) {
				actor.notifyPeers();
			}
		}

	}

	@Override
	public void push (ContainerRoot containerRoot, String s) throws Exception {
		if (httpClient == null) {
			httpClient = new HTTPClient(this.getName());
		}
		httpClient.push(containerRoot, s);
	}

	@Override
	public ContainerRoot pull (String s) throws Exception {
		if (httpClient == null) {
			httpClient = new HTTPClient(this.getName());
		}
		return httpClient.pull(this.getModelService().getLastModel(), s);
	}
}

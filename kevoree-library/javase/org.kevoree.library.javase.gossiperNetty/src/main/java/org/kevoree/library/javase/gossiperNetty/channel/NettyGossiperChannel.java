package org.kevoree.library.javase.gossiperNetty.channel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.kevoree.library.gossiperNetty.protocol.version.Version;
import org.kevoree.library.javase.gossiperNetty.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

/**
 * @author Erwan Daubert
 *         TODO add a DictionaryAttribute to define the number of uuids sent by response when a VectorClockUUIDsRequest is sent
 */
@Library(name = "JavaSE")
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
		@DictionaryAttribute(name = "port", defaultValue = "9000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "FullUDP", defaultValue = "true", optional = true, vals= {"true", "false"}),
		@DictionaryAttribute(name = "sendNotification", defaultValue = "false", optional = true, vals= {"true", "false"}),
		@DictionaryAttribute(name = "alwaysAskModel", defaultValue = "false", optional = true, vals= {"true", "false"})
})
@ChannelTypeFragment
public class NettyGossiperChannel extends AbstractChannelFragment implements GossiperComponent {

	private DataManagerForChannel dataManager;
	private Serializer serializer;
	private ChannelScorePeerSelector selector;
	private GossiperActor actor;
	private ProcessValue processValue;
	private ProcessRequest processRequest;
	private NetworkProtocolSelector protocolSelector;
	private UDPActor udpActor;
	private TCPActor tcpActor;
	protected boolean sendNotification;
	private Logger logger = LoggerFactory.getLogger(NettyGossiperChannel.class);

	@Start
	public void startGossiperChannel () {

		sendNotification = parseBooleanProperty("sendNotification");

		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));

		protocolSelector = new NetworkProtocolSelector();

		serializer = new ChannelSerializer();
		dataManager = new DataManagerForChannel(this, this.getNodeName()/*, this.getModelService()*/);
		processValue = new ProcessValue(this, parseBooleanProperty("alwaysAskModel"), protocolSelector, dataManager,
				serializer, true);
		processRequest = new ProcessRequest(this, dataManager, serializer, processValue, protocolSelector);

		selector = new ChannelScorePeerSelector(timeoutLong, this.getModelService(), this.getNodeName());

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
	public void stopGossiperChannel () {
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
	public void updateGossiperChannel () {
		// TODO use the garbage of the dataManager
		Map<UUID, Version.VectorClock> vectorClockUUIDs = dataManager.getUUIDVectorClocks();
		Map<UUID, Tuple2<Version.VectorClock, Object>> messages = new HashMap<UUID, Tuple2<Version.VectorClock, Object>>();
		for (UUID uuid : vectorClockUUIDs.keySet()) {
			messages.put(uuid, dataManager.getData(uuid));
		}

		stopGossiperChannel();
		startGossiperChannel();

		for (UUID uuid : messages.keySet()) {
			dataManager.setData(uuid, messages.get(uuid), "");
		}
	}

	@Override
	public Object dispatch (Message msg) {
		//Local delivery
		localNotification(msg);

		//CREATE NEW MESSAGE
		long timestamp = System.currentTimeMillis();
		UUID uuid = UUID.randomUUID();
		Tuple2<Version.VectorClock, Object> tuple = new Tuple2<Version.VectorClock, Object>(
				Version.VectorClock.newBuilder().
						addEnties(Version.ClockEntry.newBuilder().setNodeID(this.getNodeName())
								/*.setTimestamp(timestamp)*/.setVersion(2).build()).setTimestamp(timestamp).build(),
				msg);
		dataManager.setData(uuid, tuple, "");

		actor.notifyPeers();
		//SYNCHRONOUS NON IMPLEMENTED
		return null;
	}

	@Override
	public ChannelFragmentSender createSender (String remoteNodeName, String remoteChannelName) {
		return new NoopChannelFragmentSender();
	}

	@Override
	public void localNotification (Object o) {
		if (o instanceof Message) {
			for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
				forward(p, (Message) o);
			}
		}
	}

	@Override
	public List<String> getAllPeers () {
		List<String> peers = new ArrayList<String>();
		for (KevoreeChannelFragment fragment : getOtherFragments()) {
			peers.add(fragment.getNodeName());
		}
		return peers;
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
		return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port", nodeName);
	}

	public int parsePortNumber () {
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

	/*@Override
		 public String selectPeer() {
			 int othersSize = this.getOtherFragments().size();
			 if (othersSize > 0) {
				 SecureRandom diceRoller = new SecureRandom();
				 int peerIndex = diceRoller.nextInt(othersSize);
				 return this.getOtherFragments().get(peerIndex).getNodeName();
			 } else {
				 return "";
			 }
		 }*/
}

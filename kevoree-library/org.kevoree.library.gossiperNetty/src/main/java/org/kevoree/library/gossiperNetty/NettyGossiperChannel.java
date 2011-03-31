package org.kevoree.library.gossiperNetty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.version.Version.ClockEntry;
import org.kevoree.library.version.Version.VectorClock;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author Erwan Daubert
 * TODO add a DictionaryAttribute to define the number of uuids sent by response when a VectorClockUUIDsRequest is sent
 */
@Library(name = "Kevoree-Netty")
@DictionaryType({
	@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
	@DictionaryAttribute(name = "port", defaultValue = "9000", optional = true),
	@DictionaryAttribute(name = "FullUDP", defaultValue = "true", optional = true)
})
@ChannelTypeFragment
public class NettyGossiperChannel extends AbstractChannelFragment {

	private DataManager dataManager = null;//new DataManager();
	private GossiperActor actor = null;
	private ServiceReference sr;
	private KevoreeModelHandlerService modelHandlerService = null;
	private Logger logger = LoggerFactory.getLogger(NettyGossiperChannel.class);

	@Start
	public void startGossiperChannel() {
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
		modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);

		dataManager = new DataManager();

		actor = new GossiperActor(Long.parseLong(this.getDictionary().get("interval").toString()), this,
				dataManager,
				parsePortNumber(getNodeName()),
				parseFullUDPParameter());

		// TODO continue

		//START SERVER IF NECESSARY
	}

	@Stop
	public void stopGossiperChannel() {
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
	public void updateGossiperChannel() {
		/*stopGossiperChannel();
		startGossiperChannel();*/
	}

	@Override
	public Object dispatch(Message msg) {
		//Local delivery
		localDelivery(msg);

		//CREATE NEW MESSAGE
		long timestamp = System.currentTimeMillis();
		UUID uuid = UUID.randomUUID();
		Tuple2<VectorClock, Message> tuple = new Tuple2<VectorClock, Message>(VectorClock.newBuilder().
				addEnties(ClockEntry.newBuilder().setNodeID(this.getNodeName()).setTimestamp(timestamp).setVersion(2l).build()).setTimestamp(timestamp).build(), msg);
		dataManager.setData(uuid, tuple);

		actor.notifyPeers();
		//SYNCHRONOUS NON IMPLEMENTED
		return null;
	}

	@Override
	public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
		return new NoopChannelFragmentSender();
	}

	public void localDelivery(Message o) {
		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			forward(p, o);
		}
	}

	public List<String> getAllPeers() {
		List<String> peers = new ArrayList<String>();
		for (KevoreeChannelFragment fragment : getOtherFragments()) {
			peers.add(fragment.getNodeName());
		}
		return peers;
	}

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

	private boolean parseFullUDPParameter() {
		if (this.getDictionary().get("FullUDP").toString().equals("true")) {
			return true;
		}
		return false;

	}
}

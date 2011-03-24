/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.kevoree.annotation.ThirdParties;
import org.kevoree.annotation.ThirdParty;
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
 */
@Library(name = "Kevoree-Netty")
@DictionaryType({
	@DictionaryAttribute(name = "interval", defaultValue = "60000", optional = true),
	@DictionaryAttribute(name = "port", defaultValue = "9000", optional = true)
})
/*@ThirdParties({
    @ThirdParty(name="protobuf", url="wrap:mvn:com.google.protobuf/protobuf-java/2.3.0")
})*/
@ChannelTypeFragment
public class NettyGossiperChannel extends AbstractChannelFragment {

	private DataManager dataManager = null;//new DataManager();
	private GossiperActor actor = null;
	private ServiceReference sr;
	private KevoreeModelHandlerService modelHandlerService = null;
	//private static final String restBaseUrl = "gossipchannel";
	private Logger logger = LoggerFactory.getLogger(NettyGossiperChannel.class);

	@Start
	public void startGossiperChannel() {
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
		modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);

		actor = new GossiperActor(Long.parseLong(this.getDictionary().get("interval").toString()), this, 
				dataManager, 
				Integer.parseInt(this.getDictionary().get("port").toString()));

		// TODO continue

		//START SERVER IF NECESSARY
	}

	@Stop
	public void stopGossiperChannel() {
		actor.stop();
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		bundle.getBundleContext().ungetService(sr);
		modelHandlerService = null;
	}

	@Update
	public void updateGossiperChannel() {
		// TODO continue
	}

	/*public List<UUID> getUUIDS() {
	List<UUID> uuids = new java.util.ArrayList<UUID>();
	scala.collection.Iterator<UUID> it = clocksActor.getUUIDS().iterator();
	System.out.println("AFTER ACTOR");
	while (it.hasNext()) {
	uuids.add(it.next());
	}
	System.out.println("AFTER LOOP");
	return uuids;
	}*/

	/*public Tuple2<VectorClock, Object> getObject(UUID uuid) {
	return clocksActor.get(uuid);
	}*/
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
}

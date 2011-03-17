///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.kevoree.library.gossiperNetty;
//
//import java.security.SecureRandom;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.UUID;
//import java.util.concurrent.Semaphore;
//import org.kevoree.annotation.ChannelTypeFragment;
//import org.kevoree.annotation.DictionaryAttribute;
//import org.kevoree.annotation.DictionaryType;
//import org.kevoree.annotation.Library;
//import org.kevoree.annotation.Start;
//import org.kevoree.annotation.Stop;
//import org.kevoree.annotation.ThirdParties;
//import org.kevoree.annotation.ThirdParty;
//import org.kevoree.annotation.Update;
//import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
//import org.kevoree.framework.AbstractChannelFragment;
//import org.kevoree.framework.ChannelFragmentSender;
//import org.kevoree.framework.KevoreeChannelFragment;
//import org.kevoree.framework.KevoreePlatformHelper;
//import org.kevoree.framework.NoopChannelFragmentSender;
//import org.kevoree.framework.message.Message;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.ServiceReference;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import scala.Tuple2;
//
///**
// *
// * @author Erwan Daubert
// */
//@Library(name = "Kevoree-Android-JavaSE")
//@DictionaryType({
//	@DictionaryAttribute(name = "interval", defaultValue = "60000", optional = true)})
//@ChannelTypeFragment
//public class NettyGossiperChannel extends AbstractChannelFragment implements GossiperChannel {
//
//	GossiperChannelActor actor = null;
//	GossiperUUIDSVectorClockActor clocksActor = null;
//	private KevoreeModelHandlerService modelHandlerService = null;
//	private static final String restBaseUrl = "gossipchannel";
//	private Logger logger = LoggerFactory.getLogger(NettyGossiperChannel.class);
//
//	@Start
//	public void startGossiperChannel() {
//		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
//		ServiceReference sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
//		modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);
//		clocksActor = new GossiperUUIDSVectorClockActor();
//		actor = new GossiperChannelActor(this.getNodeName(), Long.parseLong(this.getDictionary().get("interval").toString()), this, clocksActor);
//                
//                GossiperChannelFragmentHandlerActor.setFragment(this.getName(), this);
//                
//                //START SERVER IF NECESSARY
//                
//                /*
//		try {
//			handlerAccess.acquire();
//			if (RestGossiperChannelFragmentResource.channels.keySet().isEmpty()) {
//				Handler.getDefaultHost().attach("/" + restBaseUrl + "/{channelName}/{uuid}", RestGossiperChannelFragmentResource.class);
//				//  Handler.getDefaultHost().attach("/restBaseUrl", RestGroupsResource.class);
//			}
//			RestGossiperChannelFragmentResource.channels.put(this.getName(), this);
//			handlerAccess.release();
//		} catch (InterruptedException ex) {
//			logger.error("GossipChannelStartError", ex);
//		}*/
//	}
//
//	@Stop
//	public void stopGossiperChannel() {
//		try {
//			actor.stop();
//			clocksActor.stop();
//		} catch (Exception ex) {
//			logger.error("GossipChannelStopError", ex);
//		}
//	}
//
//	@Update
//	public void updateGossiperChannel() {
//		/*
//		if (actor != null) {
//		actor.stop();
//		}
//		if (clocksActor != null) {
//		clocksActor.stop();
//		}
//		actor = null;
//		clocksActor = null;
//		clocksActor = new GossiperUUIDSVectorClockActor();
//		actor = new GossiperChannelActor(Long.parseLong(this.getDictionary().get("interval").toString()), this, clocksActor);
//		 * 
//		 */
//	}
//
//	public List<UUID> getUUIDS() {
//		List<UUID> uuids = new java.util.ArrayList<UUID>();
//		scala.collection.Iterator<UUID> it = clocksActor.getUUIDS().iterator();
//		System.out.println("AFTER ACTOR");
//		while (it.hasNext()) {
//			uuids.add(it.next());
//		}
//		System.out.println("AFTER LOOP");
//		return uuids;
//	}
//
//	public Tuple2<VectorClock, Object> getObject(UUID uuid) {
//		return clocksActor.get(uuid);
//	}
//
//	@Override
//	public Object dispatch(Message msg) {
//		//Local delivery
//		localDelivery(msg);
//		//CREATE NEW MESSAGE
//		UUID msgUUID = UUID.randomUUID();
//		Tuple2<VectorClock, Object> tuple = new Tuple2<VectorClock, Object>(VectorClock.newBuilder().
//				addEnties(ClockEntry.newBuilder().setNodeID(this.getNodeName()).setTimestamp(System.currentTimeMillis()).setVersion(2l).build()).setTimestamp(System.currentTimeMillis()).build(), msg);
//		clocksActor.swap(msgUUID, tuple);
//		actor.notifyPeers();
//		//SYNCHRONOUS NON IMPLEMENTED
//		return null;
//	}
//
//	@Override
//	public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
//		return new NoopChannelFragmentSender();
//	}
//
//	public void localDelivery(Message o) {
//		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
//			forward(p, o);
//		}
//	}
//
//	@Override
//	public VersionedModel getUUIDDataFromPeer(String targetNodeName, UUID uuid) {
//		String lastUrl = null;
//		try {
//			lastUrl = buildGroupURL(targetNodeName, this.getName());
//			lastUrl = lastUrl + "/" + uuid;
//			System.out.println("remote rest url =>" + lastUrl);
//			ClientResource remoteGroupResource = new ClientResource(lastUrl);
//			Representation result = remoteGroupResource.post(new EmptyRepresentation());
//			// byte[] modelB = IOUtils.readFully(result.getStream(), Integer.MAX_VALUE, true);
//			GossiperMessages.VersionedModel resModel = GossiperMessages.VersionedModel.parseFrom(result.getStream());
//			return resModel;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	@Override
//	public VectorClock getUUIDVectorClockFromPeer(String targetNodeName, UUID uuid) {
//		String lastUrl = null;
//		try {
//			lastUrl = buildGroupURL(targetNodeName, this.getName());
//			lastUrl = lastUrl + "/" + uuid;
//			System.out.println("remote rest url =>" + lastUrl);
//			ClientResource remoteGroupResource = new ClientResource(lastUrl);
//			Representation result = remoteGroupResource.get();
//			return GossiperMessages.VectorClock.parseFrom(result.getStream());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	@Override
//	public List<UUID> getMsgUUIDSFromPeer(String targetNodeName) {
//		String lastUrl = null;
//		try {
//			lastUrl = buildGroupURL(targetNodeName, this.getName());
//			lastUrl = lastUrl + "/all";
//			System.out.println("remote rest url =>" + lastUrl);
//			ClientResource remoteGroupResource = new ClientResource(lastUrl);
//			Representation result = remoteGroupResource.get();
//			VectorClockUUIDS uuidsMsg = GossiperMessages.VectorClockUUIDS.parseFrom(result.getStream());
//			List<UUID> results = new ArrayList<UUID>();
//			for (String uuid : uuidsMsg.getUuidsList()) {
//				results.add(UUID.fromString(uuid));
//			}
//			return results;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	protected String buildGroupURL(String remoteNodeName, String channelName) {
//		String ip = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
//		if (ip == null || ip.equals("")) {
//			ip = "127.0.0.1";
//		}
//		String port = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
//		if (port == null || port.equals("")) {
//			port = "8000";
//		}
//		return "http://" + ip + ":" + port + "/" + restBaseUrl + "/" + channelName;
//	}
//
//	@Override
//	public void notifyPeer(String nodeName) {
//		String url = "";
//		try {
//			url = buildGroupURL(nodeName, this.getName());
//			url = url + "/all";
//			ClientResource client = new ClientResource(url);
//			client.put(new StringRepresentation(this.getNodeName()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	public void notifyPeers() {
//		for (KevoreeChannelFragment cf : getOtherFragments()) {
//			String remoteNodeName = cf.getNodeName();
//			notifyPeer(remoteNodeName);
//		}
//	}
//
//	@Override
//	public String selectPeer() {
//		int othersSize = getOtherFragments().size();
//		Random diceRoller = new SecureRandom();
//		int peerIndex = diceRoller.nextInt(othersSize);
//		return getOtherFragments().get(peerIndex).getNodeName();
//	}
//
//	public void triggerGossipNotification(String nodeName) {
//		actor.scheduleGossip(nodeName);
//	}
//
//	@Override
//	public List<String> getAllPeers() {
//		List<String> peers = new ArrayList<String>();
//		for (KevoreeChannelFragment fragment : getOtherFragments()) {
//			peers.add(fragment.getNodeName());
//		}
//		return peers;
//	}
//}

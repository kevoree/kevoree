/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.gossiper.GossiperChannel;
import org.kevoree.library.gossiper.GossiperChannelActor;
import org.kevoree.library.gossiper.GossiperUUIDSVectorClockActor;
import org.kevoree.library.gossiper.version.GossiperMessages;
import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClockUUIDS;
import org.kevoree.library.gossiper.version.GossiperMessages.VersionedModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import scala.Tuple2;

/**
 *
 * @author ffouquet
 */
@Library(name = "Kevoree-Android-JavaSE")
@ThirdParties({
    @ThirdParty(name = "org.kevoree.extra.marshalling", url = "mvn:org.kevoree.extra/org.kevoree.extra.marshalling")
})
@DictionaryType({
    @DictionaryAttribute(name = "interval", defaultValue = "60000", optional = true)})
@ChannelTypeFragment
public class RestGossiperChannel extends AbstractChannelFragment implements GossiperChannel {

    GossiperChannelActor actor = null;
    GossiperUUIDSVectorClockActor clocksActor = null;
    private KevoreeModelHandlerService modelHandlerService = null;

    @Start
    public void startGossiperChannel() {
        Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        ServiceReference sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
        modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);

        clocksActor = new GossiperUUIDSVectorClockActor();
        actor = new GossiperChannelActor(Long.parseLong(this.getDictionary().get("interval").toString()), this, clocksActor);
    }

    @Stop
    public void stopGossiperChannel() {
        actor.stop();
    }

    @Update
    public void updateGossiperChannel() {
        if (actor != null) {
            actor.stop();
        }
        if (clocksActor != null) {
            clocksActor.stop();
        }
        actor = null;
        clocksActor = null;
        clocksActor = new GossiperUUIDSVectorClockActor();
        actor = new GossiperChannelActor(Long.parseLong(this.getDictionary().get("interval").toString()), this, clocksActor);
    }

    public List<UUID> getUUIDS() {
        List<UUID> uuids = new java.util.ArrayList<UUID>();
        scala.collection.Iterator<UUID> it = clocksActor.getUUIDS().iterator();
        while (it.hasNext()) {
            uuids.add(it.next());
        }
        return uuids;
    }

    public Tuple2<VectorClock, Object> getObject(UUID uuid) {
        return clocksActor.get(uuid);
    }

    @Override
    public Object dispatch(Message msg) {
        //Local delivery
        localDelivery(msg);
        //CREATE NEW MESSAGE
        UUID msgUUID = UUID.randomUUID();
        Tuple2<VectorClock, Object> tuple = new Tuple2<VectorClock, Object>(VectorClock.newBuilder().
                addEnties(ClockEntry.newBuilder().setNodeID(this.getNodeName()).setTimestamp(System.currentTimeMillis()).setVersion(2l)).setTimestamp(System.currentTimeMillis()).build(), msg);
        clocksActor.swap(msgUUID, tuple);
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

    @Override
    public VersionedModel getUUIDDataFromPeer(String targetNodeName, UUID uuid) {
        String lastUrl = null;
        try {
            lastUrl = buildGroupURL(targetNodeName, this.getName());
            lastUrl = lastUrl + "/" + uuid;
            System.out.println("remote rest url =>" + lastUrl);
            ClientResource remoteGroupResource = new ClientResource(lastUrl);
            Representation result = remoteGroupResource.post(new EmptyRepresentation());
            // byte[] modelB = IOUtils.readFully(result.getStream(), Integer.MAX_VALUE, true);
            GossiperMessages.VersionedModel resModel = GossiperMessages.VersionedModel.parseFrom(result.getStream());
            return resModel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VectorClock getUUIDVectorClockFromPeer(String targetNodeName, UUID uuid) {
        String lastUrl = null;
        try {
            lastUrl = buildGroupURL(targetNodeName, this.getName());
            lastUrl = lastUrl + "/" + uuid;
            System.out.println("remote rest url =>" + lastUrl);
            ClientResource remoteGroupResource = new ClientResource(lastUrl);
            Representation result = remoteGroupResource.get();
            return GossiperMessages.VectorClock.parseFrom(result.getStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UUID> getMsgUUIDSFromPeer(String targetNodeName) {
        String lastUrl = null;
        try {
            lastUrl = buildGroupURL(targetNodeName, this.getName());
            lastUrl = lastUrl + "/all";
            System.out.println("remote rest url =>" + lastUrl);
            ClientResource remoteGroupResource = new ClientResource(lastUrl);
            Representation result = remoteGroupResource.get();
            VectorClockUUIDS uuidsMsg = GossiperMessages.VectorClockUUIDS.parseFrom(result.getStream());
            List<UUID> results = new ArrayList<UUID>();
            for (String uuid : uuidsMsg.getUuidsList()) {
                results.add(UUID.fromString(uuid));
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String buildGroupURL(String remoteNodeName, String channelName) {
        String ip = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        String port = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
        if (port == null || port.equals("")) {
            port = "8000";
        }
        return "http://" + ip + ":" + port + "/channels/" + channelName;
    }

    @Override
    public void notifyPeer(String nodeName) {
        String url = "";
        try {
            url = buildGroupURL(nodeName, this.getName());
            ClientResource client = new ClientResource(url);
            client.put(new StringRepresentation(this.getNodeName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyPeers() {
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            String remoteNodeName = cf.getNodeName();
            notifyPeer(remoteNodeName);
        }
    }

    public void triggerGossipNotification(String nodeName) {
        actor.scheduleGossip(nodeName);
    }
}

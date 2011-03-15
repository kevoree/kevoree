package org.kevoree.library.gossiper.rest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.library.gossiper.GossipGroup;
import org.kevoree.library.gossiper.version.GossiperMessages;
import org.kevoree.remote.rest.Handler;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Library(name = "Kevoree-Android-JavaSE")
@GroupType
@DictionaryType({
    @DictionaryAttribute(name = "interval", defaultValue = "60000", optional = true)
})
public class RestGossipGroup extends GossipGroup {

    private static final Semaphore handlerAccess = new Semaphore(1, true);
    private static final Map<String, ClientResource> clients = Collections.synchronizedMap(new HashMap<String, ClientResource>());
    private Logger logger = LoggerFactory.getLogger(RestGossipGroup.class);

    //LIFE CYCLE GROUP
    @Start
    @Override
    public void startMyGroup() {
        super.startMyGroup();
        try {
            handlerAccess.acquire();
            if (RestGroupFragmentResource.groups.keySet().isEmpty()) {
                Handler.getDefaultHost().attach("/groups/{groupName}", RestGroupFragmentResource.class);
                Handler.getDefaultHost().attach("/groups", RestGroupsResource.class);
            }
            RestGroupFragmentResource.groups.put(this.getName(), this);
            handlerAccess.release();
        } catch (InterruptedException ex) {
            logger.error("GroupError", ex);
        }
    }

    @Stop
    @Override
    public void stopMyGroup() {
        try {
            handlerAccess.acquire();
            RestGroupFragmentResource.groups.remove(this.getName());
            if (RestGroupFragmentResource.groups.keySet().isEmpty()) {
                Handler.getDefaultHost().detach(RestGroupFragmentResource.class);
                Handler.getDefaultHost().detach(RestGroupsResource.class);
            }
            handlerAccess.release();
        } catch (InterruptedException ex) {
            logger.error("GroupError", ex);
        }
        clients.clear();
        super.stopMyGroup();
    }

    @Update
    @Override
    public void updateMyGroup() {
        //TODO CHECK DICTIONARY CHANGE
        super.updateMyGroup();
    }

    @Override
    public GossiperMessages.VectorClock getVectorFromPeer(String targetNodeName) {
        String lastUrl = null;
        try {
            lastUrl = buildGroupURL(targetNodeName, this.getName());
            System.out.println("remote rest url =>" + lastUrl);
            ClientResource remoteGroupResource = new ClientResource(lastUrl);
            Representation result = remoteGroupResource.get();
            return GossiperMessages.VectorClock.parseFrom(result.getStream());
        } catch (Exception e) {
            logger.debug("Fail to getVectorFromPeer via =>" + lastUrl);
        }
        return null;
    }

    @Override
    public GossiperMessages.VersionedModel getVersionnedModelToPeer(String targetNodeName) {
        String lastUrl = null;
        try {
            lastUrl = buildGroupURL(targetNodeName, this.getName());
            System.out.println("remote rest url =>" + lastUrl);
            ClientResource remoteGroupResource = new ClientResource(lastUrl);
            Representation result = remoteGroupResource.post(new EmptyRepresentation());
           // byte[] modelB = IOUtils.readFully(result.getStream(), Integer.MAX_VALUE, true);
            GossiperMessages.VersionedModel resModel = GossiperMessages.VersionedModel.parseFrom(result.getStream());
            return resModel;
        } catch (Exception e) {
            logger.debug("Fail to getVersionnedModelToPeer via =>" + lastUrl, e);
        }
        return null;
    }
    /*
    protected ClientResource getOrCreate(String url) {
    ClientResource res = null;
    if (clients.containsKey(url)) {
    res = clients.get(url);
    } else {
    res = new ClientResource(url);
    clients.put(url, res);
    }
    return res;
    }*/

    protected String buildGroupURL(String remoteNodeName, String groupName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        String port = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
        if (port == null || port.equals("")) {
            port = "8000";
        }
        return "http://" + ip + ":" + port + "/groups/" + groupName;
    }

    @Override
    public void notifyPeer(String nodeName) {
        String url = "";
        try {
            url = buildGroupURL(nodeName, this.getName());
            ClientResource client = new ClientResource(url);
            client.put(new StringRepresentation(this.getNodeName()));
        } catch (Exception e) {
            logger.debug("Fail to send gossip group notification via =>" + url);
        }
    }
}

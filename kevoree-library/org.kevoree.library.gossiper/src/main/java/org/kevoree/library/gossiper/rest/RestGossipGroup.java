package org.kevoree.library.gossiper.rest;

import org.kevoree.ContainerNode;
import org.kevoree.annotation.GroupType;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.library.gossiper.GossipGroup;
import org.kevoree.library.gossiper.version.GossiperMessages;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

//@GroupType
public class RestGossipGroup extends GossipGroup {


    protected Boolean pushVersionnedModelToPeer(ContainerNode node, GossiperMessages.VersionedModel model) {
        String lastUrl = null;
        try {

            lastUrl = buildGroupURL(node.getName(), this.getName());
            System.out.println("remote rest url =>" + lastUrl);
            ClientResource remoteGroupResource = new ClientResource(lastUrl);
            //TODO ADD COMPRESSION GZIP
            Representation representation = new StringRepresentation(model.toByteString().toStringUtf8(), MediaType.TEXT_PLAIN);
            representation.setCharacterSet(CharacterSet.UTF_8);
            Representation result = remoteGroupResource.put(representation);
            return result.isAvailable() ;
        } catch (Exception e) {
            System.err.println("Fail to send to remote channel via =>" + lastUrl);
            System.err.println("Reply not implemented => message lost !!!");
        }
        return false;
    }


    protected String buildGroupURL(String remoteNodeName, String groupName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null) {
            ip = "127.0.0.1";
        }
        String port = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
        if (port == null) {
            port = "8000";
        }
        return "http://" + ip + ":" + port + "/groups/" + groupName;
    }


}

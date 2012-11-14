package org.kevoree.library.javase.basicGossiper.group;

import jexxus.common.Connection;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.library.BasicGroup;
import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage;
import org.kevoree.library.javase.NetworkSender;
import org.kevoree.library.javase.basicGossiper.*;
import org.kevoree.library.javase.network.NodeNetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Erwan Daubert
 */
@Library(name = "JavaSE")
@GroupType
@DictionaryType({
        @DictionaryAttribute(name = "interval", defaultValue = "3000", optional = true),
        @DictionaryAttribute(name = "alwaysAskModel", defaultValue = "false", optional = true, vals = {"true", "false"}),
        @DictionaryAttribute(name = "mergeModel", defaultValue = "false", optional = true, vals = {"true", "false"})
})
public class BasicGossiperGroup extends BasicGroup implements GossiperComponent {

    protected DataManagerForGroup dataManager;
    protected GossiperActor actor;
    protected GroupScorePeerSelector selector;
    private GossiperProcess processValue;
    protected Logger logger = LoggerFactory.getLogger(BasicGossiperGroup.class);
    private boolean starting;

    @Start
    public void startGossiperGroup() throws IOException {
        udp = true;
        super.startRestGroup();

        Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
        boolean merge = "true".equalsIgnoreCase(this.getDictionary().get("mergeModel").toString());
        NetworkSender sender = new NetworkSender();
        Serializer serializer = new GroupSerializer(this.getModelService());
        dataManager = new DataManagerForGroup(this.getName(), this.getNodeName(), this.getModelService(), merge);
        processValue = new GossiperProcess(this, parseBooleanProperty("alwaysAskModel"), sender, dataManager,
                serializer, false);

        selector = new GroupScorePeerSelector(timeoutLong, this.getModelService(), this.getNodeName());
        logger.debug("{}: initialize GossiperActor", this.getName());
        actor = new GossiperActor(this, timeoutLong, selector, processValue);
        dataManager.start();
        processValue.start();
        selector.start();
        actor.start();
        starting = true;
    }

    protected void externalProcess(byte[] data, Connection from){
        try {
            ByteArrayInputStream stin = new ByteArrayInputStream(data);
            stin.read();
            KevoreeMessage.Message msg = KevoreeMessage.Message.parseFrom(stin);
            processValue.receiveRequest(msg);
        } catch (Exception e) {
            logger.error("",e);
        } finally {
           // from.close();
        }
    }

    @Stop
    public void stopGossiperGroup() {
        super.stopRestGroup();
        if (actor != null) {
            actor.stop();
            actor = null;
        }
        if (selector != null) {
            selector.stop();
            selector = null;
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
    public void updateGossiperGroup() throws IOException {
        logger.info("try to update configuration of {}", this.getName());
        stopGossiperGroup();
        startGossiperGroup();
    }

    @Override
    public List<String> getAllPeers() {
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
    public String getAddress(String remoteNodeName) {
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper
                .getStringNetworkProperties(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            return ipOption.get();
        } else {
            return "127.0.0.1";
        }
    }

    @Override
    public int parsePortNumber(String nodeName) {
        Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(this.getModelService().getLastModel(), this.getName(), "port", true, nodeName);
        if (portOption.isDefined()) {
            return portOption.get();
        } else {
            return 8000;
        }
    }

    private int parsePortNumber() {
        String portProperty = this.getDictionary().get("gossip_port").toString();
        try {
            return Integer.parseInt(portProperty);
        } catch (NumberFormatException e) {
            logger.warn("Invalid value for port parameter for {} on {}", this.getName(), this.getNodeName());
            return 0;
        }
    }

    @Override
    public Boolean parseBooleanProperty(String name) {
        return this.getDictionary().get(name) != null && "true".equals(this.getDictionary().get(name).toString());
    }

    @Override
    public void localNotification(Object data) {
        // NO OP
    }

    @Override
    public void triggerModelUpdate() {
        if (starting) {
            final Option<ContainerRoot> modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
            if (modelOption.isDefined()) {
                getModelService().unregisterModelListener(this);
                getModelService().atomicUpdateModel(modelOption.get());
                getModelService().registerModelListener(this);
            }
            starting = false;
        } else {
            actor.notifyPeers();
        }

    }


}

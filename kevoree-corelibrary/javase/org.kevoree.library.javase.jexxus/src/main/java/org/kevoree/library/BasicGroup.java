package org.kevoree.library;

import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.NetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Exchanger;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 07/11/12
 * Time: 17:24
 */
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ssl", defaultValue = "true", vals = {"true", "false"})
})
@GroupType
@Library(name = "JavaSE", names = "Android")
public class BasicGroup extends AbstractGroupType implements ConnectionListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte getModel = 0;
    private final byte pushModel = 1;

    private Server server = null;
    private boolean starting;


    @Start
    public void startRestGroup() throws IOException {
        int port = Integer.parseInt(this.getDictionary().get("port").toString());
        boolean ssl = Boolean.parseBoolean(this.getDictionary().get("ssl").toString());
        server = new Server(this, port, ssl);
        logger.info("BasicGroup listen on " + port + "-SSL=" + ssl);
        server.startServer();
        starting = true;
    }

    @Stop
    public void stopRestGroup() {
        server.shutdown();
    }

    @Override
    public void triggerModelUpdate () {
        if (starting) {
            final ContainerRoot modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
            if (modelOption != null) {
                new Thread() {
                    public void run () {
                        getModelService().unregisterModelListener(BasicGroup.this);
                        getModelService().atomicUpdateModel(modelOption);
                        getModelService().registerModelListener(BasicGroup.this);
                    }
                }.start();
            }
            starting = false;
        } else {
            Group group = getModelElement();
            for (ContainerNode subNode : group.getSubNodesForJ()) {
                if (!subNode.getName().equals(this.getNodeName())) {
                    try {
                        push(getModelService().getLastModel(), subNode.getName());
                    } catch (Exception e) {
                        logger.warn("Unable to notify other members of {} group", group.getName());
                    }
                }
            }
        }
    }

    @Override
    public void push(ContainerRoot model, String targetNodeName) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(pushModel);
        KevoreeXmiHelper.saveCompressedStream(output, model);
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper
                .getStringNetworkProperties(this.getModelService().getLastModel(), targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, this.getName(), "port", true, targetNodeName);
        int PORT = 8000;
        if (portOption.isDefined()) {
            PORT = portOption.get();
        }
        final ClientConnection[] conns = new ClientConnection[1];
        conns[0] = new ClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
            }

            @Override
            public void receive(byte[] data, Connection from) {
            }

            @Override
            public void clientConnected(ServerConnection conn) {
            }
        }, ip, PORT, true);
        conns[0].connect();
        conns[0].send(output.toByteArray(), Delivery.RELIABLE);
    }

    @Override
    public ContainerRoot pull(final String targetNodeName) throws Exception {
        final Exchanger<ContainerRoot> exchanger = new Exchanger<ContainerRoot>();
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper
                .getStringNetworkProperties(this.getModelService().getLastModel(), targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), this.getName(), "port", true, targetNodeName);
        int PORT = 8000;
        if (portOption.isDefined()) {
            PORT = portOption.get();
        }
        final ClientConnection[] conns = new ClientConnection[1];
        conns[0] = new ClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
                conns[0].close();
            }

            @Override
            public void receive(byte[] data, Connection from) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                final ContainerRoot root = KevoreeXmiHelper.loadCompressedStream(inputStream);
                try {
                    exchanger.exchange(root);
                } catch (InterruptedException e) {
                    logger.error("error while waiting model from "+targetNodeName,e);
                } finally {
                    conns[0].close();
                }
            }

            @Override
            public void clientConnected(ServerConnection conn) {
            }

        }, ip, PORT, true);
        conns[0].connect();
        byte[] data = new byte[1];
        data[0] = getModel;
        conns[0].send(data, Delivery.RELIABLE);
        return exchanger.exchange(null);
    }

    @Override
    public void connectionBroken(Connection broken, boolean forced) {
    }

    @Override
    public void receive(byte[] data, Connection from) {
        try {
            if (data == null) {
                logger.error("Null rec");
                return;
            } else {
                switch (data[0]){
                    case getModel : {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        KevoreeXmiHelper.saveCompressedStream(output, getModelService().getLastModel());
                        from.send(output.toByteArray(), Delivery.RELIABLE);
                    } break;
                    case pushModel : {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                        inputStream.read();
                        final ContainerRoot root = KevoreeXmiHelper.loadCompressedStream(inputStream);
                        new Thread() {
                            public void run() {
                                getModelService().unregisterModelListener(BasicGroup.this);
                                getModelService().atomicUpdateModel(root);
                                getModelService().registerModelListener(BasicGroup.this);
                            }
                        }.start();
                        from.close();
                    } break;
                    default : from.close();
                }
            }
        } catch (Exception e) {
            logger.error("Something bad ...", e);
        }

    }

    @Override
    public void clientConnected(ServerConnection conn) {

    }

}

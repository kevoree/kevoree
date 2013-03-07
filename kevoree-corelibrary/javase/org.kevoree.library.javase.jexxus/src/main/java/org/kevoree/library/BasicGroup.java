package org.kevoree.library;

import jexxus.client.ClientConnection;
import jexxus.client.UniClientConnection;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 07/11/12
 * Time: 17:24
 */
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ssl", defaultValue = "false", vals = {"true", "false"})
})
@GroupType
@Library(name = "JavaSE", names = "Android")
public class BasicGroup extends AbstractGroupType implements ConnectionListener {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte getModel = 0;
    private final byte pushModel = 1;

    protected Server server = null;
    private boolean starting;
    protected boolean udp = false;
    boolean ssl = false;
    protected int port = -1;


    @Start
    public void startRestGroup() throws IOException {
        port = Integer.parseInt(this.getDictionary().get("port").toString());
        ssl = Boolean.parseBoolean(this.getDictionary().get("ssl").toString());
        if (udp) {
            server = new Server(this, port, port, ssl);
        } else {
            server = new Server(this, port, ssl);
        }
        logger.info("BasicGroup listen on " + port + "-SSL=" + ssl);
        server.startServer();
        starting = true;
    }

    @Stop
    public void stopRestGroup() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Update
    public void updateRestGroup() throws IOException {
        if (port != Integer.parseInt(this.getDictionary().get("port").toString())) {
            stopRestGroup();
            startRestGroup();
        }
    }


    protected void locaUpdateModel(final ContainerRoot modelOption) {
        new Thread() {
            public void run() {
                getModelService().unregisterModelListener(BasicGroup.this);
                getModelService().atomicUpdateModel(modelOption);
                getModelService().registerModelListener(BasicGroup.this);
            }
        }.start();
    }

    @Override
    public void triggerModelUpdate() {
        if (starting) {
            final ContainerRoot modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
            if (modelOption != null) {
                new Thread() {
                    public void run() {
                        try {
                            getModelService().unregisterModelListener(BasicGroup.this);
                            getModelService().atomicUpdateModel(modelOption);
                            getModelService().registerModelListener(BasicGroup.this);
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }.start();
            }
            starting = false;
        } else {
            Group group = getModelElement();
            ContainerRoot currentModel = (ContainerRoot) group.eContainer();
            for (ContainerNode subNode : group.getSubNodes()) {
                if (!subNode.getName().equals(this.getNodeName())) {
                    try {
                        push(currentModel, subNode.getName());
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
        KevoreeXmiHelper.$instance.saveCompressedStream(output, model);
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        } else {
            logger.warn("No addr, found default local");
        }

        int PORT = 8000;
        Group groupOption = model.findByPath("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            if (portOption.isDefined()) {
                try {
                PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e){
                    logger.warn("Attribute \"port\" of {} must be an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
        }
        final UniClientConnection[] conns = new UniClientConnection[1];
        conns[0] = new UniClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
            }

            @Override
            public void receive(byte[] data, Connection from) {
            }

            @Override
            public void clientConnected(ServerConnection conn) {
            }
        }, ip, PORT, ssl);
        conns[0].connect(5000);
        conns[0].send(output.toByteArray(), Delivery.RELIABLE);
    }

    @Override
    public ContainerRoot pull(final String targetNodeName) throws Exception {
        ContainerRoot model = getModelService().getLastModel();
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        int PORT = 8000;
        Group groupOption = model.findByPath("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            if (portOption.isDefined()) {
                try {
                    PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e){
                    logger.warn("Attribute \"port\" of {} must be an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
        }
        return requestModel(ip, PORT, targetNodeName);
    }

    protected ContainerRoot requestModel(String ip, int port, final String targetNodeName) throws IOException, TimeoutException, InterruptedException {
        final Exchanger<ContainerRoot> exchanger = new Exchanger<ContainerRoot>();
        final ClientConnection[] conns = new ClientConnection[1];
        conns[0] = new ClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
                conns[0].close();
                try {
                    exchanger.exchange(null);
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
            }

            @Override
            public void receive(byte[] data, Connection from) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                final ContainerRoot root = KevoreeXmiHelper.$instance.loadCompressedStream(inputStream);
                try {
                    exchanger.exchange(root);
                } catch (InterruptedException e) {
                    logger.error("error while waiting model from " + targetNodeName, e);
                } finally {
                    conns[0].close();
                }
            }

            @Override
            public void clientConnected(ServerConnection conn) {
            }

        }, ip, port, ssl);
        conns[0].connect(5000);
        byte[] data = new byte[1];
        data[0] = getModel;
        conns[0].send(data, Delivery.RELIABLE);
        return exchanger.exchange(null, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void connectionBroken(Connection broken, boolean forced) {
    }

    @Override
    public void receive(byte[] data, Connection from) {
        try {
            if (data == null) {
                logger.error("Null rec");
            } else {
                switch (data[0]) {
                    case getModel: {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();

                        //System.err.println(KevoreeXmiHelper.$instance.saveToString(getModelService().getLastModel(),true));

                        KevoreeXmiHelper.$instance.saveCompressedStream(output, getModelService().getLastModel());
                        from.send(output.toByteArray(), Delivery.RELIABLE);
                    }
                    break;
                    case pushModel: {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                        inputStream.read();
                        final ContainerRoot root = KevoreeXmiHelper.$instance.loadCompressedStream(inputStream);
                        locaUpdateModel(root);
                        //from.close();
                    }
                    break;
                    default:
                        externalProcess(data, from);
                }
            }
        } catch (Exception e) {
            logger.error("Something bad ...", e);
        }

    }

    protected void externalProcess(byte[] data, Connection from) {
        from.close();
    }


    @Override
    public void clientConnected(ServerConnection conn) {

    }

}

package org.kevoree.library.nioChannel;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.OSGIObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.osgi.framework.Bundle;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "9000", optional = true, fragmentDependant = true)
}
)
public class NioChannel extends AbstractChannelFragment {

    private ServerBootstrap bootstrap;
    private ClientBootstrap clientBootStrap;
    private Channel serverChannel;

    public MessageQueue getMsgQueue() {
        return msgQueue;
    }

    private MessageQueue msgQueue = null;
    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


    @Start
    public void startNio() {
        
        final Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        
        msgQueue = new MessageQueue();
        msgQueue.start();
        final NioChannel selfPointer = this;

        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new ObjectEncoder(), new OSGIObjectDecoder(bundle), new NioServerHandler(selfPointer));
            }
        });

        // Bind and start to accept incoming connections.
        serverChannel = bootstrap.bind(new InetSocketAddress(Integer.parseInt(getDictionary().get("port").toString())));

        clientBootStrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        clientBootStrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new OSGIObjectDecoder(bundle),
                        new NioClientHandler(selfPointer));
            }
        });

    }

    @Stop
    public void stopNio() {
        serverChannel.close().awaitUninterruptibly(200);
        bootstrap.releaseExternalResources();
        clientBootStrap.releaseExternalResources();
        msgQueue.stop();
    }

    @Update
    public void update() {
        stopNio();
        startNio();
    }


    @Override
    public Object dispatch(Message message) {


        if (!message.getPassedNodes().contains(getNodeName())) {
            message.getPassedNodes().add(getNodeName());
        }

        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, message);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!message.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, message);
            }
        }
        return null;
    }


    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, final String remoteChannelName) {
        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message message) {
                try {
                    msgQueue.putMsg(getAddress(remoteNodeName), parsePortNumber(remoteNodeName) + "", message);
                    clientBootStrap.connect(new InetSocketAddress(getAddress(remoteNodeName),parsePortNumber(remoteNodeName)));
                } catch (IOException e) {
                    logger.error("Error while sending message to "+remoteNodeName+"-"+remoteChannelName);
                }
                return null;
            }
        };
    }

    public String getAddress(String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public int parsePortNumber(String nodeName) throws IOException {
        try {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper
                    .getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
                            nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }


}

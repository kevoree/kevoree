package org.kevoree.library.nioChannel;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.KevoreeObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        @DictionaryAttribute(name = "port", defaultValue = "9000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "type", defaultValue = "nio", optional = true, vals = {"nio","oio"})
})
public class NioChannel extends AbstractChannelFragment {

    private ServerBootstrap bootstrap;
    private ClientBootstrap clientBootStrap;
    private Channel serverChannel;

    public MessageQueue getMsgQueue() {
        return msgQueue;
    }

    private MessageQueue msgQueue = null;
    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    public Set<Channel> serverConnectedChannel ;

    @Start
    public void startNio() {

        serverConnectedChannel =  Collections.synchronizedSet(new HashSet<Channel>());

        final NioChannel selfPointer = this;

        if(this.getDictionary().get("type").equals("nio")){
            bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool()));
        } else {
            bootstrap = new ServerBootstrap(
                    new OioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool()));
        }



        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new ObjectEncoder(), new org.jboss.netty.handler.codec.serialization.KevoreeBindingObjectDecoder(selfPointer), new NioServerHandler(selfPointer));
            }
        });

        if(this.getDictionary().get("type").equals("nio")){
            clientBootStrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        } else {
            clientBootStrap = new ClientBootstrap(new OioClientSocketChannelFactory(Executors.newCachedThreadPool()));
        }

        clientBootStrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new KevoreeObjectDecoder(NioChannel.this.getClass().getClassLoader(), 2548576),
                        new NioClientHandler(selfPointer));
            }
        });

        msgQueue = new MessageQueue(clientBootStrap);
        msgQueue.start();

        // Bind and start to accept incoming connections.
        serverChannel = bootstrap.bind(new InetSocketAddress(Integer.parseInt(getDictionary().get("port").toString())));

    }

    @Stop
    public void stopNio() {
        msgQueue.flushChannel();
        clientBootStrap.releaseExternalResources();
        logger.debug("Client channels flushed");
        msgQueue.stop();

        //CLOSE ALREADY CONNECTED CLIENT
        for(Channel c :  serverConnectedChannel){
           c.close().awaitUninterruptibly(200);
        }
        serverConnectedChannel.clear();

        serverChannel.close().awaitUninterruptibly(200);
        logger.debug("Server channel closed");
     //   bootstrap.releaseExternalResources();

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
                    msgQueue.putMsg(getAddress(remoteNodeName), parsePortNumber(remoteNodeName), message);
                    //    clientBootStrap.connect(new InetSocketAddress(getAddress(remoteNodeName),parsePortNumber(remoteNodeName)));
                } catch (IOException e) {
                    logger.error("Error while sending message to " + remoteNodeName + "-" + remoteChannelName);
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

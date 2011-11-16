package org.kevoree.library.nioChannel;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;

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
    private Channel serverChannel;

    @Start
    public void startNio() {
        final NioChannel selfPointer = this;

        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new ObjectEncoder(), new ObjectDecoder(), new NioServerHandler(selfPointer));
            }
        });

        // Bind and start to accept incoming connections.
        serverChannel = bootstrap.bind(new InetSocketAddress(Integer.parseInt(getDictionary().get("port").toString())));
    }

    @Stop
    public void stopNio() {
        serverChannel.close().awaitUninterruptibly(200);
        bootstrap.releaseExternalResources();
    }

    @Update
    public void update() {
        stopNio();
        startNio();
    }


    @Override
    public Object dispatch(Message message) {
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, message);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!message.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, message);
            }
        }
        return null;    }


    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}

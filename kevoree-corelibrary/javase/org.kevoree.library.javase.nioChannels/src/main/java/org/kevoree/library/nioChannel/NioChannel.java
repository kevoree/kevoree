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
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.framework.message.Message;
import org.slf4j.LoggerFactory;
import scala.Option;

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
 */

@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "9000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "type", defaultValue = "nio", optional = true, vals = {"nio", "oio"})
})
public class NioChannel extends AbstractChannelFragment {

	private ServerBootstrap bootstrap;
	private ClientBootstrap clientBootStrap;
	private Channel serverChannel;
    private int port;
    private String type;

	public MessageQueue getMsgQueue () {
		return msgQueue;
	}

	private MessageQueue msgQueue = null;
	private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
	public Set<Channel> serverConnectedChannel;

	@Start
	public void startNio () {

		serverConnectedChannel = Collections.synchronizedSet(new HashSet<Channel>());

		final NioChannel selfPointer = this;
        type = this.getDictionary().get("type").toString();

		if ("nio".equals(type)) {
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
			public ChannelPipeline getPipeline () throws Exception {
				return Channels.pipeline(new ObjectEncoder(), new ObjectDecoder(new ChannelClassResolver(selfPointer)), new NioServerHandler(selfPointer));
			}
		});

		if ("nio".equals(type)) {
			clientBootStrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		} else {
			clientBootStrap = new ClientBootstrap(new OioClientSocketChannelFactory(Executors.newCachedThreadPool()));
		}

		clientBootStrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline () throws Exception {
				return Channels.pipeline(
						new ObjectEncoder(),
						new ObjectDecoder(new ChannelClassResolver(selfPointer)),
						new NioClientHandler(selfPointer));
			}
		});

		msgQueue = new MessageQueue(clientBootStrap);
		msgQueue.start();

		// Bind and start to accept incoming connections.
        port = Integer.parseInt(getDictionary().get("port").toString());
		serverChannel = bootstrap.bind(new InetSocketAddress(port));

	}

	@Stop
	public void stopNio () {
		msgQueue.flushChannel();

		//bootstrap.releaseExternalResources();
		logger.debug("Client channels flushed");
		msgQueue.stop();

		//CLOSE ALREADY CONNECTED CLIENT
		for (Channel c : serverConnectedChannel) {
			c.close().awaitUninterruptibly(500);
		}
		serverConnectedChannel.clear();

		serverChannel.close().awaitUninterruptibly(500);
		logger.debug("Server channel closed");
		clientBootStrap.releaseExternalResources();
		bootstrap.releaseExternalResources();

	}

	@Update
	public void update () {
        if (!getDictionary().get("port").toString().equals(port + "") || !getDictionary().get("type").toString().equals(type)) {
		    stopNio();
		    startNio();
        }
	}


	@Override
	public Object dispatch (Message message) {
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
	public ChannelFragmentSender createSender (final String remoteNodeName, final String remoteChannelName) {
		return new ChannelFragmentSender() {
			@Override
			public Object sendMessageToRemote (Message message) {
				try {
					if (!message.getPassedNodes().contains(getNodeName())) {
						message.getPassedNodes().add(getNodeName());
					}
					msgQueue.putMsg(getAddress(remoteNodeName), parsePortNumber(remoteNodeName), message);
					//    clientBootStrap.connect(new InetSocketAddress(getAddress(remoteNodeName),parsePortNumber(remoteNodeName)));
				} catch (IOException e) {
					logger.debug("Error while sending message to " + remoteNodeName + "-" + remoteChannelName);
				}
				return null;
			}
		};
	}

	public String getAddress (String remoteNodeName) {
		String ip = "127.0.0.1";
		Option<String> ipOption = NetworkHelper.getAccessibleIP(org.kevoree.framework.KevoreePropertyHelper
                .getNetworkProperties(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
		if (ipOption.isDefined()) {
			ip = ipOption.get();
		}
//		List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(this.getModelService().getLastModel(), remoteNodeName,
//				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
//
//		String ip = "";
//		for (String loopIP : ips) {
//			if (loopIP.split(".").length == 4) {
//				ip = loopIP;
//			}
//		}
//
//
//		//String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
//		//        org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
//		if ("".equals(ip)) {
//			ip = "127.0.0.1";
//		}
		return ip;
	}

	public int parsePortNumber (String nodeName) throws IOException {
		try {
			//logger.debug("look for port on " + nodeName);
			Option<String> portOption = org.kevoree.framework.KevoreePropertyHelper.getProperty(getModelElement(), "port", true, nodeName);
			if (portOption.isDefined()) {
                try {
				return Integer.parseInt(portOption.get());
                } catch (NumberFormatException e) {
                    logger.warn("Attribute \"port\" of {} is not an Integer", getName());
                    return 0;
                }
			} else {
				return 9000;
			}
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}


}

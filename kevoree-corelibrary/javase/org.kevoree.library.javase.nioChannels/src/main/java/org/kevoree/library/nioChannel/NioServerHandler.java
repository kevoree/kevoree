package org.kevoree.library.nioChannel;

import org.jboss.netty.channel.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 16:36
 */
public class NioServerHandler extends SimpleChannelUpstreamHandler {

    private NioChannel parentChannel;

    public NioServerHandler(NioChannel p) {
        parentChannel = p;
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

        //System.out.println("Remote Message Arrive !!!!");

        if (parentChannel.serverConnectedChannel.contains(e.getChannel())) {
            parentChannel.serverConnectedChannel.add(e.getChannel());
            e.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    parentChannel.serverConnectedChannel.remove(channelFuture.getChannel());
                }
            });
        }
        
        Message msg = (Message) e.getMessage();
        if (!msg.getPassedNodes().contains(parentChannel.getNodeName())) {
            msg.getPassedNodes().add(parentChannel.getNodeName());
        }

        //System.out.println("Remote Message Arrive "+msg);

        parentChannel.remoteDispatch(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.debug("Error while processing message ", e.getCause());
    }


}

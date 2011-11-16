package org.kevoree.library.nioChannel;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class NioServerHandler extends SimpleChannelUpstreamHandler {

    private NioChannel parentChannel;

    public NioServerHandler(NioChannel p) {
        parentChannel = p;
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        parentChannel.remoteDispatch((Message) e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.debug("Error while processing message ", e.getCause());
    }


}

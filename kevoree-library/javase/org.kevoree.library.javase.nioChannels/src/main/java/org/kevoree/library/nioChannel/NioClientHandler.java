package org.kevoree.library.nioChannel;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.kevoree.framework.message.Message;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class NioClientHandler extends SimpleChannelUpstreamHandler {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private NioChannel parentChannel;

    public NioClientHandler(NioChannel p) {
        parentChannel = p;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        InetSocketAddress remoteAdr = (InetSocketAddress) e.getChannel().getRemoteAddress();
        Message msg = parentChannel.getMsgQueue().popMsg(remoteAdr.getAddress().getHostAddress(), remoteAdr.getPort() + "");
        if(msg != null){
            e.getChannel().write(msg);
        }
        e.getChannel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.debug("Error while processing message ", e.getCause());
        ctx.getChannel().close();
    }


}

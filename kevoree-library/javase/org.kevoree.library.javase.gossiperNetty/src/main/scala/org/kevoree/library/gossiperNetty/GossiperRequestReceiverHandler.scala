package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class GossiperRequestReceiverHandler (serverActor: GossiperRequestReceiver) extends SimpleChannelUpstreamHandler {

  private val logger = LoggerFactory.getLogger (classOf[GossiperRequestReceiverHandler])

  override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
    val message = e.getMessage.asInstanceOf[Message]
    serverActor.sendReply (message, e.getRemoteAddress.asInstanceOf[InetSocketAddress], e.getChannel)
  }

  override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
    //NOOP
    try {
      logger.warn ("Communication failed between " + ctx.getChannel.getLocalAddress + " and " +
        ctx.getChannel.getRemoteAddress)
    } catch {
      case _ =>
    }
    //e.getChannel.close
  }
}

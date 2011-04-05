package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory

class GossiperRequestReceiverHandler(serverActor : GossiperRequestReceiver) extends SimpleChannelUpstreamHandler {

  private var logger = LoggerFactory.getLogger(classOf[GossiperRequestReceiverHandler])
  
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent) {
		val message = e.getMessage.asInstanceOf[Message]
	serverActor.sendReply(message, e.getRemoteAddress, e.getChannel)
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent) {
    //NOOP
	logger.error(this.getClass + "\n" + e.getCause.getMessage + "\n" + e.getCause.getStackTraceString)
	//e.getChannel.close
  }
}

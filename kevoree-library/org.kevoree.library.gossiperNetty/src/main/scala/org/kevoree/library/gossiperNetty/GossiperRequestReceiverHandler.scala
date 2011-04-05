package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import com.google.protobuf.ByteString

class GossiperRequestReceiverHandler(serverActor : GossiperRequestReceiver) extends SimpleChannelUpstreamHandler {

  private var logger = LoggerFactory.getLogger(classOf[GossiperRequestReceiverHandler])
  
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent) {
		val message = e.getMessage.asInstanceOf[Message]
	//println("Message received : " + message)
	serverActor.sendReply(message, e.getRemoteAddress, e.getChannel)
	//println("Message used")
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent) {
    //NOOP
	//println("Exception GossiperRequestReceiverHandler")
	logger.error(this.getClass + "\n" + e.getCause.getMessage + "\n" + e.getCause.getStackTraceString)
	//e.getChannel.close
  }
}

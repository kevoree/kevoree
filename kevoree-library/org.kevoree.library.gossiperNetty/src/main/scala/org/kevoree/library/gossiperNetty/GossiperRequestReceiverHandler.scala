package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message

class GossiperRequestReceiverHandler(serverActor : GossiperRequestReceiver) extends SimpleChannelUpstreamHandler {

  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent)={
	var message = e.getMessage.asInstanceOf[Message]
	//println("Message received : " + message)
	// TODO insert Actor into this
	serverActor.sendReply(message, e.getRemoteAddress, e.getChannel)
	//println("Message used")
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent)={
    //NOOP
	println("Exception GossiperRequestReceiverHandler")
	e.getCause().printStackTrace();
	//e.getChannel.close
  }
}

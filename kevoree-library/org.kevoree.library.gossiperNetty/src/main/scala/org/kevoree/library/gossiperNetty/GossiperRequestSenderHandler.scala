package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossip.Gossip.VectorClockUUIDs
import org.kevoree.library.gossip.Gossip.VersionedModel
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import com.google.protobuf.ByteString

class GossiperRequestSenderHandler(gossiperRequestSender: GossiperRequestSender) extends SimpleChannelUpstreamHandler {

	private var logger = LoggerFactory.getLogger(classOf[GossiperRequestSenderHandler])

	override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
		val message = e.getMessage.asInstanceOf[Message]
		//println("response received" + message.getContentClass)
		if (message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) {
			//var vectorClockUUIDs = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VectorClockUUIDs])
			gossiperRequestSender.initSecondStepAction(message, e.getRemoteAddress /*, e.getChannel*/)
		} /* else if (message.getContentClass.equals(classOf[VectorClockUUID].getName)) {
	  //var vectorClockUUID = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VectorClockUUID])
	  gossiperRequestSender.initLastStepAction(message, e.getRemoteAddress, e.getChannel)
	}*/
		else if (message.getContentClass.equals(classOf[VersionedModel].getName)) {
			//var versionModel = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VersionedModel])
			gossiperRequestSender.endGossipAction(message)
			//e.getChannel.close.awaitUninterruptibly
		}
	}

	override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
		//NOOP
		//println("Exception GossiperRequestSenderHandler")
		logger.error(this.getClass + "\n" + e.getCause.getMessage + "\n" + e.getCause.getStackTraceString)
		//e.getChannel.close.awaitUninterruptibly
	}
}

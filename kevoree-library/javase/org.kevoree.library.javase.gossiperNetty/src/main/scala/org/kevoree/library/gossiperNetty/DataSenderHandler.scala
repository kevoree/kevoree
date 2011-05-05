package org.kevoree.library.gossiperNetty

import com.google.protobuf.ByteString
import java.util.UUID
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import version.Gossip

class DataSenderHandler(channelFragment: NettyGossipAbstractElement, dataManager: DataManager, serializer : Serializer) extends SimpleChannelUpstreamHandler {

	private var logger = LoggerFactory.getLogger(classOf[DataSenderHandler])

	override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
		//println("message received")
		val message = e.getMessage.asInstanceOf[Message]
		if (message.getContentClass.equals(classOf[Gossip.UUIDDataRequest].getName)) {
			val uuidDataRequest = Gossip.UUIDDataRequest.parseFrom(message.getContent)
			val data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))

			val modelBytes = ByteString.copyFrom(serializer.serialize(data._2))

			val modelBytes2 = Gossip.VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1).setModel(modelBytes).build.toByteString
			val responseBuilder: Message.Builder = Message.newBuilder.setDestName(message.getDestName).setDestNodeName(channelFragment.getNodeName)
			responseBuilder.setContentClass(classOf[Gossip.VersionedModel].getName).setContent(modelBytes2)

			//e.getChannel.write(responseBuilder.build)
			e.getChannel.write(responseBuilder.build)
			//println("response sent")
		}
	}

	override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
		//NOOP
		logger.warn("Communication failed between " + ctx.getChannel.getLocalAddress + " and " + ctx.getChannel.getRemoteAddress)
		e.getChannel.close.awaitUninterruptibly
	}
}

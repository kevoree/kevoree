package test

import java.io.ByteArrayInputStream
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.ContainerRoot
import org.kevoree.extra.marshalling.RichString
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.library.gossip.Gossip.VersionedModel
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import com.google.protobuf.ByteString

class DataReceiverHandlerPojo() extends SimpleChannelUpstreamHandler {

	private var logger = LoggerFactory.getLogger(classOf[DataReceiverHandlerPojo])

	override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
		//println("response received")
		val message = e.getMessage.asInstanceOf[Message]
		if (message.getContentClass.equals(classOf[VersionedModel].getName)) {

			println("versionModel received")

			val versionedModel = VersionedModel.parseFrom(message.getContent)
		//	val data : Array[Byte] = RichString(versionedModel.getModel.toStringUtf8).fromJSON(classOf[Array[Byte]]).asInstanceOf[Array[Byte]]

			val root = modelFromString(versionedModel.getModel.toByteArray)

			println(root)
			println(root.getGroups)
			println(root.getNodes)
			//e.getChannel.getCloseFuture.awaitUninterruptibly
			e.getChannel.getCloseFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
		//NOOP

		e.getCause.printStackTrace
		e.getChannel.close.awaitUninterruptibly
	}

	private def modelFromString(model: Array[Byte]): ContainerRoot = {
		val stream = new ByteArrayInputStream(model)
		KevoreeXmiHelper.loadStream(stream)
	}
}

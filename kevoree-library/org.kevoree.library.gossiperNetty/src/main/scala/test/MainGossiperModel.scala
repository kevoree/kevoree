package test

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.{ServerBootstrap, ClientBootstrap}
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.channel.socket.nio.{NioServerSocketChannelFactory, NioClientSocketChannelFactory}
import org.jboss.netty.channel.{ChannelFuture, Channels, ChannelPipeline, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufDecoder}
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.library.gossip.Gossip.UUIDDataRequest
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 11:00
 */

object MainGossiperModel {

	//private var logger = LoggerFactory.getLogger(classOf[MainGossiperModel])

	def main(args: Array[String]): Unit = {
		val factoryTCP = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
		val bootstrapTCP = new ClientBootstrap(factoryTCP)
		bootstrapTCP.setPipelineFactory(new ChannelPipelineFactory() {
			override def getPipeline: ChannelPipeline = {
				val p: ChannelPipeline = Channels.pipeline()
				//p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
				p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
				//p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
				p.addLast("protobufEncoder", new ProtobufEncoder())
				p.addLast("handler", new DataReceiverHandlerPojo())
				return p
			}
		}
		)
		bootstrapTCP.setOption("tcpNoDelay", true)

		val channelGroup = new DefaultChannelGroup

		val factoryForRequestTCP = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
		val bootstrapForRequestTCP = new ServerBootstrap(factoryForRequestTCP)
		bootstrapForRequestTCP.setPipelineFactory(new ChannelPipelineFactory() {
			override def getPipeline(): ChannelPipeline = {
				val p: ChannelPipeline = Channels.pipeline()
				//p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
				p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
				//p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
				p.addLast("protobufEncoder", new ProtobufEncoder())
				p.addLast("handler", new DataSenderHandlerPojo(model("/home/edaubert/gossipGroupTest.kev")))
				return p
			}
		}
		)
		bootstrapForRequestTCP.setOption("tcpNoDelay", true)
		/*private var channelTCP = */ bootstrapForRequestTCP.bind(new InetSocketAddress(9000));


		val messageBuilder: Message.Builder = Message.newBuilder.setDestName("titi").setDestNodeName("toto")
		messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid("1").build.toByteString)
		val channelFuture = bootstrapTCP.connect(new InetSocketAddress("localhost", 9000)).asInstanceOf[ChannelFuture]
		val channel = channelFuture.awaitUninterruptibly.getChannel
		if (!channelFuture.isSuccess) {
			//channelFuture.getCause.printStackTrace
			println(this.getClass + "\n" + channelFuture.getCause.getMessage + "\n" + channelFuture.getCause.getStackTraceString)
			bootstrapTCP.releaseExternalResources
		} else {
			/*var future = */
			//channel.write(messageBuilder.build)
			channel.write(messageBuilder.build)
			//future.awaitUninterruptibly
			//channel.getCloseFuture.awaitUninterruptibly
			channelGroup.add(channel)
			//future.addListener(ChannelFutureListener.CLOSE)
			//channel.close.awaitUninterruptibly
			//println("TCP sent")
		}
	}

	def model(url: String): ContainerRoot = {
		println("load model")
		KevoreeXmiHelper.load(url)
	}
}
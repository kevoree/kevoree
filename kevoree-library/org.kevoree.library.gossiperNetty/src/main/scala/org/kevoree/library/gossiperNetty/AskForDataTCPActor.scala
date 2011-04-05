/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.kevoree.library.gossip.Gossip.UUIDDataRequest
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf.{ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder, ProtobufEncoder}

class AskForDataTCPActor(channelFragment: NettyGossipAbstractElement, requestSender: GossiperRequestSender) extends actors.DaemonActor {

	var factoryTCP = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
	var bootstrapTCP = new ClientBootstrap(factoryTCP)
	bootstrapTCP.setPipelineFactory(new ChannelPipelineFactory() {
		override def getPipeline: ChannelPipeline = {
			val p: ChannelPipeline = Channels.pipeline()
			p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
			p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
			p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
			p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
			p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
			p.addLast("protobufEncoder", new ProtobufEncoder)
			p.addLast("handler", new DataReceiverHandler(requestSender))
			return p
		}
	}
	)
	bootstrapTCP.setOption("tcpNoDelay", true)

	private var channelGroup = new DefaultChannelGroup

	private var logger = LoggerFactory.getLogger(classOf[AskForDataTCPActor])

	this.start()

	/* PUBLIC PART */
	case class STOP()

	case class ASK_FOR_DATA(uuid: UUID, remoteNodeName: String)

	def stop() {
		this ! STOP()
	}

	def askForDataAction(uuid: UUID, remoteNodeName: String) {
		this ! ASK_FOR_DATA(uuid, remoteNodeName)
	}

	/* PRIVATE PROCESS PART */
	def act() {
		loop {
			react {
				case STOP => {
					channelGroup.close.awaitUninterruptibly
					bootstrapTCP.releaseExternalResources
					this.exit
				}
				case ASK_FOR_DATA(uuid, remoteNodeName) => {
					if (channelGroup.size > 10) {
						closeUnusedChannels
					}
					askForData(uuid, remoteNodeName)
				}
			}
		}
	}

	def closeUnusedChannels() {
		channelGroup.foreach {
			channel: Channel =>
				logger.debug("channel must be closed")
				channel.close.awaitUninterruptibly
				logger.debug("channel are closed")
		}
	}

	def askForData(uuid: UUID, remoteNodeName: String) = {
		val messageBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
		messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
		//println("TCP sending ...")
		val channelFuture = bootstrapTCP.connect(new InetSocketAddress(channelFragment.getAddress(remoteNodeName), channelFragment.parsePortNumber(remoteNodeName))).asInstanceOf[ChannelFuture]
		val channel = channelFuture.awaitUninterruptibly.getChannel
		if (!channelFuture.isSuccess) {
			logger.error(this.getClass + "\n" + channelFuture.getCause.getMessage + "\n" + channelFuture.getCause.getStackTraceString)
			bootstrapTCP.releaseExternalResources
		} else {
			channel.write(messageBuilder.build)
			channelGroup.add(channel)
		}
	}
}

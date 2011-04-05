package org.kevoree.library.gossiperNetty

import java.util.UUID
import java.util.concurrent.Executors
import com.google.protobuf.ByteString
import java.net.InetSocketAddress
import java.net.SocketAddress
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.kevoree.extra.marshalling.RichJSONObject
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.kevoree.library.gossip.Gossip
import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder}

class GossiperRequestReceiver(channelFragment: NettyGossipAbstractElement, dataManager: DataManager, port: Int, gossiperRequestSender: GossiperRequestSender, fullUDP: java.lang.Boolean, serializer: Serializer) extends actors.DaemonActor {

	var self = this
	// define attributes used to define channel to listen request
	var factoryForRequest = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
	var bootstrapForRequest = new ConnectionlessBootstrap(factoryForRequest)
	bootstrapForRequest.setPipelineFactory(new ChannelPipelineFactory() {
		override def getPipeline(): ChannelPipeline = {
			val p: ChannelPipeline = Channels.pipeline()
			//p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
			//p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
			p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
			p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
			p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
			p.addLast("protobufEncoder", new ProtobufEncoder())
			p.addLast("handler", new GossiperRequestReceiverHandler(self))
			return p
		}
	}
	)
	private var channel = bootstrapForRequest.bind(new InetSocketAddress(port));

	var factoryForRequestTCP = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
	var bootstrapForRequestTCP = new ServerBootstrap(factoryForRequestTCP)
	bootstrapForRequestTCP.setPipelineFactory(new ChannelPipelineFactory() {
		override def getPipeline(): ChannelPipeline = {
			val p: ChannelPipeline = Channels.pipeline()
			p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
			p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
			p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
			p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
			p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
			p.addLast("protobufEncoder", new ProtobufEncoder)
			p.addLast("handler", new DataSenderHandler(channelFragment, dataManager, serializer))
			return p
		}
	}
	)
	bootstrapForRequestTCP.setOption("tcpNoDelay", true)
	/*private var channelTCP = */ bootstrapForRequestTCP.bind(new InetSocketAddress(port));

	this.start

	case class SendReply(message: Message, address: SocketAddress, channel: Channel)

	//case class RETURN_MSG()
	case class STOP_GOSSIPER()

	def stop() {
		this ! STOP_GOSSIPER()
	}

	def sendReply(message: Message, address: SocketAddress, channel: Channel) = {
		this ! SendReply(message, address, channel)
	}

	/* PRIVATE PROCESS PART */
	def act() {
		loop {
			react {
				case STOP_GOSSIPER() => {
					//channel.close.awaitUninterruptibly
					channel.close.awaitUninterruptibly
					bootstrapForRequest.releaseExternalResources
					this.exit
				}
				case SendReply(message, address, channel) => doGossip(message, address, channel)
			}
		}
	}

	private def doGossip(message: Message, address: SocketAddress, channel: Channel) /*: Channel*/ = {
		//println(address)
		var responseBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)

		message.getContentClass match {
			case "org.kevoree.library.gossip.Gossip$VectorClockUUIDsRequest" => {
				val uuidVectorClocks = dataManager.getUUIDVectorClocks
				var vectorClockUUIDsBuilder = Gossip.VectorClockUUIDs.newBuilder
				uuidVectorClocks.keySet.foreach {
					uuid: UUID =>
						vectorClockUUIDsBuilder.addVectorClockUUIDs(Gossip.VectorClockUUID.newBuilder.setUuid(uuid.toString).setVector(uuidVectorClocks.get(uuid)).build)
						if (vectorClockUUIDsBuilder.getVectorClockUUIDsCount == 1) {
							// it is possible to increase the number of vectorClockUUID on each message
							responseBuilder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
							val modelBytes = vectorClockUUIDsBuilder.build.toByteString
							responseBuilder.setContentClass(classOf[Gossip.VectorClockUUIDs].getName).setContent(modelBytes)
							channel.write(responseBuilder.build, address)
							vectorClockUUIDsBuilder = Gossip.VectorClockUUIDs.newBuilder
						}
				}
			}
			/*case "org.kevoree.library.gossip.Gossip$UUIDVectorClockRequest" => {
								 var uuidVectorClockRequest = Gossip.UUIDVectorClockRequest.parseFrom(message.getContent.asInstanceOf[ByteString])
								 var vectorClock =dataManager.getUUIDVectorClock(UUID.fromString(uuidVectorClockRequest.getUuid))

								 var modelBytes = Gossip.VectorClockUUID.newBuilder.setUuid(uuidVectorClockRequest.getUuid).setVector(vectorClock).build.toByteString
								 responseBuilder.setContentClass(classOf[Gossip.VectorClockUUID].getName).setContent(modelBytes)
								 channel.write(responseBuilder.build, address);
								 println("response of secondStep")
								 }*/
			case "org.kevoree.library.gossip.Gossip$UUIDDataRequest" => {
				val uuidDataRequest = Gossip.UUIDDataRequest.parseFrom(message.getContent)
				val data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))
				val modelBytes = ByteString.copyFrom(serializer.serialize(data._2))

				val modelBytes2 = Gossip.VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1).setModel(modelBytes).build.toByteString
				responseBuilder.setContentClass(classOf[Gossip.VersionedModel].getName).setContent(modelBytes2)
				channel.write(responseBuilder.build, address);
			}
			case "org.kevoree.library.gossip.Gossip$UpdatedValueNotification" => {
				gossiperRequestSender.initGossipAction(message.getDestNodeName)
			}
		}
		//channel.close.awaitUninterruptibly
	}
}

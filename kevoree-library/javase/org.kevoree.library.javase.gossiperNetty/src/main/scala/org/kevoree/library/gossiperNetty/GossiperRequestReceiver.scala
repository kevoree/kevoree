package org.kevoree.library.gossiperNetty

import java.util.UUID
import java.util.concurrent.Executors
import com.google.protobuf.ByteString
import java.net.InetSocketAddress
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder}
import version.Gossip
import version.Gossip.{UpdatedValueNotification, UUIDDataRequest, VectorClockUUIDsRequest}
import org.slf4j.LoggerFactory
import org.jboss.netty.channel._
class GossiperRequestReceiver(protected var channelFragment: NettyGossipAbstractElement, dataManager: DataManager, port: Int, gossiperRequestSender: GossiperRequestSender, fullUDP: java.lang.Boolean, serializer: Serializer) extends actors.DaemonActor {

  private val logger = LoggerFactory.getLogger(classOf[GossiperRequestReceiver])

  var self = this
  // define attributes used to define channel to listen request
  var factoryForRequest = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrapForRequest = new ConnectionlessBootstrap(factoryForRequest)
  bootstrapForRequest.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
      p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
      p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
      p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
      p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
      p.addLast("protobufEncoder", new ProtobufEncoder())
      p.addLast("handler", new GossiperRequestReceiverHandler(self))
      p
    }
  }
                                        )
  private var channel : Channel = null //bootstrapForRequest.bind(new InetSocketAddress(port));

  var factoryForRequestTCP = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
  var bootstrapForRequestTCP = new ServerBootstrap(factoryForRequestTCP)
  bootstrapForRequestTCP.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
      p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
      p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
      p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
      p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
      p.addLast("protobufEncoder", new ProtobufEncoder)
      p.addLast("handler", new DataSenderHandler(channelFragment, dataManager, serializer))
      p
    }
  }
                                           )
  bootstrapForRequestTCP.setOption("tcpNoDelay", true)
  private var channelTCP : Channel = null //bootstrapForRequestTCP.bind(new InetSocketAddress(port))


  /* PUBLIC PART */
  override def start () = {
    channel = bootstrapForRequest.bind (new InetSocketAddress (port))//.asInstanceOf[DatagramChannel]
    channelTCP = bootstrapForRequestTCP.bind(new InetSocketAddress(port))
    super.start()
    this
  }

  case class SendReply(message: Message, address: InetSocketAddress, channel: Channel)

  //case class RETURN_MSG()
  case class STOP_GOSSIPER()

  def stop() {
    this ! STOP_GOSSIPER()
  }

  def sendReply(message: Message, address: InetSocketAddress, channel: Channel) = {
    this ! SendReply(message, address, channel)
  }

  /* PRIVATE PROCESS PART */
  def act() {
    loop {
      react {
        case STOP_GOSSIPER() => {
          //channelTCP.close.awaitUninterruptibly
          //channel.close.awaitUninterruptibly // TODO do not block on actor
          channelTCP.close().addListener(ChannelFutureListener.CLOSE)
          channel.close().addListener(ChannelFutureListener.CLOSE)
          bootstrapForRequest.releaseExternalResources()
          this.exit()
        }
        case SendReply(message, address, channel) => doGossip(message, address, channel)
      }
    }
  }

  private def doGossip(message: Message, address: InetSocketAddress, channel: Channel) /*: Channel*/ = {
    //println(address)
    var responseBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)

    message.getContentClass match {
      case s: String if (s == classOf[VectorClockUUIDsRequest].getName) => {
        logger.debug("vectorcclock request received from " + address)
        val uuidVectorClocks = dataManager.getUUIDVectorClocks()
        var vectorClockUUIDsBuilder = Gossip.VectorClockUUIDs.newBuilder
        uuidVectorClocks.keySet.foreach {
          uuid: UUID =>
            vectorClockUUIDsBuilder.addVectorClockUUIDs(Gossip.VectorClockUUID.newBuilder.setUuid(uuid.toString).setVector(uuidVectorClocks.get(uuid)).build)
            if (vectorClockUUIDsBuilder.getVectorClockUUIDsCount == 1) {
              // it is possible to increase the number of vectorClockUUID on each message
              responseBuilder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
              val modelBytes = vectorClockUUIDsBuilder.build.toByteString
              responseBuilder.setContentClass(classOf[Gossip.VectorClockUUIDs].getName).setContent(modelBytes)
              writeMessage(responseBuilder.build, address, channel)
              vectorClockUUIDsBuilder = Gossip.VectorClockUUIDs.newBuilder
            }
        }
      }
      /*case "org.kevoree.library.gossiperNetty.version.Gossip$UUIDVectorClockRequest" => {
                       var uuidVectorClockRequest = Gossip.UUIDVectorClockRequest.parseFrom(message.getContent.asInstanceOf[ByteString])
                       var vectorClock =dataManager.getUUIDVectorClock(UUID.fromString(uuidVectorClockRequest.getUuid))

                       var modelBytes = Gossip.VectorClockUUID.newBuilder.setUuid(uuidVectorClockRequest.getUuid).setVector(vectorClock).build.toByteString
                       responseBuilder.setContentClass(classOf[Gossip.VectorClockUUID].getName).setContent(modelBytes)
                       channel.write(responseBuilder.build, address);
                       println("response of secondStep")
                       }*/
      case s: String if (s == classOf[UUIDDataRequest].getName) => {
        val uuidDataRequest = Gossip.UUIDDataRequest.parseFrom(message.getContent)
        val data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))
        logger.debug("before serializing data")
        val bytes : Array[Byte] = serializer.serialize(data._2);
        logger.debug("after serializing data")
        if (bytes != null) {
          val modelBytes = ByteString.copyFrom(bytes)

          val modelBytes2 = Gossip.VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1).setModel(modelBytes).build.toByteString
          responseBuilder.setContentClass(classOf[Gossip.VersionedModel].getName).setContent(modelBytes2)
          writeMessage(responseBuilder.build, address, channel)
        } else {
          logger.warn("Serialization failed !")
        }

      }
      case s: String if (s == classOf[UpdatedValueNotification].getName) => {
        println("notification received from " + address)
        gossiperRequestSender.initGossipAction(message.getDestNodeName)
      }
    }
    //channel.close.awaitUninterruptibly
  }

  protected def writeMessage(o : Object, address : InetSocketAddress, channel : Channel) {
    if (address != null && channel != null) {
      logger.debug("message sent")
      channel.write (o, address)
    }
  }
}

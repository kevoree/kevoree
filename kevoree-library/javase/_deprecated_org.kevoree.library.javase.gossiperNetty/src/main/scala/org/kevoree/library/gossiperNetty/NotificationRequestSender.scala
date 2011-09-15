package org.kevoree.library.gossiperNetty

import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.socket.DatagramChannel
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf.{ProtobufVarint32LengthFieldPrepender, ProtobufVarint32FrameDecoder, ProtobufDecoder, ProtobufEncoder}
import version.Gossip.UpdatedValueNotification
import org.slf4j.LoggerFactory
import java.net.{SocketAddress, InetSocketAddress}
import org.jboss.netty.channel._

class NotificationRequestSender (protected var channelFragment: NettyGossipAbstractElement) extends actors.DaemonActor {

  // define attributes used to define channel to send notification message
  var factoryNotificationMessage = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrapNotificationMessage = new ConnectionlessBootstrap(factoryNotificationMessage)
  bootstrapNotificationMessage.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      //p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
      //p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
      p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
      p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
      p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender);
      p.addLast("protobufEncoder", new ProtobufEncoder)

      p.addLast("handler", new GossiperRequestSenderHandler(null))
      p
    }
  }
                                                 )
  private val logger = LoggerFactory.getLogger(classOf[NotificationRequestSender])

  //private var channels : ChannelGroup = new DefaultChannelGroup
  protected var channel : Channel = null //bootstrapNotificationMessage.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]


  /* PUBLIC PART */
  override def start () = {
    channel = bootstrapNotificationMessage.bind (new InetSocketAddress (0))//.asInstanceOf[DatagramChannel]
    super.start()
    this
  }


  /* PUBLIC PART */
  case class STOP_GOSSIPER ()

  case class NOTIFY_PEERS ()

  def stop () {
    this ! STOP_GOSSIPER()
  }

  def notifyPeersAction () {
    this ! NOTIFY_PEERS()
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      react {
        //reactWithin(timeout.longValue){
        case STOP_GOSSIPER() => {
          //println("stop gossiper")
          //channel.close.awaitUninterruptibly // TODO do not block on actor
          channel.close().addListener(ChannelFutureListener.CLOSE)
          bootstrapNotificationMessage.releaseExternalResources()
          this.exit()
        }
        case NOTIFY_PEERS() => {
          //channels.close.awaitUninterruptibly
          doNotifyPeers()
        }
      }
    }
  }

  private def doNotifyPeers () {
    channelFragment.getAllPeers.foreach {
      peer =>
        if (!peer.equals(channelFragment.getNodeName)) {
          logger.debug("send notification to " + peer)
          val messageBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName)
            .setDestNodeName(channelFragment.getNodeName)
          messageBuilder.setContentClass(classOf[UpdatedValueNotification].getName)
            .setContent(UpdatedValueNotification.newBuilder.build.toByteString)
          //channel.write(messageBuilder.build, new InetSocketAddress(channelFragment.getAddress(peer), channelFragment.parsePortNumber(peer)))
          writeMessage(messageBuilder.build, new InetSocketAddress(channelFragment.getAddress(peer), channelFragment.parsePortNumber(peer)))
        }
    }
  }

  protected def writeMessage (o: Object, address: InetSocketAddress) {
    logger.debug("message sent")
    channel.write(o, address)
  }
}

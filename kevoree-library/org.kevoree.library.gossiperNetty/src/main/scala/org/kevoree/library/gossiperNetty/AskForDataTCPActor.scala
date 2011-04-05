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
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}

class AskForDataTCPActor(channelFragment: NettyGossipAbstractElement, requestSender: GossiperRequestSender[_]) extends actors.DaemonActor {
  var factoryTCP = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
  var bootstrapTCP = new ClientBootstrap(factoryTCP)
  bootstrapTCP.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline : ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
		//p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
      //p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
		//p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
      //p.addLast("protobufEncoder", new ProtobufEncoder())
			p.addLast("StringEncoder", new StringEncoder(CharsetUtil.UTF_8))
			p.addLast("StringDecoder", new StringDecoder(CharsetUtil.UTF_8))
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
            println("too many opened channels : " + channelGroup.size)
            closeUnusedChannels
            println("some channels may be closed : " + channelGroup.size)
          }
          askForData(uuid, remoteNodeName)
        }
      }
    }

  }

  def closeUnusedChannels() {
    channelGroup.foreach {
      channel: Channel =>
        println("channel must be closed")
        channel.close.awaitUninterruptibly
        println("channel are closed")
    }
  }


  def askForData(uuid: UUID, remoteNodeName: String) = {
    val messageBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
    messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
    //println("TCP sending ...")
    val channelFuture = bootstrapTCP.connect(new InetSocketAddress(channelFragment.getAddress(remoteNodeName), channelFragment.parsePortNumber(remoteNodeName))).asInstanceOf[ChannelFuture]
    val channel = channelFuture.awaitUninterruptibly.getChannel
    if (!channelFuture.isSuccess) {
      //channelFuture.getCause.printStackTrace
			logger.error(this.getClass + "\n" +channelFuture.getCause.getMessage + "\n" + channelFuture.getCause.getStackTraceString)
      bootstrapTCP.releaseExternalResources
    } else {
      /*var future = */
			//channel.write(messageBuilder.build)
			channel.write(messageBuilder.build.toByteString.toStringUtf8)
      //future.awaitUninterruptibly
      //channel.getCloseFuture.awaitUninterruptibly
      channelGroup.add(channel)
      //future.addListener(ChannelFutureListener.CLOSE)
      //channel.close.awaitUninterruptibly
      //println("TCP sent")
    }
  }
}

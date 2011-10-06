package org.kevoree.library.javase.streamChannels

import actors.DaemonActor

import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.serialization.{ObjectDecoder, ObjectEncoder}
import java.net.InetSocketAddress
import socket.nio.{NioDatagramChannelFactory, NioServerSocketChannelFactory}
import org.jboss.netty.bootstrap.{ConnectionlessBootstrap, ServerBootstrap}
/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:45
 */

class StreamingActor (instance: StreamChannel, port: Int) extends DaemonActor {

  private val logger = LoggerFactory.getLogger(classOf[StreamingActor])

  val bootstrapServer = new ServerBootstrap(new
      NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))
  bootstrapServer.setPipelineFactory(new ChannelPipelineFactory {
    def getPipeline = {
      val p = Channels.pipeline()
      p.addLast("decoder", new ObjectDecoder())
      p.addLast("encoder", new ObjectEncoder())

      p.addLast("handler", new DataReceiverHandler(instance))
      p
    }
  })
  val channel: Channel = bootstrapServer.bind(new InetSocketAddress(port))

  var factory = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrap = new ConnectionlessBootstrap(factory)
  bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      p.addLast("decoder", new ObjectDecoder())
      p.addLast("encoder", new ObjectEncoder())

      p.addLast("handler", new DataSenderHandler())
      p
    }
  })
  var channel: Channel = bootstrap.bind(new InetSocketAddress(0))

  case class STOP ()

  case class SEND_TO_PEERS (bytes: Array[Byte])

  def sendToPeers (bytes: Array[Byte]) {
    this ! SEND_TO_PEERS(bytes)
  }

  def act () {
    loop {
      react {
        case STOP() =>
        case SEND_TO_PEERS(bytes) => sendToPeersInternal(bytes)
      }
    }
  }

  private def sendToPeersInternal (bytes: Array[Byte]) {
    instance.getAllPeers.foreach {
      peer =>
        if (!peer.equals(instance.getNodeName)) {
          logger.debug("send data to " + peer)
          val address = instance.getAddress(instance.getNodeName)
          val port = instance.parsePortNumber(instance.getNodeName)
          val future = bootstrap.connect(new InetSocketAddress(address, port))
          // Wait until the connection attempt succeeds or fails.
          val channel = future.awaitUninterruptibly().getChannel
          if (!future.isSuccess) {
            logger.error(address + "is not available", future.getCause.printStackTrace())
          } else {
            channel.write(o)
            if (channelGroup.size() == 10) {
              channelGroup.foreach {
                channel => {
                  channel.close().addListener(ChannelFutureListener.CLOSE)
                  logger.debug("releasing too old channel ...")
                }
              }
              channelGroup.clear()
            }
            channelGroup.add(channel);
          }
        }
    }
  }

  private class DataReceiverHandler (instance: StreamChannel) extends SimpleChannelUpstreamHandler {
    override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
      if () {
        instance.localNotification((Message)e.getMessage))
        logger.debug("data sent")
        e.getChannel.close()
      }
    }

    override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
      logger.error("Communication failed between " + ctx.getChannel.getLocalAddress + " and " +
        ctx.getChannel.getRemoteAddress, e.getCause)
      e.getChannel.close()
    }
  }

  private class DataSenderHandler () extends SimpleChannelUpstreamHandler {
    override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
      logger.debug("data sent")
      e.getChannel.getCloseFuture.addListener(ChannelFutureListener.CLOSE)
    }

    override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
      logger.error("Communication failed between " + ctx.getChannel.getLocalAddress + " and " +
        ctx.getChannel.getRemoteAddress, e.getCause)
      e.getChannel.close()
    }
  }

}
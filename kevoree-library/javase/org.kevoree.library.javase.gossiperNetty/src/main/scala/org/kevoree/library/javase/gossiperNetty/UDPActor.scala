package org.kevoree.library.javase.gossiperNetty

import java.net.InetSocketAddress
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder}
import java.util.concurrent.Executors
import org.jboss.netty.channel._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:26
 */

class UDPActor (port: Int, processValue: ProcessValue, processRequest: ProcessRequest) extends NetworkActor {
  private val logger = LoggerFactory.getLogger(classOf[UDPActor])


  var factoryServer = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrapServer = new ConnectionlessBootstrap(factoryServer)
  var self = this
  bootstrapServer.setPipelineFactory(new ChannelPipelineFactory {
    def getPipeline = {
      val p = Channels.pipeline()
      p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
      p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
      p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
      p.addLast("protobufEncoder", new ProtobufEncoder)

      p.addLast("handler", new UDPRequestHandler(processRequest))
      p
    }
  })
  var channelServer: Channel = bootstrapServer.bind(new InetSocketAddress(port))


  var factory = new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrap = new ConnectionlessBootstrap(factory)
  bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
      p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
      p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
      p.addLast("protobufEncoder", new ProtobufEncoder())

      p.addLast("handler", new UDPValueHandler(processValue))
      p
    }
  })
  var channel: Channel = bootstrap.bind(new InetSocketAddress(0))


  protected def stopInternal () {
    channelServer.unbind()
    if (!channelServer.getCloseFuture.awaitUninterruptibly(5000)) {
      channelServer.close().awaitUninterruptibly();
    }
    channel.close().awaitUninterruptibly()
    // Shut down all thread pools to exit.
    bootstrap.releaseExternalResources();
    bootstrapServer.releaseExternalResources();
  }

  protected def sendMessageInternal (o: Message, address: InetSocketAddress) {
    //    logger.debug("Sending message to " + address)
    channel.write(o, address)
  }

  protected def sendMessageToChannelInternal (o: Message, channel: Channel, address: InetSocketAddress) {
    //    logger.debug("Sending message to " + address)
    channel.write(o, address)
  }

  private class UDPRequestHandler (processRequest: ProcessRequest) extends SimpleChannelUpstreamHandler {
    override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
      //      logger.debug("something received as a request...")
      if (e.getMessage.isInstanceOf[Message]) {
        //        logger.debug("request message received from " + ctx.getChannel.getRemoteAddress)
        processRequest.receiveRequest(e.getMessage.asInstanceOf[Message], e.getChannel,
                                       e.getRemoteAddress.asInstanceOf[InetSocketAddress])
      }
    }

    override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
      logger.error("Communication failed between " + ctx.getChannel.getLocalAddress + " and " +
        ctx.getChannel.getRemoteAddress, e.getCause)
      e.getChannel.close()
    }
  }

  private class UDPValueHandler (processValue: ProcessValue) extends SimpleChannelUpstreamHandler {
    override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
      //      logger.debug("something received as a data...")
      if (e.getMessage.isInstanceOf[Message]) {
        //        logger.debug("data message received from " + ctx.getChannel.getRemoteAddress)
        processValue.receiveValue(e.getMessage.asInstanceOf[Message])
      }
    }

    override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
      logger.error("Communication failed between " + ctx.getChannel.getLocalAddress + " and " +
        ctx.getChannel.getRemoteAddress, e.getCause)
      e.getChannel.close()
    }
  }

}

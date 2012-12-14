package org.kevoree.library.javase.gossiperNetty.group

import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory
import java.io._
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import java.net.{InetSocketAddress, URLConnection, URL}
import org.jboss.netty.channel._
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import org.kevoree.framework.{ZipUtil, KevoreeXmiHelper, KevoreePropertyHelper, Constants}
import java.util.zip.Inflater
import org.kevoree.loader.ContainerRootLoader
import java.nio.charset.Charset

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/02/12
 * Time: 09:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class HTTPServer (group: NettyGossiperGroup, port: Int) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  // configure the server
  var factoryServer = new
      NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
  var bootstrapServer = new ServerBootstrap(factoryServer)
  bootstrapServer.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      p.addLast("decoder", new HttpRequestDecoder)
      p.addLast("aggregator", new HttpChunkAggregator(1024048576))
      p.addLast("encoder", new HttpResponseEncoder)
      p.addLast("handler", new HTTPServerHandler())
      p
    }
  })
  bootstrapServer.setOption("tcpNoDelay", true)
  var channelServer: Channel = bootstrapServer.bind(new InetSocketAddress(port))

  private class HTTPServerHandler extends SimpleChannelUpstreamHandler {
    override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
      if (e.getMessage.isInstanceOf[HttpRequest]) {
        val httpRequest = e.getMessage.asInstanceOf[HttpRequest]
        if (httpRequest.getUri == "/model/current/zip" && httpRequest.getMethod == HttpMethod.POST) {
          logger.debug("receive zip model")
          val stream = new ByteArrayInputStream(httpRequest.getContent.array())
          group.getModelService.updateModel(KevoreeXmiHelper.loadCompressedStream(stream))
          ctx.getChannel.write(buildResponse("<ack nodeName=\"" + group.getNodeName + "\" />"))
        } else if (httpRequest.getUri == "/model/current/zip" && httpRequest.getMethod == HttpMethod.GET) {
          logger.debug("Ask for zip model")
          val stream = new ByteArrayOutputStream()
          KevoreeXmiHelper.saveCompressedStream(stream, group.getModelService.getLastModel)
          ctx.getChannel.write(buildResponse(stream.toByteArray))
        } else if (httpRequest.getUri == "/model/current" && httpRequest.getMethod == HttpMethod.POST) {
          logger.debug("receive model")
          val modelString = httpRequest.getContent.toString(Charset.forName("UTF-8"))
          group.getModelService.updateModel(KevoreeXmiHelper.loadString(modelString))
          ctx.getChannel.write(buildResponse("<ack nodeName=\"" + group.getNodeName + "\" />"))
        } else if (httpRequest.getUri == "/model/current" && httpRequest.getMethod == HttpMethod.GET) {
          logger.debug("Ask for model")
          ctx.getChannel.write(buildResponse(KevoreeXmiHelper.saveToString(group.getModelService.getLastModel, prettyPrint = false)))
        }
        ctx.getChannel.close()
      }
    }

    override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
      logger.error("Communication failed between " + ctx.getChannel.getLocalAddress + " and " + ctx.getChannel.getRemoteAddress, e.getCause)
      e.getChannel.close()
    }

    private def buildResponse (content: String): HttpResponse = {
      // Build the response object.
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8))
      response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8")
      response
    }

    private def buildResponse (content: Array[Byte]): HttpResponse = {
      // Build the response object.
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setContent(ChannelBuffers.copiedBuffer(content))
      //response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8")
      response
    }
  }

  def stop () {
    logger.debug("stopping HTTP server")
    channelServer.unbind()
    if (!channelServer.getCloseFuture.awaitUninterruptibly(5000)) {
      channelServer.close().awaitUninterruptibly()
    }
    bootstrapServer.releaseExternalResources()
    logger.debug("HTTP server stopped")
  }

}

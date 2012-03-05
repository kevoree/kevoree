package org.kevoree.library.javase.gossiperNetty.group

import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory
import org.kevoree.framework.{KevoreeXmiHelper, KevoreePropertyHelper, Constants}
import java.io._
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import java.net.{InetSocketAddress, URLConnection, URL}
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponseEncoder, HttpContentCompressor, HttpRequestDecoder}
import org.jboss.netty.util.CharsetUtil

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/02/12
 * Time: 09:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class HTTPServer (group: NettyGossiperGroup, port: Int) {
  private val logger = LoggerFactory.getLogger(classOf[HTTPServer])

  // configure the server
  var factoryServer = new
      NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
  var bootstrapServer = new ServerBootstrap(factoryServer)
  bootstrapServer.setPipelineFactory(new ChannelPipelineFactory() {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline()
      p.addLast("decoder", new HttpRequestDecoder)
      p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
      p.addLast("deflater", new HttpContentCompressor)
      p.addLast("encoder", new HttpResponseEncoder)
      p.addLast("handler", new HTTPServerHandler())
      p
    }
  })
  bootstrapServer.setOption("tcpNoDelay", true)
  var channelServer: Channel = bootstrapServer.bind(new InetSocketAddress(port))

  def push (model: ContainerRoot, targetNodeName: String) {
    val ipOption = KevoreePropertyHelper.getStringNetworkProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var IP = "127.0.0.1"
    if (ipOption.isDefined) {
      IP = ipOption.get
    }
    val portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, group.getName, "port", true, targetNodeName)
    var PORT: Int = 8000
    if (portOption.isDefined) {
      PORT = portOption.get
    }

    logger.debug("url=>" + "http://" + IP + ":" + PORT + "/model/current")

    if (!sendModel(model, "http://" + IP + ":" + PORT + "/model/current")) {
      logger.debug("Unable to push a model on " + targetNodeName)
    }
  }

  def pull (model: ContainerRoot, targetNodeName: String): ContainerRoot = {
    var localhost: String = "localhost"
    var port: Int = 8000
    try {
      val addressOption = KevoreePropertyHelper.getStringNetworkProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
      if (addressOption.isDefined) {
        localhost = addressOption.get
      }
      val portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, group.getName, "port", true, targetNodeName)
      if (portOption.isDefined) {
        port = portOption.get
      }
    }
    catch {
      case e: IOException => {
        logger.error("Unable to getAddress or Port of " + targetNodeName, e)
      }
    }
    logger.debug("Pulling model " + targetNodeName + " " + "http://" + localhost + ":" + port + "/model/current")


    try {
      val url: URL = new URL("http://" + localhost + ":" + port + "/model/current")
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(2000)
      val inputStream: InputStream = conn.getInputStream
      KevoreeXmiHelper.loadStream(inputStream)
    }
    catch {
      case e: IOException => {
        logger.error("error while pulling model for name " + targetNodeName, e)
        null
      }
    }


  }

  private def sendModel (model: ContainerRoot, urlPath: String): Boolean = {
    try {
      val outStream = new ByteArrayOutputStream
      KevoreeXmiHelper.saveStream(outStream, model)
      outStream.flush()
      val url = new URL(urlPath)
      val conn = url.openConnection
      conn.setConnectTimeout(3000)
      conn.setDoOutput(true)
      val wr = new OutputStreamWriter(conn.getOutputStream)
      wr.write(outStream.toString)
      wr.flush()
      val rd = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line = rd.readLine
      while (line != null) {
        line = rd.readLine
      }
      wr.close()
      rd.close()
      true
    } catch {
      case e: Exception => {
        false
      }
    }
  }

  private class HTTPServerHandler extends SimpleChannelUpstreamHandler {
    override def messageReceived (ctx: ChannelHandlerContext, e: MessageEvent) {
      if (e.getMessage.isInstanceOf[HttpRequest]) {
        val httpRequest = e.getMessage.asInstanceOf[HttpRequest]
        val content = httpRequest.getContent
        val modelString = content.toString(CharsetUtil.UTF_8)
        group.getModelService.updateModel(KevoreeXmiHelper.loadString(modelString))
        ctx.getChannel.write("<ack nodeName=\"" + group.getNodeName + "\" />")
        ctx.getChannel.close()

      }
    }

    override def exceptionCaught (ctx: ChannelHandlerContext, e: ExceptionEvent) {
      logger.error("Communication failed between " + ctx.getChannel.getLocalAddress + " and " +
        ctx.getChannel.getRemoteAddress, e.getCause)
      e.getChannel.close()
    }
  }

  def stop () {
    channelServer.close().awaitUninterruptibly()
    bootstrapServer.releaseExternalResources();
  }

}

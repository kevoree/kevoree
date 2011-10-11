package org.kevoree.library.sky.minicloud.http

import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.util.CharsetUtil._
import org.jboss.netty.handler.codec.http.{DefaultHttpResponse, HttpRequest}
import org.kevoree.library.sky.minicloud.{VirtualNodeHTMLHelper, HttpServer}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 07:57
 * To change this template use File | Settings | File Templates.
 */

trait AdminRespond {

  def sendAdminNodeList(request: HttpRequest, server: HttpServer.Respond): Future[DefaultHttpResponse] = {
    val response = new DefaultHttpResponse(HTTP_1_1, OK)
    val htmlContent = VirtualNodeHTMLHelper.exportNodeListAsHTML()
    response.setContent(server.createBufferFromString(htmlContent))
    Future.value(response)
  }

  def sendNodeHome(nodeName: String, request: HttpRequest, server: HttpServer.Respond): Future[DefaultHttpResponse] = {
    val response = new DefaultHttpResponse(HTTP_1_1, OK)
    val htmlContent = VirtualNodeHTMLHelper.getNodeHomeAsHTML(nodeName)
    response.setContent(server.createBufferFromString(htmlContent))
    Future.value(response)
  }

  def sendNodeFlux(fluxName : String,nodeName: String, request: HttpRequest, server: HttpServer.Respond): Future[DefaultHttpResponse] = {
    val response = new DefaultHttpResponse(HTTP_1_1, OK)
    val htmlContent = VirtualNodeHTMLHelper.getNodeStreamAsHTML( nodeName,fluxName)
    response.setContent(server.createBufferFromString(htmlContent))
    Future.value(response)
  }

}
package org.kevoree.library.sky.minicloud

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.twitter.finagle.Service
import http.AdminRespond
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.util.Future
import org.kevoree.framework.KevoreeXmiHelper
import org.jboss.netty.buffer.ChannelBufferInputStream
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.io.ByteArrayOutputStream
import org.slf4j.{LoggerFactory, Logger}
import util.matching.Regex

object HttpServer {

  private val logger: Logger = LoggerFactory.getLogger(HttpServer.getClass)

  class Respond(handler: KevoreeModelHandlerService,nodeManager : KevoreeNodeManager) extends Service[HttpRequest, HttpResponse] with AdminRespond {

    def getNodeManager = nodeManager
      def createBufferFromString(content : String)= copiedBuffer(content.getBytes("UTF-8"))

    val NodeSubRequest = new Regex("/nodes/(.+)/(.+)")
    val NodeHomeRequest = new Regex("/nodes/(.+)")

    def apply(request: HttpRequest): Future[DefaultHttpResponse] = {

      request.getMethod match {
        case HttpMethod.GET => {
          request.getUri match {
            case "/" => sendAdminNodeList(request,this)
            case "/model/current" => sendModel(request)
            case NodeSubRequest(nodeName,fluxName)=> sendNodeFlux(fluxName,nodeName,request,this)
            case NodeHomeRequest(nodeName)=> sendNodeHome(nodeName,request,this)
            case _ => sendError(request)
          }
        }
        case HttpMethod.POST => {
          request.getUri match {
            case "/model/current" => receiveModel(request)
            case _ => sendError(request)
          }
        }
      }
    }


    private def sendError(request: HttpRequest): Future[DefaultHttpResponse] = {
      val response = new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST)
      response.setContent(copiedBuffer("Unknown request", UTF_8))
      Future.value(response)
    }

    private def sendModel(request: HttpRequest): Future[DefaultHttpResponse] = {
      val response = new DefaultHttpResponse(HTTP_1_1, OK)
      val output = new ByteArrayOutputStream
      val model = handler.getLastModel
      KevoreeXmiHelper.saveStream(output, model)
      response.setContent(copiedBuffer(output.toString("UTF-8"), UTF_8))
      Future.value(response)
    }

    private def receiveModel(request: HttpRequest): Future[DefaultHttpResponse] = {
      try {
        val model = KevoreeXmiHelper.loadStream(new ChannelBufferInputStream(request.getContent))
        val response = new DefaultHttpResponse(HTTP_1_1, OK)
        response.setContent(copiedBuffer("ok thanks for the model ;-)", UTF_8))
        handler.updateModel(model)
        Future.value(response)
      } catch {
        case _@e => {
          val response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND)
          response.setContent(copiedBuffer("error while loading model", UTF_8))
          Future.value(response)
        }
      }
    }
  }

}
package org.kevoree.library.sky.manager.http

import akka.actor.Actor
import org.slf4j.LoggerFactory
import org.kevoree.framework.AbstractNodeType
import cc.spray.can._
import util.matching.Regex

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/02/12
 * Time: 16:23
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class HTTPServerRoot (id: String, node: AbstractNodeType) extends Actor {
  val log = LoggerFactory.getLogger(getClass)
  self.id = id

  val NodeSubRequest = new Regex("/nodes/(.+)/(.+)")
  val NodeHomeRequest = new Regex("/nodes/(.+)")

  protected def receive = {

    case RequestContext(HttpRequest(HttpMethods.GET, "/favicon.ico", _, _, _), _, responder) =>
      responder.complete(response("Unknown resource!", 404))

    case RequestContext(HttpRequest(HttpMethods.GET, path, _, _, _), _, responder) => {
      val response = path match {
        case "/" => sendAdminNodeList()
        case NodeSubRequest(nodeName, fluxName) => sendNodeFlux(fluxName, nodeName)
        case NodeHomeRequest(nodeName) => sendNodeHome(nodeName)
        case _ => sendError()
      }
      responder.complete(response)
    }

    case Timeout(method, uri, _, _, _, complete) => complete {
      HttpResponse(status = 401).withBody("The " + method + " request to '" + uri + "' has timed out...")
    }

  }

  val defaultHeaders = List(HttpHeader("Content-Type", "text/html"))

  def response (msg: String, status: Int = 200) = HttpResponse(status, defaultHeaders, msg.getBytes("UTF-8"))

  private def sendAdminNodeList (): HttpResponse = {
    val htmlContent = VirtualNodeHTMLHelper.exportNodeListAsHTML()
    new HttpResponse(status = 200).withBody(htmlContent)
  }

  private def sendNodeHome (nodeName: String): HttpResponse = {
    val htmlContent = VirtualNodeHTMLHelper.getNodeHomeAsHTML(nodeName)
    new HttpResponse(status = 200).withBody(htmlContent)
  }

  private def sendNodeFlux (fluxName: String, nodeName: String): HttpResponse = {
    val htmlContent = VirtualNodeHTMLHelper.getNodeStreamAsHTML(nodeName, fluxName)
    new HttpResponse(status = 200).withBody(htmlContent)
  }

  private def sendError (): HttpResponse = {
    new HttpResponse(status = 400).withBody("Unknown request !")
  }
}

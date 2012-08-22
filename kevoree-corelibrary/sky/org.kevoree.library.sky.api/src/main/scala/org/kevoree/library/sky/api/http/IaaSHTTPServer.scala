package org.kevoree.library.sky.api.http

import org.kevoree.library.webserver.internal.KTinyWebServerInternalServe
import org.kevoree.library.sky.api.nodeType.IaaSNode
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import util.matching.Regex
import org.kevoree.framework.FileNIOHelper

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/05/12
 * Time: 17:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class IaaSHTTPServer (node: IaaSNode) extends Runnable {
  val logger = LoggerFactory.getLogger(this.getClass)
  private var srv: KTinyWebServerInternalServe = null
  private var mainT: Thread = null
  val NodeSubRequest = new Regex("/nodes/(.+)/(.+)")
  val NodeHomeRequest = new Regex("/nodes/(.+)")

  def startServer (port: Int) {
    srv = new KTinyWebServerInternalServe
    val properties: Properties = new Properties
    properties.put("port", new Integer(port))
    properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup")
    srv.arguments = properties
    mainT = new Thread(this)
    mainT.start()
    srv.addServlet("/*", new HttpServlet {
      protected override def service (req: HttpServletRequest, resp: HttpServletResponse) {
        req.getRequestURI match {
          case "/" => sendAdminNodeList(req, resp)
          case "/bootstrap.min.css" => sendFile(req, resp, FileNIOHelper.getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap.min.css")), "text/css")
          case "/scaled500.png" => sendFile(req, resp, FileNIOHelper.getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("scaled500.png")), "image/png")
          case NodeSubRequest(nodeName, fluxName) => sendNodeFlux(req, resp, fluxName, nodeName)
          case NodeHomeRequest(nodeName) => sendNodeHome(req, resp, nodeName)
          case _ => sendError(req, resp)
        }
      }
    })
  }

  def stopServer () {
    srv.notifyStop()
    srv.destroyAllServlets()
    mainT.interrupt()
  }

  def run () {
    srv.serve
  }

  private def sendAdminNodeList (req: HttpServletRequest, resp: HttpServletResponse) {
    val htmlContent = VirtualNodeHTMLHelper.exportNodeListAsHTML(node.getNodeManager)
    resp.setStatus(200)
    resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
  }

  private def sendNodeHome (req: HttpServletRequest, resp: HttpServletResponse, nodeName: String) {
    val htmlContent = VirtualNodeHTMLHelper.getNodeHomeAsHTML(nodeName, node.getNodeManager)
    resp.setStatus(200)
    resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
  }

  private def sendNodeFlux (req: HttpServletRequest, resp: HttpServletResponse, fluxName: String, nodeName: String) {
    val htmlContent = VirtualNodeHTMLHelper.getNodeStreamAsHTML(nodeName, fluxName, node.getNodeManager)
    resp.setStatus(200)
    resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
  }

  private def sendError (req: HttpServletRequest, resp: HttpServletResponse) {
    resp.setStatus(400)
    resp.getOutputStream.write("Unknown Requt!".getBytes("UTF-8"))
  }

  private def sendFile(req: HttpServletRequest, resp: HttpServletResponse, bytes : Array[Byte], contentType : String) {
    resp.setStatus(200)
    resp.setContentType(contentType)
    resp.getOutputStream.write(bytes)
  }
}

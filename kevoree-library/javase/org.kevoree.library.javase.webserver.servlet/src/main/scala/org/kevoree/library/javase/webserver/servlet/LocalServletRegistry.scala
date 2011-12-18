package org.kevoree.library.javase.webserver.servlet

import javax.servlet.http.HttpServlet
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import java.io.InputStream
import xml.XML
import org.osgi.framework.Bundle
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */

class LocalServletRegistry(bundle: Bundle = null) {

  private val servlets = new scala.collection.mutable.HashMap[String, AbstractHttpServletPage]
  private val logger = LoggerFactory.getLogger(this.getClass)
  
  def registerServlet(urlpattern: String, servletClass: HttpServlet) {
    val servletWrapper = new AbstractHttpServletPage {
      def initServlet() {
        this.legacyServlet = servletClass
      }
    }
    servletWrapper.getDictionary.put("urlpattern", "**" + urlpattern)
    servletWrapper.startPage()
    servlets.put(urlpattern, servletWrapper)
    logger.debug("Subscript servlet for url "+"**" + urlpattern+" => "+servletClass.getClass.getSimpleName)
  }

  def unregisterUrl(urlpattern: String) {
    servlets.get(urlpattern).map {
      oldServlet =>
        oldServlet.stopPage()
        servlets.remove(urlpattern)
    }
  }

  def tryURL(url: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    servlets.keySet.find(urlP => url.endsWith(urlP)) match {
      case Some(wrapperServletKey) => {
        servlets.get(wrapperServletKey).get.process(request, response)
        true
      }
      case None => false
    }
  }

  def loadWebXml(st: InputStream) {
    try {
      val xmlnode = XML.load(st)
      xmlnode.child.foreach {
        cNode =>
          cNode.label match {
            case "servlet" => {
              val servletID = cNode.child.find(c => c.label == "servlet-name").get.text
              val servletClass = cNode.child.find(c => c.label == "servlet-class").get.text
              val patternMapping = xmlnode.child.find(c => c.label == "servlet-mapping" && c.child.exists(cc => cc.label == "servlet-name" && cc.text == servletID))
              val pattern = patternMapping.get.child.find(c => c.label == "url-pattern").get.text
              val classServlet = bundle.loadClass(servletClass)
              val servlet = classServlet.newInstance()
              registerServlet(pattern,servlet.asInstanceOf[HttpServlet])
            }
            case _ =>
          }
      }
    } catch {
      case _@e => logger.warn("Error during load of web.xml",e)
    }


  }


}
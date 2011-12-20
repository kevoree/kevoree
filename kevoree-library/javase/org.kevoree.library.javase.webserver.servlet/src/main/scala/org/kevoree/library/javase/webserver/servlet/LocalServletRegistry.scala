package org.kevoree.library.javase.webserver.servlet

import javax.servlet.http.HttpServlet
import java.io.InputStream
import xml.XML
import org.osgi.framework.Bundle
import org.slf4j.LoggerFactory
import org.kevoree.library.javase.webserver.{URLHandlerScala, KevoreeHttpResponse, KevoreeHttpRequest}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */

class LocalServletRegistry(bundle: Bundle = null) {

  private val servlets = new scala.collection.mutable.HashMap[URLHandlerScala, AbstractHttpServletPage]
  private val logger = LoggerFactory.getLogger(this.getClass)
  
  def registerServlet(urlpattern: String, servletClass: HttpServlet) {
    val servletWrapper = new AbstractHttpServletPage {
      def initServlet() {
        this.legacyServlet = servletClass
      }
    }
    servletWrapper.getDictionary.put("urlpattern", "**" + urlpattern)
    servletWrapper.startPage()
    val up = new URLHandlerScala
    up.initRegex("**" + urlpattern)
    servlets.put(up, servletWrapper)
    logger.debug("Subscript servlet for url "+"**" + urlpattern+" => "+servletClass.getClass.getSimpleName)
  }

  def unregisterUrl(urlpattern: String) {
    servlets.find(p=> p._1 == "**" + urlpattern).map {
      s =>
        s._2.stopPage()
        servlets.remove(s._1)
    }
  }

  def tryURL(url: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    servlets.keySet.find(urlP => urlP.precheck(url)) match {
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
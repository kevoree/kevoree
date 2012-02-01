package org.kevoree.library.javase.webserver.servlet

import javax.servlet.http.HttpServlet
import java.io.InputStream
import org.slf4j.LoggerFactory
import org.kevoree.library.javase.webserver.{URLHandlerScala, KevoreeHttpResponse, KevoreeHttpRequest}
import scala.xml.XML
import java.lang.String
import java.net.URL
import javax.servlet.{ServletContext, ServletContextEvent, ServletContextListener}
import java.util.{Set}
import io.Source

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */

class LocalServletRegistry {

  private val servlets = new scala.collection.mutable.HashMap[URLHandlerScala, AbstractHttpServletPage]
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getCDefaultPath : String = "/"
  
  private val sharedCTX = new FakeServletContext{
    override def getResourcePaths(path: String): Set[String] = {
      val s = new java.util.HashSet[String]()
      import scala.collection.JavaConversions._
      //bundle.getEntryPaths(path).foreach(m => s.add(m.toString))
      s
    }


    override def getContextPath: String = {
      getCDefaultPath
    }

    override def getResource(path: String): URL = {
      //println(path)
      //bundle.getResource(path)
      null
    }

    override def getResourceAsStream(path: String): InputStream = {
      //println(path)
     // bundle.getResource(path).openStream()
      null
    }
  }
  

  def registerServlet(urlpattern: String, servletClass: HttpServlet) {
    val servletWrapper = new AbstractHttpServletPage {
      def initServlet() {
        this.legacyServlet = servletClass
      }

      def getSharedServletContext: ServletContext = {
        sharedCTX
      }
    }



    servletWrapper.getDictionary.put("urlpattern", "**" + urlpattern)
    servletWrapper.startPage()
    val up = new URLHandlerScala
    up.initRegex("**" + urlpattern)
    servlets.put(up, servletWrapper)
    logger.debug("Subscript servlet for url " + "**" + urlpattern + " => " + servletClass.getClass.getSimpleName)
  }

  def unregisterUrl(urlpattern: String) {
    servlets.find(p => p._1 == "**" + urlpattern).map {
      s =>
        s._2.stopPage()
        servlets.remove(s._1)
    }
  }

  def tryURL(url: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {

    if (logger.isDebugEnabled) {
      logger.debug("Servlet regsitry for url " + url)
      servlets.foreach {
        s =>
          logger.debug("=>" + s._1.LocalURLPattern.toString() + " -> " + s._1.precheck(url))
      }
    }

    servlets.keySet.find(urlP => urlP.precheck(url)) match {
      case Some(wrapperServletKey) => {
        servlets.get(wrapperServletKey).get.process(request, response)
        true
      }
      case None => false
    }
  }


  var listeners = List[ServletContextListener]()

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
              //val classServlet = bundle.loadClass(servletClass)
             // val servlet = classServlet.newInstance()
            //  registerServlet(pattern, servlet.asInstanceOf[HttpServlet])
            }

            case "listener" => {
              cNode.child.find(c => c.label == "listener-class").map {
                listener =>
                 // val newL = bundle.loadClass(listener.text).newInstance().asInstanceOf[ServletContextListener]
                  //listeners = listeners ++ List(newL)
              }
            }
            case _ =>
          }
      }
    } catch {
      case _@e => logger.warn("Error during load of web.xml", e)
    }

    listeners.foreach {
      l =>
        val sevent = new ServletContextEvent(sharedCTX)
        l.contextInitialized(sevent)
    }

  }

  def unload() {
    listeners.foreach {
      l =>
        val sevent = new ServletContextEvent(sharedCTX)
        l.contextDestroyed(sevent)
    }
    listeners = List()
  }


}
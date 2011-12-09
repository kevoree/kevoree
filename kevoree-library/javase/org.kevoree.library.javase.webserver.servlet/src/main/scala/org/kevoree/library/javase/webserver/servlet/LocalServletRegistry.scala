package org.kevoree.library.javase.webserver.servlet

import javax.servlet.http.HttpServlet
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */

class LocalServletRegistry {

  private val servlets = new scala.collection.mutable.HashMap[String, AbstractHttpServletPage]

  def registerServlet(urlpattern: String, servletClass: HttpServlet) {
    val servletWrapper = new AbstractHttpServletPage {
      def initServlet() {
        this.legacyServlet = servletClass
      }
    }
    servletWrapper.getDictionary.put("urlpattern","**"+urlpattern)
    servletWrapper.startPage()
    servlets.put(urlpattern, servletWrapper)
  }

  def unregisterUrl(urlpattern: String) {
    servlets.get(urlpattern).map {
      oldServlet =>
        oldServlet.stopPage()
        servlets.remove(urlpattern)
    }
  }

  def tryURL(url:String, request: KevoreeHttpRequest , response: KevoreeHttpResponse ): Boolean = {
    servlets.keySet.find(urlP => url.endsWith(urlP)) match {
      case Some(wrapperServletKey)=> {
        servlets.get(wrapperServletKey).get.process(request,response)
        true
      }
      case None => false
    }
  }


}
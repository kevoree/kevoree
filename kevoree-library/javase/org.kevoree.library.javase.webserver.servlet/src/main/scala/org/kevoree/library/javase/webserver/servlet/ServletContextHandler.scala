package org.kevoree.library.javase.webserver.servlet

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 22:55
 * To change this template use File | Settings | File Templates.
 */

object ServletContextHandler {

  private val context = new FakeServletContext

  def getContext = context

}
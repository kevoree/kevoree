package org.kevoree.library.javase.webserver.servlet

import javax.servlet.http.HttpServlet
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/12/11
 * Time: 08:12
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  println("Hi")


  val lr = new LocalServletRegistry

  lr.registerServlet("/*",new HttpServlet{})
//  println(lr.tryURL("/",new KevoreeHttpRequest,new KevoreeHttpResponse))



}
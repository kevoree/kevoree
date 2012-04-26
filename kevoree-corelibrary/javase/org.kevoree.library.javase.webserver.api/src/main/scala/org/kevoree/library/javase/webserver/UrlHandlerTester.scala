package org.kevoree.library.javase.webserver

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/03/12
 * Time: 13:00
 */

object UrlHandlerTester extends App {

  val h = new URLHandlerScala
  
  println(h.getLastParam("/core/t","/{p1}/**"))
  
}

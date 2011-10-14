package org.kevoree.library.javase.webserver

import java.util.HashMap
import util.matching.Regex

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/10/11
 * Time: 23:23
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {
          /*
  val server = new WebServer
  val prop = new HashMap[String,Object]()
  prop.put("port","7000")
  server.setDictionary(prop)

  server.start()      */


  val Regex = new Regex("\\/(\\w+)/(\\w+)")
  
  "/ti112" match {
    case Regex(e)=> println(e)
    case _ =>
  }



  /*

  val urlHandler = new URLHandlerScala
  urlHandler.initRegex("/{titi}")

  println(urlHandler.check("/myURll") )
      */
}
package org.kevoree.library.javase.webserver

import java.util.HashMap
import util.matching.Regex
import java.util.regex.Pattern

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


val urlHandler = new URLHandlerScala
urlHandler.initRegex("*/{titi}")

  val ask = new KevoreeHttpRequest
  ask.setUrl("previousPAth/bou")

println(urlHandler.check(ask).get.asInstanceOf[KevoreeHttpRequest].getResolvedParams )

}
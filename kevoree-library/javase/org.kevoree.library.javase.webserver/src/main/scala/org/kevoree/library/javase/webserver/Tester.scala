package org.kevoree.library.javase.webserver

import java.util.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/10/11
 * Time: 23:23
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  val server = new WebServer
  val prop = new HashMap[String,Object]()
  prop.put("port","7000")
  server.setDictionary(prop)

  server.start()

}
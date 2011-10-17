package org.kevoree.library.javase.webserver

import collection.immutable.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/10/11
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */

object GetParamsParser {

  def getParams(queryString: String): Tuple2[String, java.util.HashMap[String, String]] = {
    var params = new java.util.HashMap[String, String]()
    val urlParts = queryString.split("\\?")
    if (urlParts.size > 1) {
      val arrParameters = urlParts(1).split("&")
      arrParameters.foreach { p =>
        val pair = p.toString.split("=")
        params.put(pair(0),pair(1))
      }
      (urlParts(0).toString, params)
    } else {
      (queryString,params)
    }
  }

}
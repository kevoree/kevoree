package org.kevoree.library.javase.webserver

import cc.spray.can.HttpHeader
import org.slf4j.LoggerFactory


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/10/11
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */

object GetParamsParser {
  private val logger = LoggerFactory.getLogger(getClass)

  def getParams (queryString: String): Tuple2[String, java.util.HashMap[String, String]] = {
    val params = new java.util.HashMap[String, String]()
    val urlParts = queryString.split("\\?")
    if (urlParts.size > 1) {
      val arrParameters = urlParts(1).split("&")
      arrParameters.foreach {
        p =>
          val pair = p.toString.split("=")
          if (pair.size >= 2) {
            params.put(URLUtil.unescape(pair(0)), URLUtil.unescape(pair(1)))
          }
      }
      (urlParts(0).toString, params)
    } else {
      (queryString, params)
    }
  }

  def getParams (headers: List[HttpHeader], body: Array[Byte]): java.util.HashMap[String, String] = {
    headers.find {
      h => h.name == "Content-Type"
    } match {
      case Some(header) => {
        // manage multipart enctype form
        if (header.value.contains("multipart/form-data; boundary=")) {
          val params = new java.util.HashMap[String, String]()
          val boundary = header.value.split("multipart/form-data; boundary=")(1)
          new String(body).replaceAll("\r\n", "\n").split("--" + boundary).foreach {
            content =>
              if (!content.equals("") && !content.equals("--") && !content.equals("--\n")) {
                try {
                  val key = content.substring("Content-Disposition: form-data; name=\"".length() + 1, content
                    .indexOf("\"", "Content-Disposition: form-data; name=\"".length() + 1))
                  val value = content.substring(content.indexOf("\n\n") + 2, content.lastIndexOf("\n"))
                  params.put(key, value)
                } catch {
                  case _@e => logger.error("unable to parser HTTP request", e)
                }
              }
          }
          params
        } /*else {
          getParams("?" + new String(body))._2
        }*/
        new java.util.HashMap[String, String](0)
      }
      case None => new java.util.HashMap[String, String](0)//getParams("?" + new String(body))._2
    }
  }

}
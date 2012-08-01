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

  def getParams (queryString: String): (String, java.util.HashMap[String, String]) = {
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
      h => h.name.toLowerCase == "Content-Type".toLowerCase
    } match {
      case Some(header) if (header.value.toLowerCase.startsWith("multipart/form-data;")) => {
        // manage multipart enctype form
        val params = new java.util.HashMap[String, String]()
        val boundary = header.value.split("multipart/form-data; boundary=")(1)
        new String(body, "UTF-8").replaceAll("\r\n", "\n").split("--" + boundary).foreach {
          content =>
            if (content.startsWith("\nContent-Disposition: form-data;")) {
              try {
                val key = content.substring("\nContent-Disposition: form-data; name=\"".length(), content.indexOf("\"", "Content-Disposition: form-data; name=\"".length() + 1))

                val value = content.substring(content.indexOf("\n\n") + 2, content.lastIndexOf("\n"))

                params.put(key, value)
              } catch {
                case _@e => logger.error("unable to parser HTTP request", e)
              }
            } else {
              if (content != "" || content != "--") {}
              logger.error("Unrecognized HTTP body:\n\"{}\"", content)
            }
        }
        params
      }
      case Some(header) if (header.value.toLowerCase == "application/x-www-form-urlencoded".toLowerCase) => {
        getParams("?" + new String(body, "UTF-8"))._2
      }
      case Some(header) => {
        logger.debug("The web server is currently not able to manage this kind of content-type: \"{}\"\nto get parameters on the body: \"{}\"", header._2, new String(body, "UTF-8"))
        new java.util.HashMap[String, String](0)
      }
      case None => new java.util.HashMap[String, String](0)
    }
  }

}
package org.kevoree.library.javase.webserver

import util.matching.Regex
import java.util.HashMap
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 09:04
 * To change this template use File | Settings | File Templates.
 */

class URLHandlerScala {
  val logger = LoggerFactory.getLogger(this.getClass)
  var LocalURLPattern = new Regex("/")

  def initRegex(pattern: String) {
     //TODO
  }

  def check(url: Any): Option[KevoreeHttpRequest] = {
    url match {
      case request :  KevoreeHttpRequest => {
        request.getUrl match {
           case LocalURLPattern() => {
             val params = new HashMap[String, String]
             request.setResolvedParams(params)
             Some(request)
           }
           case _ => { logger.debug("Bad Pattern "+request.getUrl) ; None }
         }
      }
      case _ => None
    }
  }
}
package org.kevoree.library.javase.webserver

import util.matching.Regex
import java.util.HashMap
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

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
  var paramNames = List[String]()

  def initRegex(pattern: String) {
    paramNames = List()
    val m = Pattern.compile("\\{(\\w+)\\}").matcher(pattern)
    val sb = new StringBuffer();
    val rsb = new StringBuffer()
    while (m.find) {
      paramNames = paramNames ++ List(m.group(1))
      rsb.replace(0, rsb.length, m.group(1));
      m.appendReplacement(sb, "(\\\\w+)")
    }
    m.appendTail(sb)
    val regexText = sb.toString.replaceAll("\\*",".*")
    LocalURLPattern = new Regex(regexText)
  }

  def check(url: Any): Option[KevoreeHttpRequest] = {
    url match {
      case request: KevoreeHttpRequest => {

        LocalURLPattern.unapplySeq(request.getUrl) match {
          case Some(paramsList) => {
            val params = new HashMap[String, String]
            params.putAll(request.getResolvedParams)
            var i = 0
            paramsList.foreach{ param =>
              params.put(paramNames(i),param)
              i = i +1
            }
            request.setResolvedParams(params)
            Some(request)
          }
          case _ => {
            logger.debug("Bad Pattern " + request.getUrl);
            None
          }
        }
      }
      case _ => None
    }
  }
}
package org.kevoree.library.channels

import org.kevoree.extra.osgi.rxtx.ContentListener
import util.matching.Regex
import org.kevoree.framework.message.Message
import org.kevoree.framework.ChannelFragment
import scala.collection.JavaConversions._
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: ffouquet
 * Date: 17/08/11
 * Time: 08:59
 */

class KContentListener(cf: ChannelFragment) extends ContentListener {
  private final val logger: Logger = LoggerFactory.getLogger(classOf[KContentListener])
  val KevSerialMessageRegex = new Regex("(.+):(.+)\\[(.*)\\]")

  def recContent(content: String) {
    //if (content.contains("\n")) {
      content.trim() match {
        case KevSerialMessageRegex(srcChannelName, nodeName, contentBody) => {
          val message = new Message();
          val buffer = new StringBuffer()
          var metric: String = null
          contentBody.trim().split('/').foreach {
            v =>
              if (metric == null) {
                metric = v
              } else {
                if (buffer.length() > 0) {
                  buffer.append(",")
                }
                buffer.append(metric)
                buffer.append("=")
                buffer.append(v)
              }
          }
          message.content = buffer.toString
          message.inOut = false
          message.passedNodes.add(nodeName);
          if (cf.getOtherFragments.exists(ofrag => ofrag.getName == srcChannelName)) {
            cf.remoteDispatch(message);
          }
        }
        case _@e => {
          logger.debug("Msg format error => " + content.trim() + "\nMsg lost!", e)
        }
      }
   // }
  }
}
package org.kevoree.library.rest.consensus

import cc.spray.can._
import org.kevoree.library.rest.{RootService, RestConsensusGroup}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 09:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class ConsensusRootService (id: String, group: RestConsensusGroup) extends RootService(id, group) {

  override protected def receive = {
    super.receive.orElse {

      // ConsensusRootService specific cases
      case RequestContext(HttpRequest(HttpMethods.GET, uri, _, _, _), _, responder) if (uri.startsWith("/model/consensus/lock")) => {
        // get parameters which must be currentModel and futureModel
        val currentHashModel = getParam(uri, "currentModel").getBytes("UTF-8")
        val futureHashModel = getParam(uri, "futureModel").getBytes("UTF-8")
        // call the lock on the node
        val hashes = group.lock(currentHashModel, futureHashModel)
        val body = "currentModel" + "=" + new String(hashes._1, "UTF-8") + "\n" + "futureModel" + "=" + new String(hashes._2, "UTF-8") + "\n"
        responder.complete(response(body))
      }
      case RequestContext(HttpRequest(HttpMethods.GET, uri, _, _, _), _, responder) if (uri.startsWith("/model/consensus/hash")) => {
        val hashes = group.hashes()
        val body = "currentModel" + "=" + new String(hashes._1, "UTF-8") + "\n" + "futureModel" + "=" + new String(hashes._2, "UTF-8") + "\n"
        responder.complete(response(body))
      }
        // ConsensusRootService specific cases
      case RequestContext(HttpRequest(HttpMethods.GET, uri, _, _, _), _, responder) if (uri.startsWith("/model/consensus/unlock")) => {
        // call the unlock on the node
        group.unlock()
        val body = "<unlock nodeName=\"" + group.getNodeName + "\" />"
        responder.complete(response(body))
      }
    }
  }

  private def getParam (uri: String, parameterName: String): String = {
    val urlParts = uri.split("\\?")
    if (urlParts.size > 1) {
      val arrParameters = urlParts(1).split("&")
      var value = ""
      arrParameters.forall {
        p =>
          val pair = p.toString.split("=")
          if (pair.size >= 2 && pair(0) == parameterName) {
            value = pair(1)
            false
          } else {
            true
          }
      }
      value
    } else {
      ""
    }
  }


}
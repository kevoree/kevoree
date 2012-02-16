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
        val remoteHash = java.lang.Long.parseLong(getParam(uri, "hash"))
        //        val futureHashModel = getParam(uri, "futureModel").getBytes("UTF-8")
        // call the lock on the node
        val isLock = group.lock(remoteHash)
        var body = ""
        if (isLock) {
          body = "<lock nodeName=\"" + group.getNodeName + "\" hash" + "=\"" + group.getHash + "\" />"
        } else {
          body = "<unlock nodeName=\"" + group.getNodeName + "\" />"
        }
        responder.complete(response(body))
      }
      case RequestContext(HttpRequest(HttpMethods.GET, uri, _, _, _), _, responder) if (uri.startsWith("/model/consensus/hash")) => {
        val body = "<hash nodeName=\"" + group.getNodeName + "\">" + group.getHash + "</hash>"
        responder.complete(response(body))
      }
      // ConsensusRootService specific cases
      case RequestContext(HttpRequest(HttpMethods.GET, uri, _, _, _), _, responder) if (uri.startsWith("/model/consensus/unlock")) => {
        // call the unlock on the node
        val newHash = group.unlock(java.lang.Long.parseLong(getParam(uri, "hash")))
        val body = "<unlock nodeName=\"" + group.getNodeName + "\" hash=" + newHash + "\" />"
        responder.complete(response(body))
      }
    }
  }
}
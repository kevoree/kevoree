package org.kevoree.library.rest

import cc.spray.can._
import org.kevoree.framework.KevoreeXmiHelper

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
    case RequestContext(HttpRequest(HttpMethods.GET, "/favicon.ico", _, _, _), _, responder) =>
      responder.complete(response("Unknown resource!", 404))

    case RequestContext(HttpRequest(HttpMethods.GET, "/model/current", _, _, _), _, responder) => {
      responder.complete(response(group.getModel))
    }

    case RequestContext(HttpRequest(HttpMethods.GET, url, _, _, _), _, responder) if (url.startsWith("/provisioning")) => {
      responder.complete(getResponse(url))
    }

    case RequestContext(HttpRequest(HttpMethods.POST, url, _, body, _), _, responder) => {
      try {
        val model = KevoreeXmiHelper.loadString(new String(body))
        new scala.actors.Actor {
          def act () {
            group.updateModel(model)
          }
        }.start()
        responder.complete(response("<ack nodeName=\"" + group.getNodeName + "\" />"))
      } catch {
        case _@e => {
          log.error("Error while uploading model from group " + group.getName, e)
          responder.complete(HttpResponse(status = 501).withBody("Error while uploading model "))
        }
      }
    }

    case Timeout(method, uri, _, _, _, complete) => complete {
      HttpResponse(status = 401).withBody("The " + method + " request to '" + uri + "' has timed out...")
    }

      // ConsensusRootService specific cases
    case RequestContext(HttpRequest(HttpMethods.GET, "/model/lock", _, _, _), _, responder) => {
      // call the lock on the node
      if (group.lock()) {
        responder.complete(response(new String(group.getHashedModel)))
      } else {
        responder.complete(response(""))
      }
    }
  }

}
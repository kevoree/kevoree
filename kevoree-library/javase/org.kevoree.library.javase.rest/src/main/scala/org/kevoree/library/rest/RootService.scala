package org.kevoree.library.rest

/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.slf4j.LoggerFactory
import akka.actor.Actor
import cc.spray.can._
import java.util.UUID
import org.kevoree.framework.{KevoreeXmiHelper, MessagePort}

class RootService(id: String, group: RestGroup) extends Actor {
  val log = LoggerFactory.getLogger(getClass)
  self.id = id

  protected def receive = {

    case RequestContext(HttpRequest(HttpMethods.GET, "/favicon.ico", _, _, _), _, responder) =>
      responder.complete(response("Unknown resource!", 404))

    case RequestContext(HttpRequest(HttpMethods.GET, "/model/current", _, _, _), _, responder) => {
      val modelSerialized = KevoreeXmiHelper.saveToString(group.getModelService.getLastModel, false)
      responder.complete(response(modelSerialized))
    }

    case RequestContext(HttpRequest(HttpMethods.POST, url, _, body, _), _, responder) => {
      try {
         val model = KevoreeXmiHelper.loadString(new String(body))
         group.getModelService.updateModel(model)
         responder.complete(response("<ack nodeName=\"" + group.getNodeName + "\" />"))
      } catch {
        case _ @ e => {
          log.error("Error while uploading model from group "+group.getName,e)
          responder.complete(HttpResponse(status = 501).withBody("Error while uploading model "))
        }
      }
    }

    case Timeout(method, uri, _, _, _, complete) => complete {
      HttpResponse(status = 401).withBody("The " + method + " request to '" + uri + "' has timed out...")
    }

  }

  val defaultHeaders = List(HttpHeader("Content-Type", "text/html"))

  def response(msg: String, status: Int = 200) = HttpResponse(status, defaultHeaders, msg.getBytes("UTF-8"))

}
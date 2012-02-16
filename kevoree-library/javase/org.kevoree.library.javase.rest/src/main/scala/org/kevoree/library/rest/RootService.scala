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
import org.kevoree.framework.KevoreeXmiHelper

class RootService (id: String, group: RestGroup) extends Actor with FileServer {
  val log = LoggerFactory.getLogger(getClass)
  self.id = id

  protected def receive = {

    case RequestContext(HttpRequest(HttpMethods.GET, "/favicon.ico", _, _, _), _, responder) =>
      responder.complete(response("Unknown resource!", 404))

    case RequestContext(HttpRequest(HttpMethods.GET, "/model/current", _, _, _), _, responder) => {
      responder.complete(response(group.getModel))
    }

    case RequestContext(HttpRequest(HttpMethods.GET, url, _, _, _), _, responder) if (url.startsWith("/provisioning")) => {
      responder.complete(getResponse(url))
    }

    case RequestContext(HttpRequest(HttpMethods.POST, url, _, body, _), _, responder) if (url.startsWith("/model/current")) => {
      try {
        val hashString = getParam(url, "hash")
        if (hashString != "") {
          val hash = java.lang.Long.parseLong(hashString)
          if (hash != group.getHash) {
            val model = KevoreeXmiHelper.loadString(new String(body))
            if (group.updateModel(model)) {
              responder.complete(response("<ack nodeName=\"" + group.getNodeName + "\" />"))
            } else {
              responder.complete(response("<nack nodeName=\"" + group.getNodeName + "\" />"))
            }
          } else {
            responder.complete(response("<ack nodeName=\"" + group.getNodeName + "\" />"))
          }
        } else {
          val model = KevoreeXmiHelper.loadString(new String(body))
          if (group.updateModel(model)) {
            responder.complete(response("<ack nodeName=\"" + group.getNodeName + "\" />"))
          } else {
            responder.complete(response("<nack nodeName=\"" + group.getNodeName + "\" />"))
          }
        }
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

  }

  val defaultHeaders = List(HttpHeader("Content-Type", "text/html"))

  def response (msg: String, status: Int = 200) = HttpResponse(status, defaultHeaders, msg.getBytes("UTF-8"))

  def getParam (uri: String, parameterName: String): String = {
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
package org.kevoree.library.javase.webserver

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

import impl.KevoreeHttpRequestImpl
import org.slf4j.LoggerFactory
import akka.actor.Actor
import cc.spray.can._
import org.kevoree.framework.MessagePort
import java.util.UUID
import scala.collection.JavaConversions._
import java.util

class RootService (id: String, request: MessagePort, bootstrap: ServerBootstrap, timeout: Long) extends Actor {
  val log = LoggerFactory.getLogger(getClass)
  self.id = id

  case class GARBAGE (minLastCheck: Long)

  case class RequestResponderTuple (responder: RequestResponder, uuid: Int, time: Long)

  class ResponseActor extends akka.actor.Actor {
    var map: scala.collection.mutable.HashMap[Int, (RequestResponder, Long)] = scala.collection.mutable
      .HashMap[Int, (RequestResponder, Long)]()

    def receive = {
      case GARBAGE(minLastCheck) => {
        log.debug("Garbage begin, cache size " + map.size)
        map.foreach {
          elem =>
            if (elem._2._2 < minLastCheck) {
              log.debug("Drop key " + elem._1)
              map.remove(elem._1)
            }
        }
        log.debug("Garbage finish, cache size " + map.size)
      }
      case msg: org.kevoree.library.javase.webserver.KevoreeHttpResponse => {
        map.get(msg.getTokenID) match {
          case Some(responder) => {

            var headers: List[HttpHeader] = List()
            headers = headers ++ List(HttpHeader("Content-Type", msg.getHeaders.get("Content-Type")))
            import scala.collection.JavaConversions._
            msg.getHeaders.foreach {
              header => {
                headers = headers ++ List(HttpHeader(header._1, header._2))
              }
            }

            if (msg.getRawContent != null) {
              responder._1.complete(rawResponse(msg.getRawContent, msg.getStatus, headers))
            } else {
              responder._1.complete(response(msg.getContent, msg.getStatus, headers))
            }
            map.remove(msg.getTokenID)
          }
          case None => log.error("responder not found for tokenID=" + msg.getTokenID)
        }

        //TEST IF FINAL
      }
      case rr: RequestResponderTuple => {
        map.put(rr.uuid, (rr.responder, rr.time))
      }
      case _ =>
    }
  }

  val actorRef = Actor.actorOf(new ResponseActor)
  actorRef.start()
  bootstrap.setResponseActor(actorRef)

  private val random = new util.Random()

  protected def receive = {

    case RequestContext(HttpRequest(HttpMethods.GET, "/favicon.ico", _, _, _), _, responder) =>
      responder.complete(response("Unknown resource!", 404, defaultHeaders))

    case RequestContext(HttpRequest(HttpMethods.GET, url, headers, _, _), _, responder) =>
      val kevMsg = new KevoreeHttpRequestImpl
      kevMsg.setMethod("GET")
      kevMsg.setTokenID(random.nextInt())
      actorRef ! RequestResponderTuple(responder, kevMsg.getTokenID, System.currentTimeMillis())
      val paramsRes = GetParamsParser.getParams(url)
      kevMsg.setUrl(paramsRes._1)
      kevMsg.setCompleteUrl("http://" + headers.find(header => header._1.equalsIgnoreCase("host")).getOrElse(("", ""))._2 + url)
      kevMsg.setRawParams(defineRawParams(paramsRes._2))
      kevMsg.setResolvedParams(paramsRes._2)
      headers.foreach {
        header =>
          kevMsg.getHeaders.put(header._1, header._2)
      }
      request.process(kevMsg)

    case RequestContext(HttpRequest(HttpMethods.POST, url, headers, body, _), _, responder) =>
      val kevMsg = new KevoreeHttpRequestImpl
      kevMsg.setMethod("POST")
      kevMsg.setTokenID(random.nextInt())
      actorRef ! RequestResponderTuple(responder, kevMsg.getTokenID, System.currentTimeMillis())

      val paramsRes1 = GetParamsParser.getParams(url)
      val paramsRes = GetParamsParser.getParams(headers, body)
      kevMsg.setRawBody(body)
      kevMsg.setRawParams(defineRawParams(paramsRes1._2))
      kevMsg.setUrl(paramsRes1._1)
      paramsRes.putAll(paramsRes1._2)
      kevMsg.setResolvedParams(paramsRes)
      headers.foreach {
        header =>
          kevMsg.getHeaders.put(header._1, header._2)
      }
      request.process(kevMsg)

    case Timeout(method, uri, _, _, _, complete) => complete {
      actorRef ! GARBAGE(System.currentTimeMillis() - timeout)
      HttpResponse(status = 500).withBody("The " + method + " request to '" + uri + "' has timed out...")
    }
  }

  private def defineRawParams (params: java.util.HashMap[String, String]): String = {
    val stringBuilder = new StringBuilder()
    stringBuilder append "?"
    params.keySet().foreach {
      key =>
        if (stringBuilder.get(stringBuilder.length - 1) != '?') {
          stringBuilder append "&"
        }
        stringBuilder append key + "=" + params.get(key)
    }
    if (stringBuilder.size == 1) {
      stringBuilder.deleteCharAt(0)
    }
    stringBuilder.toString()
  }

  val defaultHeaders = List(HttpHeader("Content-Type", "text/html"))

  def rawResponse (msg: Array[Byte], status: Int = 200, headers: List[HttpHeader]) = HttpResponse(status, headers, msg)

  def response (msg: String, status: Int = 200, headers: List[HttpHeader]) = HttpResponse(status, headers,
                                                                                           msg.getBytes("UTF-8"))

  ////////////// helpers //////////////
  /*


lazy val serverActor = Actor.registry.actorsFor("spray-can-server").head



lazy val index = KevoreeHttpResponse(
headers = List(HttpHeader("Content-Type", "text/html")),
body =
<html>
  <body>
    <h1>Say hello to <i>spray-can</i>!</h1>
    <p>Defined resources:</p>
    <ul>
      <li><a href="/ping">/ping</a></li>
      <li><a href="/stream">/stream</a></li>
      <li><a href="/stats">/stats</a></li>
      <li><a href="/crash">/crash</a></li>
      <li><a href="/timeout">/timeout</a></li>
      <li><a href="/stop">/stop</a></li>
    </ul>
  </body>
</html>.toString.getBytes("ISO-8859-1")
)      */
}
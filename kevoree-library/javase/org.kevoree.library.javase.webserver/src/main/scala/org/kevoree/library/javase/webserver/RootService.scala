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

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import akka.actor.{PoisonPill, Scheduler, Kill, Actor}
import cc.spray.can._

class RootService(id: String) extends Actor {
  val log = LoggerFactory.getLogger(getClass)
  self.id = id

  protected def receive = {

    case RequestContext(HttpRequest(HttpMethods.GET, "/", _, _, _), _, responder) =>
      responder.complete(response("PONG!"))

    case RequestContext(HttpRequest(_, _, _, _, _), _, responder) =>
      responder.complete(response("Unknown resource!", 404))

    case Timeout(method, uri, _, _, _, complete) => complete {
      HttpResponse(status = 500).withBody("The " + method + " request to '" + uri + "' has timed out...")
    }
  }

  val defaultHeaders = List(HttpHeader("Content-Type", "text/plain"))
  def response(msg: String, status: Int = 200) = HttpResponse(status, defaultHeaders , msg.getBytes("ISO-8859-1"))

  ////////////// helpers //////////////
          /*


  lazy val serverActor = Actor.registry.actorsFor("spray-can-server").head



  lazy val index = HttpResponse(
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
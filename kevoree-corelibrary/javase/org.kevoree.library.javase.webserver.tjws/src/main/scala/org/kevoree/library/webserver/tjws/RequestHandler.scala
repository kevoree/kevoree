package org.kevoree.library.webserver.tjws

import org.slf4j.LoggerFactory
import org.kevoree.library.javase.webserver.{AbstractWebServer, KevoreeHttpRequest, KevoreeHttpResponse}
import org.kevoree.framework.{MessagePort, AbstractComponentType}
import collection.mutable.Stack
import actors.{Actor}
import collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/04/12
 * Time: 20:41
 */

case class CLOSE()

case class REMOVE(id: Int)

case class ENABLE(id: Int)

case class GetHandler()

class RequestHandler(origin: AbstractWebServer) extends Actor {
  def killActors() {
    this ! CLOSE()
    handlers.foreach{ handler =>
      handler ! CLOSE()
    }
  }

  val log = LoggerFactory.getLogger(this.getClass)
  var handlers = new Array[ResponseHandler](200)
  var freeIDS = new mutable.Stack[Int]()

  def staticInit(timeout : Int) {
    val pointer = this
    for (i <- 0 until handlers.length) {
      handlers(i) = new ResponseHandler(timeout, pointer)
      handlers(i).start()
      freeIDS.push(i)
    }
  }


  def sendAndWait(rr: KevoreeHttpRequest): KevoreeHttpResponse = {
    val handlerID = (this !? GetHandler()).asInstanceOf[Int]
    rr.setTokenID(handlerID)
    origin.getPortByName("handler", classOf[MessagePort]).process(rr)
    handlers(handlerID).sendAndWait(handlerID)
  }

  def internalSend(resp: KevoreeHttpResponse) {
    this ! resp
  }

  def act() {
    loop {
      react {
        case GetHandler() => {
          reply(freeIDS.pop())
        }
        case REMOVE(i) => {
          freeIDS.push(i)
        }
        case msg: KevoreeHttpResponse => {
          if (msg.getTokenID >= 0 && msg.getTokenID < handlers.size) {
            handlers(msg.getTokenID).checkAndReply(msg)
          }
        }
        case CLOSE() => exit()
      }
    }
  }
}

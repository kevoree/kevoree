package org.kevoree.library.webserver.tjws

import org.kevoree.library.javase.webserver.{KevoreeHttpRequest, KevoreeHttpResponse}
import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl
import actors.{Actor, TIMEOUT, DaemonActor}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/04/12
 * Time: 20:42
 */


class ResponseHandler(timeout: Long, origin: RequestHandler) extends Actor {

  def sendAndWait(index : Int): KevoreeHttpResponse = {
    (this !? ENABLE(index)).asInstanceOf[KevoreeHttpResponse]
  }

  def checkAndReply(res: KevoreeHttpResponse) {
    this ! res
  }

  def act() {
    loop {
      react {
        case ENABLE(index) => {
          val resultSender = sender
          reactWithin(timeout) {
            case res: KevoreeHttpResponse => {
              resultSender ! res
              origin ! REMOVE(index)
            }
            case TIMEOUT => {
              val result = new KevoreeHttpResponseImpl
              result.setTokenID(index)
              result.setStatus(504)
              result.setContent("Kevoree Server Timeout")
              resultSender ! result
              origin ! REMOVE(index)
            }
            case CLOSE() => exit()
          }
        }
        case CLOSE() => exit()
      }
    }

  }

}

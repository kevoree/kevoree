package org.kevoree.library.webserver.tjws

import org.kevoree.library.javase.webserver.{KevoreeHttpRequest, KevoreeHttpResponse}
import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl
import actors.{TIMEOUT, DaemonActor}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/04/12
 * Time: 20:42
 */

class ResponseHandler(in: KevoreeHttpRequest, timeout: Long, origin: RequestHandler) extends DaemonActor {

  def sendAndWait(): KevoreeHttpResponse = {
    (this !? true).asInstanceOf[KevoreeHttpResponse]
  }

  def checkAndReply(res: KevoreeHttpResponse) {
    this ! res
  }

  def act() {
    react {
      case true => {
        val resultSender = sender
        reactWithin(timeout) {
          case res: KevoreeHttpResponse => {
            resultSender ! res
            origin ! REMOVE(in.getTokenID)
            exit()
          };
          case TIMEOUT => {
            val result = new KevoreeHttpResponseImpl
            result.setTokenID(in.getTokenID)
            result.setContent("Kevoree Server Timeout")
            resultSender ! result
            origin ! REMOVE(in.getTokenID)
            exit()
          }
          case _ => {
            origin ! REMOVE(in.getTokenID)
            exit()
          }
        }
      }
      case _ => exit()
    }
  }

}

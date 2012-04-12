package org.kevoree.library.webserver.tjws

import actors.DaemonActor
import java.util.UUID
import org.slf4j.LoggerFactory
import org.kevoree.library.javase.webserver.{AbstractWebServer, KevoreeHttpRequest, KevoreeHttpResponse}
import org.kevoree.framework.{MessagePort, AbstractComponentType}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/04/12
 * Time: 20:41
 */

case class CLOSE()

class RequestHandler(origin: AbstractWebServer) extends DaemonActor {
  def killActor() {
    this ! CLOSE()
  }

  val log = LoggerFactory.getLogger(this.getClass)
  var map: scala.collection.mutable.HashMap[UUID, ResponseHandler] = scala.collection.mutable.HashMap[UUID, ResponseHandler]()

  def sendAndWait(rr: KevoreeHttpRequest): KevoreeHttpResponse = {
    val handler = new ResponseHandler(rr, 3000, this)
    handler.start()
    this ! Tuple2(rr.getTokenID, handler)
    origin.getPortByName("handler", classOf[MessagePort]).process(rr)
    handler.sendAndWait()
  }


  def act() {
    react {
      case msg: KevoreeHttpResponse => {
        map.get(msg.getTokenID) match {
          case Some(responder) => {
            map.get(msg.getTokenID).map(r => r.checkAndReply(msg))
            map.remove(msg.getTokenID)
          }
          case None => log.error("responder not found for tokenID=" + msg.getTokenID)
        }
        //TEST IF FINAL
      }
      case rr: Tuple2[UUID, ResponseHandler] => {
        map.put(rr._1, rr._2);
      }
      case CLOSE() => exit()
    }

  }
}

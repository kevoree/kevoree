package org.kevoree.library.webserver.tjws

import actors.DaemonActor
import org.kevoree.library.javase.webserver.{KevoreeHttpRequest, KevoreeHttpResponse}
import org.omg.CORBA.TIMEOUT
import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/04/12
 * Time: 20:42
 */

class ResponseHandler(in : KevoreeHttpRequest,timeout : Long,origin : RequestHandler) extends DaemonActor {

  def sendAndWait() : KevoreeHttpResponse = {
    println("BeforWait");
      (this !? true).asInstanceOf[KevoreeHttpResponse]
  }

  def checkAndReply(res : KevoreeHttpResponse){
    println("Reply ;-)");
    if (res.getTokenID.compareTo(in.getTokenID) == 0){
      this ! res
    }
  }

  def act() {
       reactWithin(timeout){
         case res : KevoreeHttpResponse => reply(res);exit()
         case t : TIMEOUT => {
           val result = new KevoreeHttpResponseImpl
           origin ! result
           exit()
         }
         case _ => exit()
       }
  }
}

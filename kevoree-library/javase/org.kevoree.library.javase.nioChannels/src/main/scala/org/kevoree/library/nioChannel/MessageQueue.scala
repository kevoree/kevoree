package org.kevoree.library.nioChannel

import actors.{DaemonActor, Actor}
import org.kevoree.framework.message.Message


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */

class MessageQueue extends DaemonActor {

  case class GET_MESSAGE(host: String, port: String)

  case class PUT_MESSAGE(host: String, port: String, msg: Message)
  
  case class STOP()
  
  def stop() = { this ! STOP() }

  def putMsg(host: String, port: String, msg: Message) = { this ! PUT_MESSAGE(host, port, msg) }

  def popMsg(host: String, port: String) : Message = { (this !? GET_MESSAGE(host,port)).asInstanceOf[Message]   }

  private val cache = new scala.collection.mutable.HashMap[String, List[Message]]()

  private def buildKey(host: String, port: String): String = {
    host + ":" + port
  }

  def act() {
    loop {
      react {
        case STOP() => exit()
        case GET_MESSAGE(host, port) => {
          val msgL = cache.get(buildKey(host,port)).getOrElse(List())
          if(msgL.isEmpty){
            reply(null)
           } else {
            val result = msgL.head
            cache.put(buildKey(host,port),msgL.tail)
            reply(result)
          }
        }
        case PUT_MESSAGE(host, port, msg) => {
          val msgL = cache.get(buildKey(host,port)).getOrElse(List())
          cache.put(buildKey(host,port),msgL++List(msg))
        }
      }
    }


  }
}
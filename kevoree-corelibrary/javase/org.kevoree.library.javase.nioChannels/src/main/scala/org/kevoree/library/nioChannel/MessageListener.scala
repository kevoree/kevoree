package org.kevoree.library.nioChannel

import actors.DaemonActor
import org.jboss.netty.channel.Channel
import org.kevoree.framework.message.Message

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/11/11
 * Time: 11:14
 */
class MessageListener(host: String, port: Int, channel: Channel, msgQ: MessageQueue) extends DaemonActor {

  case class STOP()
  
  def stopProcess = { this ! STOP() }
  
  def act() {
    loop {
      react {
        case STOP()=> exit() //TODO RECOVERY OF LOST MESSAGE
        case msg: Message => {
          try {
            channel.write(msg)
          } catch {
            case _@e => {
              msgQ.invalidChannel(host,port)
            }
          }

        }
      }
    }
  }

}
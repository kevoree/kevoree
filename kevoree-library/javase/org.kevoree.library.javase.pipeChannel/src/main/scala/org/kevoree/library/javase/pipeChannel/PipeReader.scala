package org.kevoree.library.javase.pipeChannel

import actors.DaemonActor
import org.kevoree.framework.message.Message
import java.io.{ObjectInputStream, File, FileInputStream}
import java.lang.Thread

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 08/11/11
 * Time: 13:25
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class PipeReader (pipeChannel: PipeChannel) extends DaemonActor {

  case class STOP ()

  case class RECEIVE_MESSAGE (msg: Message)

  var alive = true

  val inputStream: ObjectInputStream = new
      ObjectInputStream(new
          FileInputStream(new File(System.getProperty("java.io.tmpdir") + File.separator + pipeChannel.getName) + "_" +
            pipeChannel.getNodeName))
  
  new Thread() {
    override def run() {
      while (alive) {
        receiveMessage(inputStream.readObject.asInstanceOf[Message])
      }
      inputStream.close()
    }
  }

  start()

  def stop () {
    this ! STOP()
  }

  def receiveMessage (msg: Message) {
    this ! RECEIVE_MESSAGE(msg)
  }

  def act () {
    loop {
      react {
        case STOP() => stopInternals()
        case RECEIVE_MESSAGE(msg) => receiveInternals(msg)
      }
    }
  }

  private def stopInternals () {
    alive = false
    this.exit()
  }

  private def receiveInternals (msg: Message) {
    pipeChannel.forward(msg)
  }
}
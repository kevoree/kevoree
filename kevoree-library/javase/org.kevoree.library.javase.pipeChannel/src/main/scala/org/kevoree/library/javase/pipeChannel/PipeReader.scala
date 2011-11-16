package org.kevoree.library.javase.pipeChannel

import actors.DaemonActor
import org.kevoree.framework.message.Message
import java.lang.Thread
import java.io._

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

  new Thread() {
    override def run () {
      val f = new RandomAccessFile(System.getProperty("java.io.tmpdir") + File.separator + pipeChannel.getName + "_" +
        pipeChannel.getNodeName, "r")
      var bytes = Array[Byte](0)
      while (alive) {
        val length = f.readInt()
        if (bytes.size < length) {
          bytes = new Array[Byte](length)
        }
        Thread.sleep(100)
        f.readFully(bytes, 0, length)
        val inputStream = new ByteArrayInputStream(bytes)
        receiveMessage(new ObjectInputStream(inputStream).readObject.asInstanceOf[Message])
        inputStream.close()
      }
      f.close()
    }
  }.start();

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
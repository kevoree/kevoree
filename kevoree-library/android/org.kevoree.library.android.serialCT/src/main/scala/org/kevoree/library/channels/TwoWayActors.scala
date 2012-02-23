package org.kevoree.library.channels;


import eu.powet.android.serialUSB
import actors.{Actor, TIMEOUT, DaemonActor}
import serialUSB._
import org.kevoree.android.framework.service.KevoreeAndroidService
import org.kevoree.android.framework.helper.UIServiceHandler

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 15:27
 */
class TwoWayActors(portName: String) extends 	SerialListener {

  var replyActor = new DaemonActor {
    def act() {
      loop {
        react {
          case CONTENTREC(recString) => {
            KevoreeSharedCom.notifyObservers(portName, recString)
          }
          case CLOSEPORT() => exit()
          case msg: Tuple3[String, String, Long] => {
            val originalSender = sender
            if (serialPort != null) {
              try {
                serialPort.write(msg._1.getBytes)
                reactWithin(msg._3) {
                  case CONTENTREC(recString) if (recString.contains(msg._2)) => {
                    originalSender ! true
                    KevoreeSharedCom.notifyObservers(portName, recString)
                  }
                  case TIMEOUT => println("TimeOut internal") //LOST NEXT MESSAGE
                  case CLOSEPORT() => exit()
                }
              } catch {
                case _ => {originalSender ! false}
              }
            }
          }
          case simpleMsg: String => {
            if (serialPort != null) {
              serialPort.write(simpleMsg.getBytes)
            }
          }
        }
      }
    }
  }.start()

  var serialPort: ISerial = null
  private[library] var uiService: KevoreeAndroidService = null
  uiService = UIServiceHandler.getUIService
  serialPort = new UsbSerial(portName,115200,uiService.getRootActivity)
  serialPort.open()
  if(serialPort.isConnected){
    serialPort.addEventListener(this)
  }else {
    killConnection();
  }


  def sendAndWait(msg: String, waitMsg: String, timeout: Long): java.lang.Boolean = {
    (replyActor !?(timeout, Tuple3(msg, waitMsg, timeout))) match {
      case Some(e) => true
      case None => println("timeout"); false
    }
  }

  def send(msg: String) {
    replyActor ! msg
  }

  case class CLOSEPORT()
  case class CONTENTREC(content: String)

  var closed = false

  def killConnection() {
    replyActor ! CLOSEPORT()
    if (serialPort != null) {
      serialPort.close()
    }
    closed = true
  }

  def incomingDataEvent(evt: SerialEvent) {
    replyActor ! CONTENTREC(new String(evt.read()))
  }


}
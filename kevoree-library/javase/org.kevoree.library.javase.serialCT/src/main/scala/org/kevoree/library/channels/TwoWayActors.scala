package org.kevoree.library.channels

import actors.{TIMEOUT, DaemonActor}
import gnu.io._
import org.kevoree.framework.ChannelFragment
import org.kevoree.framework.message.Message
import util.matching.Regex
import scala.collection.JavaConversions._
import java.lang.StringBuffer

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 15:27
 */

class TwoWayActors(portName: String) extends SerialPortEventListener with DaemonActor {

  private var observers: List[ChannelFragment] = List()

  def getObserversSize = observers.size

  def addObserver(c: ChannelFragment) {
    observers = observers ++ List(c)
  }

  def removeObserver(c: ChannelFragment) {
    observers = observers.filterNot(p => p == c)
  }

  case class CLOSEPORT()

  case class CONTENTREC()

  case class MSGTOSEND(msg: String)

  def act() {
    loop {
      react {
        case MSGTOSEND(msg: String) => {
          serialPort.getOutputStream.write(msg.getBytes)
        }
        case CLOSEPORT() => {
          exit()
        }
        case CONTENTREC() => {
          if (serialPort.getInputStream.available() > 0) {
            recString = recString + (serialPort.getInputStream.read().toChar);
            if ( /*recString.contains("]") ||*/ recString.contains("\n") ) {

              recString.trim() match {
                case KevSerialMessageRegex(srcChannelName, nodeName, contentBody) => {
                  val message = new Message();
                  val buffer = new StringBuffer()
                  var metric : String = null
                  contentBody.trim().split('/').foreach{ v =>
                    if(metric == null){
                      metric = v
                    } else {
                      if(buffer.length() > 0){
                        buffer.append(",")
                      }
                      buffer.append(metric)
                      buffer.append("=")
                      buffer.append(v)
                    }
                  }
                  message.setContent(buffer.toString());
                  message.setInOut(false);
                  message.getPassedNodes().add(nodeName);
                  observers.foreach{ ct =>
                      if(ct.getOtherFragments.exists(ofrag=> ofrag.getName == srcChannelName)){
                         ct.remoteDispatch(message);
                      }
                  }

                }
                case _ => {
                  println("Msg format error => " + recString)
                  println("Msg lost")
                  recString = ""
                }
              }
              recString = "";
            }
          }
        }
      }
    }
  }

  def sendMessage(instanceName: String, nodeName: String, msg: String) {
    val messageTosSend = instanceName + ":" + nodeName + "[" + msg + "]"
    this ! MSGTOSEND(messageTosSend)
  }

  def getPortName = portName

  //RXTXPort.staticSetDTR(portName, false)
  //RXTXPort.staticSetRTS(portName, false)
  //RXTXPort.staticSetDSR(portName, false)

  var serialPort: SerialPort = null
  var portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
  if (portIdentifier.isCurrentlyOwned) {
    System.out.println("Error: Port is currently in use");
  } else {
    val commPort = portIdentifier.open(portName, 2000);
    if (commPort.isInstanceOf[SerialPort]) {
      serialPort = commPort.asInstanceOf[SerialPort];
      //serialPort.setDTR(false)
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
      serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      //serialPort.disableReceiveTimeout();
      //serialPort.enableReceiveThreshold(1);

      serialPort.addEventListener(this)
      serialPort.notifyOnDataAvailable(true);

    } else {
      System.out.println("Error: Only serial ports are handled by this example.");
    }
  }
  Thread.sleep(2000)
  start()


  def killConnection() {
    this ! CLOSEPORT()
    if (serialPort != null) {
      serialPort.getInputStream.close()
      serialPort.getOutputStream.close()
      serialPort.close()
    }
  }

  var recString = ""
  val KevSerialMessageRegex = new Regex("(.+):(.+)\\[(.*)\\]")


  def serialEvent(p1: SerialPortEvent) {
    p1.getEventType match {
      case SerialPortEvent.DATA_AVAILABLE => {
        this ! CONTENTREC()
      }
    }

  }
}
package org.kevoree.library.channels

import actors.{TIMEOUT, DaemonActor}
import gnu.io._
import org.kevoree.framework.ChannelFragment
import org.kevoree.framework.message.Message

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 15:27
 */

class TwoWayActors(portName: String, channel: ChannelFragment) extends SerialPortEventListener {

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

  def sendMsg(msg: String) {
    val msgToSend = "["+msg+"]"
    serialPort.getOutputStream.write(msgToSend.getBytes)
  }


  case class CLOSEPORT()

  case class CONTENTREC()

  def killConnection() {
    readerActor ! CLOSEPORT()
    if (serialPort != null) {
      serialPort.getInputStream.close()
      serialPort.getOutputStream.close()
      serialPort.close()
    }
  }

  var recString = ""
  var readerActor = new DaemonActor {
    def act() {
      loop {
        react {
          case CLOSEPORT() => exit()
          case _ => {
            if (serialPort.getInputStream.available() > 0) {
              recString = recString + (serialPort.getInputStream.read().toChar);
              if (recString.contains("]") || recString.contains("\n")) {
                val message = new Message();
                message.setContent(recString.trim());
                message.setInOut(false);
                message.getPassedNodes().add("unamedNode");
                channel.remoteDispatch(message);
                recString = "";
              }
            }
          }
        }
      }

    }

  }.start()


  def serialEvent(p1: SerialPortEvent) {
    p1.getEventType match {
      case SerialPortEvent.DATA_AVAILABLE => {
        readerActor ! "trigger"
      }
    }

  }
}
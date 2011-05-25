package org.kevoree.library.arduinoNodeType

import javax.swing.JOptionPane
import scala.collection.JavaConversions._
import gnu.io.{PortInUseException, CommPortIdentifier}

/**
 * User: ffouquet
 * Date: 24/05/11
 * Time: 13:17
 */

object GuiAskForComPort {

  def askPORT: String = {
    var comPorts: List[String] = List()
    CommPortIdentifier.getPortIdentifiers.foreach {
      portID =>
        portID.asInstanceOf[CommPortIdentifier].getPortType match {
          case CommPortIdentifier.PORT_SERIAL => {
            comPorts = comPorts ++ List(portID.asInstanceOf[CommPortIdentifier].getName)

            /*try {
              var thePort = portID.asInstanceOf[CommPortIdentifier].open("CommUtil", 50);
              thePort.close();
              //h.add(com);
            } catch {
              case _ @ e => e.printStackTrace()
            } */
          }
          case _ =>
        }
    }

    // val comPorts = org.kevoree.extra.osgi.rxtx.RXTXHelper.getAvailablePorts;

    if (comPorts.size > 0) {
      val res = JOptionPane.showInputDialog(null, "Kevoree", "Select serial port for NodeType model upload", JOptionPane.PLAIN_MESSAGE,
        null, comPorts.toArray, comPorts.get(0))
      if (res != null) {
        res.toString
      } else {
        ""
      }
    } else {
      JOptionPane.showMessageDialog(null, "No available serial port", "JArduino", JOptionPane.ERROR_MESSAGE);
      "";
    }
  }

}
package org.kevoree.library.arduinoNodeType

import javax.swing.JOptionPane
import scala.collection.JavaConversions._
import org.kevoree.extra.kserial.Utils.KHelpers

/**
 * User: ffouquet
 * Date: 24/05/11
 * Time: 13:17
 */

object GuiAskForComPort {

  def askPORT: String = {
    val comPorts: List[String] = KHelpers.getPortIdentifiers.toList
    if (comPorts.size > 0) {
      val res = JOptionPane.showInputDialog(null, "Kevoree", "Select serial port for NodeType model upload", JOptionPane.PLAIN_MESSAGE,
        null, comPorts.toArray, comPorts.get(0))
      if (res != null) {
        res.toString
      } else {
        ""
      }
    } else {
      JOptionPane.showMessageDialog(null, "No available serial port", "Kevoree", JOptionPane.ERROR_MESSAGE);
      "";
    }
  }

}
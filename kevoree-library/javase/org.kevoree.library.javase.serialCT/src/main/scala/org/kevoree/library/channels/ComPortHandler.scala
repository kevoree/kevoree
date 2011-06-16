/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.channels

import scala.Some
import org.kevoree.framework.ChannelFragment

object ComPortHandler {

  var ports = new scala.collection.mutable.ArrayBuffer[TwoWayActors]

  def getPortByName(pname: String) = {
    ports.find(tw => tw.getPortName == pname) match {
      case Some(twFound) => twFound
      case None => {
        val newTw = new TwoWayActors(pname)
        ports.append(newTw)
        newTw
      }
    }
  }

  def addListener(portName: String, cf: ChannelFragment) = {
    getPortByName(portName).addObserver(cf)
  }

  def removeListener(portName: String, cf: ChannelFragment) = {
    val port = getPortByName(portName)
    port.removeObserver(cf)
    if(port.getObserversSize <= 0){
      port.killConnection()
    }
    ports.remove(ports.indexOf(port))
  }


}

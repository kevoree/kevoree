package org.kevoree.library.javase.basicGossiper

import jexxus.common.{Delivery, Connection, ConnectionListener}
import jexxus.server.ServerConnection
import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/11/12
 * Time: 14:49
 */
class ProtocConnectionListener(pv : ProcessValue) extends ConnectionListener {
  def connectionBroken(p1: Connection, p2: Boolean) {

  }

  def receive(p1: Array[Byte], p2: Connection) {
    val msg : Message = Message.parseFrom(p1)
    pv.receiveRequest(msg, new GossiperGossiperConnection(){
      def write(data: Array[Byte]) {
         p2.send(data,Delivery.RELIABLE)
      }
    })
  }

  def clientConnected(p1: ServerConnection) {

  }
}

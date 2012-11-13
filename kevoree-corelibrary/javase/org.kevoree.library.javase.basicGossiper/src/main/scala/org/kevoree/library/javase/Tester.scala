package org.kevoree.library.javase

import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message
import org.kevoree.library.basicGossiper.protocol.gossip.Gossip.UpdatedValueNotification

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 13/11/12
 * Time: 15:15
 */
object Tester extends App {

  val messageBuilder: Message.Builder = Message.newBuilder.setDestName("L").setDestNodeName("L")
  val res  = messageBuilder.setContentClass(classOf[UpdatedValueNotification].getName).setContent(UpdatedValueNotification.newBuilder.build.toByteString).build()

  val ser = res.toByteArray

  println(ser.size+"-"+res.getSerializedSize)

  Message.parseFrom(ser)

}

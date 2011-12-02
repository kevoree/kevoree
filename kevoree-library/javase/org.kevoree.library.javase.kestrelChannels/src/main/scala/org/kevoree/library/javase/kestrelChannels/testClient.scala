package org.kevoree.library.javase.kestrelChannels

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 01/12/11
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.framework.KevoreeXmiHelper
import actors.remote.Serializer
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import org.kevoree.extra.marshalling.{RichString, RichJSONObject}
import org.kevoree.framework.message.Message

object testClient extends App {


  val client = new KestrelClient("localhost", 22133)
  client.connect()
  System.out.println(client.stats())

  val msgToEnqueue = new Message()
  msgToEnqueue.setContent("HELLO")

  client.enqueue("jed",msgToEnqueue)



  val msgToDequeue= client.dequeue("jed")

  println(msgToDequeue.getContent())
}
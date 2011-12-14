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
import org.kevoree.framework.message.Message

object testClient extends App {





  //val msgToEnqueue = new Message()
  //msgToEnqueue.setContent("HELLO")

  // msgToEnqueue.setDestNodeName("node0")
  //client.enqueue("node0",msgToEnqueue)
  val client = new KestrelClient("localhost", 22133)

  while(true){
    client.connect()
    val msgToDequeue= client.dequeue("kevoree")

    println(msgToDequeue.getContent())

    client.disconnect()

  }





}
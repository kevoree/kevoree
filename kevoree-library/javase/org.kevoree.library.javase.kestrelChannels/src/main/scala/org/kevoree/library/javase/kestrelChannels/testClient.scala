package org.kevoree.library.javase.kestrelChannels

/**
 * Created by IntelliJ IDEA.
 * User: jedartois@gmail.com
 * Date: 01/12/11
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.framework.KevoreeXmiHelper
import actors.remote.Serializer
import org.kevoree.framework.message.Message
import java.io._
import org.kevoree.ContainerNode

object testClient extends App {





  //val msgToEnqueue = new Message()
  //msgToEnqueue.setContent("HELLO")

  // msgToEnqueue.setDestNodeName("node0")
  //client.enqueue("node0",msgToEnqueue)
  /*
  val client = new KestrelClient("localhost", 22133)

  while(true){
    client.connect()
    val msgToDequeue= client.dequeue("kevoree")

    println(msgToDequeue.getContent())

    client.disconnect()     }
                         */


  val f = new File("/home/jed/modeKestrel");
  val out=new FileInputStream(f).asInstanceOf[InputStream]
  val model = KevoreeXmiHelper.loadStream(out)
 // println(KevoreeUtil.isProvided(model,"KestrelCh871"))
  println(KevoreeUtil.isRequired(model,"KestrelCh871","node0"))



 // model.getHubs.find(f => f.getName == "KestrelCh871").get.





}
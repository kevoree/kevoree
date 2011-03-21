/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import com.google.protobuf.ByteString
import java.net.InetSocketAddress
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage

object GossiperMain {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    
    GossiperChannelServer.startOrUpdate(8000);
    
    
    var client = new GossiperChannelClient(3000,new InetSocketAddress("127.0.0.1",8000))
	var modelBytes = ByteString.copyFromUtf8("Hello");
	var message = KevoreeMessage.Message.newBuilder.setContent(modelBytes).setDestChannelName("localhost").build
	
    println(client.call(message))
    
    var stop = new Thread(){
      override def run()={
        println("stop")
        Thread.sleep(10000)
        GossiperChannelServer.stop
      }
    }.start
    
  }

}

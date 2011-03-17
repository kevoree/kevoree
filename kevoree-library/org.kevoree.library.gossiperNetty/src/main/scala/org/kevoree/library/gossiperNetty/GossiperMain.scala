/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress

object GossiperMain {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    
    GossiperChannelServer.startOrUpdate(8000);
    
    
    var client = new GossiperChannelClient(3000,new InetSocketAddress("127.0.0.1",8000))
    println(client.call("hello"))
    
    var stop = new Thread(){
      override def run()={
        println("stop")
        Thread.sleep(10000)
        GossiperChannelServer.stop
      }
    }.start
    
  }

}

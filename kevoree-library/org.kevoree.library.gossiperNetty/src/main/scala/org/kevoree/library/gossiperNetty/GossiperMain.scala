/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

object GossiperMain {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    
    GossiperChannelServer.startOrUpdate(8000);
    
    var stop = new Thread(){
      override def run()={
        println("stop")
        Thread.sleep(10000)
        GossiperChannelServer.stop
      }
    }.start
    
  }

}

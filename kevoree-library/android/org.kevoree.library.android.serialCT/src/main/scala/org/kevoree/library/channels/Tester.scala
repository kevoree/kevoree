package org.kevoree.library.channels;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/02/12
 * Time: 21:09
 */

object Tester extends App {

  KevoreeSharedCom.addObserver("/dev/tty.usbmodem26231", new ContentListener {
    def recContent(content: String) {
      println("Rec=" + content)
    }
  })

  Thread.sleep(2000)
  
  
  KevoreeSharedCom.send("/dev/tty.usbmodem26231","$8{udi:t1:period=500}")


  Thread.sleep(20000)
  //KevoreeSharedCom.send("/dev/tty.usbmodem621", "1")
 // KevoreeSharedCom.send("/dev/tty.usbmodem621", "1")
 // KevoreeSharedCom.send("/dev/tty.usbmodem621", "1")
 // KevoreeSharedCom.send("/dev/tty.usbmodem621", "1")

  /*
  for(i <- 0 until 10){
    KevoreeSharedCom.sendSynch("/dev/tty.usbmodem411", "1","J'ai recu : 10",1000)
    Thread.sleep(4000)
  }*/

}

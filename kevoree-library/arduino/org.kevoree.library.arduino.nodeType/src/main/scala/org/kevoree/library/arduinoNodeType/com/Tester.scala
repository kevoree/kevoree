package org.kevoree.library.arduinoNodeType.com

import gnu.io.RXTXPort
import org.kevoree.extra.osgi.rxtx.{ContentListener, KevoreeSharedCom}
import org.kevoree.extra.osgi.rxtx.KevoreeSharedCom.SYNC_SEND

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 18:38
 */

object Tester extends App {

  println("Hello world !")

  val portName = "/dev/tty.usbserial-A400g2AP"

  KevoreeSharedCom.addObserver(portName,new ContentListener {
    def recContent(content: String) {
      println(" Rec => "+content.trim())
    }
  })

  //Thread.sleep(2000)

  println(KevoreeSharedCom.sendSynch(portName,"$3{udi:t1:period=100}","ack3",2000))


  Thread.sleep(2000)


  //KevoreeSharedCom !? SYNC_SEND(portName,"$5{rbi:light2:hub1:toggle/abi:light4:hub1:toggle}","ack5",2000))

  println("Ok I will die")

  KevoreeSharedCom.killAll()

}
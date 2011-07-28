package org.kevoree.library.arduinoNodeType.com

import gnu.io.RXTXPort

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 18:38
 */

object Tester extends App {

  println("Hello world !")



  var tester = new TwoWayActors("/dev/tty.usbserial-A400g2AP");

  println(tester.recString)

  println(tester.sendAndWait("$6{ping}","ack6",2000))

  println(tester.recString)

  println(tester.sendAndWait("$5{rbi:light2:hub1:toggle/abi:light4:hub1:toggle}","ack5",2000))

  println(tester.recString)

  tester.killConnection()

}
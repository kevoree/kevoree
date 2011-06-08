package org.kevoree.library.arduinoNodeType.com

import gnu.io.RXTXPort

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 18:38
 */

object Tester extends App {

  println("Hello world !")



  var tester = new TwoWayActors("/dev/tty.usbserial-A400g2se");

  println(tester.sendAndWait("$6{ping};","ack6",2000))
  println(tester.sendAndWait("$7{udi:t1:period=50};","ack7",2000))

  tester.killConnection()



}
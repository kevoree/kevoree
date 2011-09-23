package org.kevoree.channels

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/09/11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */

object Tester2 extends App {

  val discovery = new BluetoothDiscovery
  println(discovery.getRemoteDevice("KevoreeSensor1"))

}
package org.kevoree.library.arduinoNodeType

import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 30/03/12
 * Time: 14:25
 */

object Tester extends  App {

  val model = KevoreeXmiHelper.load("/home/jed/Desktop/model.kev")



  ArduinoModelGetHelper.getCurrentModel(model, "node0","/dev/tty.usbmodem26231")


  
  
}
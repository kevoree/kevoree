package org.kevoree.platform.osgi.standalone.gui

import java.io.PrintStream

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/09/11
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */

object DefaultSystem {

  var defout: PrintStream = null
  var deferr: PrintStream = null

  def saveSystemFlux() {
    defout = System.out
    deferr = System.err
  }

  def resetSystemFlux(){
    System.setErr(deferr)
    System.setOut(defout)
  }


}
package org.kevoree.experiment.modelScript

object BootStrapApp extends App {

  override def main (args: Array[String]) { // TODO define precise parameters organization
    if (args.length == 1) {
      Configuration.grid5000 = true
      Configuration.build()
    } else {
      Configuration.grid5000 = false
      Configuration.build()
      var nbNodes = 0
      Configuration.packets.foreach {
        p =>
          nbNodes = nbNodes + p.nbElem
      }
    }

      BootStrapAppComplex
        .bootStrap(Configuration.packets, Configuration.logServer, Configuration.ips, false, false, 10000)
  }
}
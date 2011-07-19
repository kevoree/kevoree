package org.kevoree.experiment.modelScript


object BootStrapApp extends App {

  override def main (args: Array[String]) { // TODO define precise parameters organization
    if (args.length == 2) {
      Configuration.grid5000 = true
      Configuration.build()
      Configuration.logServer = args(1)
    } else {
      Configuration.grid5000 = false
      Configuration.build()
      var nbNodes = 0
      Configuration.packets.foreach {
        p =>
          nbNodes = nbNodes + p.nbElem
      }
    }

    println("logServer = " + Configuration.logServer)

      BootStrapAppComplex
        .bootStrap(Configuration.packets, Configuration.logServer, Configuration.ips, true, false, 10000)
  }
}
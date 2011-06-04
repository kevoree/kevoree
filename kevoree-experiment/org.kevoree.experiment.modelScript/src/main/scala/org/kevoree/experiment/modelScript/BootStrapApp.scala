package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper
import java.net.URL
import java.io.{ByteArrayOutputStream, BufferedReader, InputStreamReader, OutputStreamWriter}
import org.kevoree.experiment.modelScript.NodePacket._

object BootStrapApp extends Application {

  override def main (args: Array[String]) {
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
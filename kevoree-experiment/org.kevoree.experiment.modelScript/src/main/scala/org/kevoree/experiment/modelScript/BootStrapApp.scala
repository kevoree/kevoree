package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper
import java.net.URL
import java.io.{ByteArrayOutputStream, BufferedReader, InputStreamReader, OutputStreamWriter}
import org.kevoree.experiment.modelScript.NodePacket._

object BootStrapApp extends Application {

  var nbNodes = 0
  Configuration.packets.foreach {
    p =>
      nbNodes = nbNodes + p.nbElem
  }


  BootStrapAppComplex.bootStrap(Configuration.packets, Configuration.paraisseuxIP, Configuration.ips)


}
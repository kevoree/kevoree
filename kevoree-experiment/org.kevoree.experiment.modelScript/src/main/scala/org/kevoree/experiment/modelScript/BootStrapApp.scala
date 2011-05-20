package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper
import java.net.URL
import java.io.{ByteArrayOutputStream, BufferedReader, InputStreamReader, OutputStreamWriter}

object BootStrapApp extends Application {


  val dukeIP = "131.254.15.214"
  val paraisseuxIP = "131.254.12.28"
  val ips = List(dukeIP, paraisseuxIP)
  val packets = List(
                      NodePacket("duke", dukeIP, 8000, 4),
                      NodePacket("duke2", dukeIP, 8100, 4),
                      //NodePacket("duke3", dukeIP, 8200, 4),
                      //NodePacket("duke4", dukeIP, 8300, 4),
                      //NodePacket("duke5", dukeIP, 8400, 4),
                      //NodePacket("duke6", dukeIP, 8500, 4),
                      NodePacket("paraisseux", paraisseuxIP, 8000, 4),
                      NodePacket("paraisseux1", paraisseuxIP, 8100, 8)
                    )
  var nbNodes = 0
  packets.foreach {
    p =>
      nbNodes = nbNodes + p.nbElem
  }


  BootStrapAppComplex.bootStrap(packets, paraisseuxIP, ips)


}
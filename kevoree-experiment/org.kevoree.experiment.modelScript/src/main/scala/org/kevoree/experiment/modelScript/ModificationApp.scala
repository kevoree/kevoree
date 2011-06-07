package org.kevoree.experiment.modelScript

import java.io.{FileReader, BufferedReader, File}
import collection.immutable.List._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */

object ModificationApp extends Application {

  var grid5000 = false

  override def main (args: Array[String]) {
    val modificationGenerator = new ModificationGenerator(Configuration.ips)

    if (grid5000) {
      println(System.getProperty("OAR_NODE_FILE"))
      if (System.getProperty("OAR_NODE_FILE") != null) {
        val file = new File(System.getProperty("OAR_NODE_FILE"))
        if (file.exists()) {
          val reader = new BufferedReader(new FileReader(file))
          var first = true
          val line: String = reader.readLine()

              packets = packets ++ List(NodePacket(line.replaceAll("-", "").replaceAll("\\.", ""), line, 8000, 4))
          }
        } else {
          val stream = System.in
          var b = stream.read()
          while (b != -1 && b != 'q') {
            modificationGenerator.doAction("kspark0")
            b = stream.read()
          }
        }
      }

    }
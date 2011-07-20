package org.kevoree.experiment.modelScript

import java.io.{FileReader, BufferedReader, File}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */

object ModificationApp extends App {

  override def main (args: Array[String]) {
    //println("starting modification process...")
    if (args.length != 0 && args(0).equals("true")) {
      //println("modification process is executed on Grid5000...")
      Configuration.grid5000 = true
    }
    Configuration.build()

    val modificationGenerator = new ModificationGenerator(Configuration.ips)

    if (Configuration.grid5000) {
      println(System.getProperty("OAR_NODE_FILE"))
      if (System.getProperty("OAR_NODE_FILE") != null) {
        val file = new File(System.getProperty("OAR_NODE_FILE"))
        if (file.exists()) {
          val reader = new BufferedReader(new FileReader(file))
          var line: String = reader.readLine()
          line = line.replaceAll("-", "").replaceAll("\\.", "") + "0"
          reader.close()

          // TODO register the node we use to push update

          /*val stream = System.in
          var b = stream.read()
          while (b != -1 && b != 'q') {*/
            modificationGenerator.doAction(line)
            /*b = stream.read()
          }*/
        } else {
          println("file \"" + file.getAbsolutePath + "\" is missing.\nModification is not possible")
        }
      } else {
        println("missing $OAR_NODE_FILE variables")
      }
    } else {
      val stream = System.in
      var b = stream.read()
      while (b != -1 && b != 'q') {
        modificationGenerator.doAction("duke0")
        b = stream.read()
      }
    }
  }
}
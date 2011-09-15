package org.kevoree.library.javase.gossiperNetty

import java.io.{FileReader, BufferedReader, File}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */

object ModificationApp extends App {

  def run () {
    val modificationGenerator = new ModificationGenerator(Configuration.ips)
    //val stream = System.in
    //var b = stream.read()
    //while (b != -1 && b != 'q') {
    modificationGenerator.doAction(Configuration.followingPlatform)
    //  b = stream.read()
    //}
  }

  /*def runForGrid () {
    val modificationGenerator = new ModificationGenerator(Configuration.ips)
    if (System.getProperty("OAR_NODE_FILE") != null) {
      val file = new File(System.getProperty("OAR_NODE_FILE"))
      if (file.exists()) {
        val reader = new BufferedReader(new FileReader(file))
        var line: String = reader.readLine()
        line = line.replaceAll("-", "").replaceAll("\\.", "") + "0"
        reader.close()
        modificationGenerator.doAction(line)
      } else {
        println("file \"" + file.getAbsolutePath + "\" is missing.\nModification is not possible")
      }
    } else {
      println("missing $OAR_NODE_FILE variables")
    }
  }*/

  override def main (args: Array[String]) {

    if (args.contains("grid")) {
      Configuration.grid = true
    } else {
      args.filter(arg => arg.startsWith("nodeFile=")).foreach {
        arg => Configuration.nodeFile = arg.substring("nodeFile=".size, arg.size);
      }
    }
    Configuration.build()
    run()
  }
}
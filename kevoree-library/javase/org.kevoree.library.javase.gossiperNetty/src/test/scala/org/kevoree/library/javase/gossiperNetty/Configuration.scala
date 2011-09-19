package org.kevoree.library.javase.gossiperNetty

import java.io.{FileReader, BufferedReader, File}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */
object Configuration {

  var logServer: String = null
  var followingPlatform: String = null
  var packets: List[NodePacket] = List()
  var ips: List[String] = List()
  var nodeFile = ""
  var grid = false

  def build () {
    if (grid) {
      buildForGrid()
    } else if (!nodeFile.equals("")) {
      buildWithNodeFile()
    } else {
      buildDefault()
    }

  }

  private def buildDefault () {
    val dukeIP = "duke.irisa.fr"
    val paraisseuxIP = "paraisseux.irisa.fr"
    val tombombadilIP = "tombombadil.irisa.fr"
    val cigogneIP = "cigogne.irisa.fr"
    val galadrielIP = "galadriel.irisa.fr"
    logServer = paraisseuxIP
    //logServer = dukeIP
    ips = List(dukeIP, paraisseuxIP/*, abricotierIP, tombombadilIP, cigogneIP,
                galadrielIP, lyraIP, olivierbravoIP, didieradminIP*/)
    packets = List(
                    NodePacket("duke0", dukeIP, 8000, 3),
                    /*NodePacket("duke1", dukeIP, 8010, 3),
                    NodePacket("duke2", dukeIP, 8020, 3),*/
                    NodePacket("paraisseux0", paraisseuxIP, 8000, 3)//,
//                    NodePacket("paraisseux1", paraisseuxIP, 8010, 3),
//                    NodePacket("paraisseux2", paraisseuxIP, 8020, 3),
//                    NodePacket("tombombadil0", tombombadilIP, 8000, 3),
//                    NodePacket("tombombadil1", tombombadilIP, 8010, 3),
//                    NodePacket("tombombadil2", tombombadilIP, 8020, 3),
//                    NodePacket("cigogne0", cigogneIP, 8000, 3),
//                    NodePacket("cigogne1", cigogneIP, 8010, 3),
//                    NodePacket("cigogne2", cigogneIP, 8020, 3),
//                    NodePacket("galadriel0", galadrielIP, 8000, 3)//,
//                    NodePacket("galadriel1", galadrielIP, 8010, 3),
//                    NodePacket("galadriel2", galadrielIP, 8020, 3)
                  )
    followingPlatform = "paraisseux00"
  }

  private def buildForGrid () {
    //    println(System.getProperty("OAR_NODE_FILE"))
    if (System.getProperty("OAR_NODE_FILE") != null) {
      val file = new File(System.getProperty("OAR_NODE_FILE"))
      if (file.exists()) {
        val reader = new BufferedReader(new FileReader(file))
        var first = true
        var line: String = reader.readLine()
        while (line != null) {
          if (!ips.contains(line)) {
            ips = ips ++ List(line)
            packets = packets ++ List(NodePacket(line.replaceAll("-", "").replaceAll("\\.", ""), line, 8000, 8))
            if (first) {
              logServer = line
              followingPlatform = line.replaceAll("-", "").replaceAll("\\.", "") + "0"
              first = false
            }
          }
          line = reader.readLine()
        }
        reader.close()
      } else {
        println("file is missing.\nBootstrap is not possible")
      }
    } else {
      println("missing $OAR_NODE_FILE variables")
    }
  }

  private def buildWithNodeFile () {
    val file = new File(nodeFile)
    if (file.exists()) {
      val reader = new BufferedReader(new FileReader(file))
      var first = true
      var line: String = reader.readLine()
      while (line != null) {
        val name = line.split(":")(0)
        val ip = line.split(":")(1)
        val basePort = Integer.parseInt(line.split(":")(2))
        if (!ips.contains(ip)) {
          ips = ips ++ List(ip)
        }
        packets = packets ++ List(NodePacket(name, ip, basePort, 3))
        if (first) {
          logServer = ip
          followingPlatform = name + "0"
          first = false
        }
        line = reader.readLine()
      }
      reader.close()
    } else {
      println("file is missing.\nBootstrap is not possible")
    }
  }
}
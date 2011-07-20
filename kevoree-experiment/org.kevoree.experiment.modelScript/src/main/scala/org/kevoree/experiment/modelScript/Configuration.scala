package org.kevoree.experiment.modelScript

import java.io.{FileReader, BufferedReader, File}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */
object Configuration {

  var job_id = "0"
  var grid5000 = false
  var logServer: String = null
  var packets: List[NodePacket] = List()
  var ips: List[String] = List()

  def build () {
    if (grid5000) {
      println(System.getProperty("OAR_NODE_FILE"))
      if (System.getProperty("OAR_NODE_FILE") != null) {
        val file = new File(System.getProperty("OAR_NODE_FILE"))
        if (file.exists()) {
          val reader = new BufferedReader(new FileReader(file))
          var first = true
          var line: String = reader.readLine()
          while (line != null) {
            //line = line.replaceAll("\\.", "").replaceAll("-", "")
            if (!ips.contains(line)) {
              println("add " + line + " as machine")
              ips = ips ++ List(line)
              packets = packets ++ List(NodePacket(line.replaceAll("-", "").replaceAll("\\.", ""), line, 8000, 4))
              if (first) {
                logServer = line
                println("logServer = " + logServer)
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
    } else {
      val dukeIP = "131.254.15.214"
      val paraisseuxIP = "131.254.12.28"
//      val paraisseuxIP = "192.168.0.14"
//      val ksparkIP = "192.168.0.16"
//      val theogalIP = "192.168.0.11"
//      val faineantosIP = "192.168.0.17"
//      logServer = paraisseuxIP
      logServer = dukeIP
      ips = List(dukeIP, paraisseuxIP/*, ksparkIP, theogalIP, faineantosIP*/)
      packets = List(
                      NodePacket("duke", dukeIP, 8000, 4),
                      //NodePacket("duke2", dukeIP, 8100, 4),
                      //NodePacket("duke3", dukeIP, 8200, 4),
                      //NodePacket("duke4", dukeIP, 8300, 4),
                      //NodePacket("duke5", dukeIP, 8400, 4),
                      //NodePacket("duke6", dukeIP, 8500, 4),
                      NodePacket("paraisseux", paraisseuxIP, 8000, 4)
                      //NodePacket("paraisseux1", paraisseuxIP, 8100, 8)
                    //  NodePacket("kspark", ksparkIP, 8000, 4),
                    //  NodePacket("theogal", theogalIP, 8000, 4),
                    //  NodePacket("faineantos", faineantosIP, 8000, 4)
                    )
    }
  }
}
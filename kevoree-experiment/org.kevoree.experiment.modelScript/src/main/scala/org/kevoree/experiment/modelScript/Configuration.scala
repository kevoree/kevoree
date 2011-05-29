package org.kevoree.experiment.modelScript

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */

object Configuration {
  val dukeIP = "131.254.15.214"
  //val paraisseuxIP = "131.254.12.28"
  val paraisseuxIP = "192.168.0.14"
  val ksparkIP = "192.168.0.16"
  val theogalIP = "192.168.0.11"
  val faineantosIP = "192.168.0.17"
  val ips = List(/*dukeIP,*/ paraisseuxIP, ksparkIP, theogalIP, faineantosIP)
  val packets = List(
                      //NodePacket("duke", dukeIP, 8000, 4),
                      //NodePacket("duke2", dukeIP, 8100, 4),
                      //NodePacket("duke3", dukeIP, 8200, 4),
                      //NodePacket("duke4", dukeIP, 8300, 4),
                      //NodePacket("duke5", dukeIP, 8400, 4),
                      //NodePacket("duke6", dukeIP, 8500, 4),
                      NodePacket("paraisseux", paraisseuxIP, 8000, 4),
                      //NodePacket("paraisseux1", paraisseuxIP, 8100, 8)
                      NodePacket("kspark", ksparkIP, 8000, 4),
                      NodePacket("theogal", theogalIP, 8000, 4),
                      NodePacket("faineantos", faineantosIP, 8000, 4)
                    )
}
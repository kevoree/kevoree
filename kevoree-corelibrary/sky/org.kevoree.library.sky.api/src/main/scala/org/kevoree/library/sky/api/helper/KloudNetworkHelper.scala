package org.kevoree.library.sky.api.helper

import org.kevoree.{DictionaryAttribute, ContainerRoot}
import org.kevoree.framework.{NetworkHelper, KevoreePropertyHelper}
import java.net.{ServerSocket, InetSocketAddress, Socket, InetAddress}
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 01/11/12
 * Time: 19:36
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object KloudNetworkHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def selectIP (parentNodeName: String, kloudModel: ContainerRoot, alreadyUsedIps: Array[String]): Option[String] = {
    logger.debug("try to select an IP for a child of {}", parentNodeName)
    kloudModel.getNodes.find(n => n.getName == parentNodeName) match {
      case None => None
      case Some(node) => {
        val subnetOption = KevoreePropertyHelper.getStringPropertyForNode(kloudModel, parentNodeName, "subnet")
        val maskOption = KevoreePropertyHelper.getStringPropertyForNode(kloudModel, parentNodeName, "mask")
        if (subnetOption.isDefined && maskOption.isDefined) {
          Some(lookingForNewIp(alreadyUsedIps, subnetOption.get, maskOption.get))
        } else {
          Some("127.0.0.1")
        }
      }
    }
  }

  private def lookingForNewIp (ips: Array[String], subnet: String, mask: String): String = {
    var newIp = subnet
    val ipBlock = subnet.split("\\.")
    var i = Integer.parseInt(ipBlock(0))
    var j = Integer.parseInt(ipBlock(1))
    var k = Integer.parseInt(ipBlock(2))
    var l = Integer.parseInt(ipBlock(3)) + 2

    var found = false
    while (i < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
      while (j < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
        while (k < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
          while (l < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
            val tmpIp = i + "." + j + "." + k + "." + l
            if (!ips.contains(tmpIp)) {
              if (!NetworkHelper.isAccessible(tmpIp)) {
                newIp = tmpIp
                found = true
              }
            }
            l += 1
          }
          l = 1
          k += 1
        }
        k = 1
        j += 1
      }
      j = 1
      i += 1
    }
    newIp
  }

  private def checkMask (i: Int, j: Int, k: Int, l: Int, subnet: String, mask: String): Boolean = {
    val maskInt = ~((1 << (32 - Integer.parseInt(mask))) - 1)
    val ipBytes = InetAddress.getByName(i + "." + j + "." + k + "." + l).getAddress
    val subnetBytes = InetAddress.getByName(subnet).getAddress
    val subnetInt = (subnetBytes(0) << 24) | (subnetBytes(1) << 16) | (subnetBytes(2) << 8) | (subnetBytes(3) << 0)
    val ipInt = (ipBytes(0) << 24) | (ipBytes(1) << 16) | (ipBytes(2) << 8) | (ipBytes(3) << 0)
    (subnetInt & maskInt) == (ipInt & maskInt)
  }

  def selectPortNumber (address: String, ports: Array[Int]): Int = {
    var i = 8000
    if (address != "") {
      var found = false
      while (!found) {
        if (!ports.contains(i)) {
          try {
            val socket = new Socket()
            socket.connect(new InetSocketAddress(address, i), 1000)
            socket.close()
            i = i + 1
          } catch {
            case _@e =>
              found = true
          }
        } else {
          i = i + 1
        }
      }
    } else {
      var found = false
      while (!found) {
        if (!ports.contains(i)) {
          try {
            val socket = new ServerSocket(i)
            socket.close()
            found = true
          } catch {
            case _@e =>
              i = i + 1
          }
        } else {
          i = i + 1
        }
      }
    }
    i
  }

}

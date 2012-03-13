package org.kevoree.library.sky.jails

import java.net.InetAddress
import java.io._
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 10:37
 */

object PropertyHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def checkMask(i: Int, j: Int, k: Int, l: Int, subnet: String, mask: String): Boolean = {
    val maskInt = ~((1 << (32 - Integer.parseInt(mask))) - 1)
    val ipBytes = InetAddress.getByName(i + "." + j + "." + k + "." + l).getAddress
    val subnetBytes = InetAddress.getByName(subnet).getAddress
    val subnetInt = (subnetBytes(0) << 24) | (subnetBytes(1) << 16) | (subnetBytes(2) << 8) | (subnetBytes(3) << 0)
    val ipInt = (ipBytes(0) << 24) | (ipBytes(1) << 16) | (ipBytes(2) << 8) | (ipBytes(3) << 0)
    (subnetInt & maskInt) == (ipInt & maskInt)
  }

  def lookingForNewIp(ips: List[String], subnet: String, mask: String): String = {
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
              val inet = InetAddress.getByName(tmpIp)
              if (!inet.isReachable(5000)) {
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


  def copyFile(inputFile: String, outputFile: String): Boolean = {
    if (new File(inputFile).exists()) {
      try {
        if (new File(outputFile).exists()) {
          new File(outputFile).delete()
        }
        val reader = new DataInputStream(new FileInputStream(new File(inputFile)))
        val writer = new DataOutputStream(new FileOutputStream(new File(outputFile)))

        val bytes = new Array[Byte](2048)
        var length = reader.read(bytes)
        while (length != -1) {
          writer.write(bytes, 0, length)
          length = reader.read(bytes)

        }
        writer.flush()
        writer.close()
        reader.close()
        true
      } catch {
        case _@e => logger.error("Unable to copy {} on {}", Array[AnyRef](inputFile, outputFile), e); false
      }
    } else {
      logger.debug("Unable to find {}", inputFile)
      false
    }
  }


}

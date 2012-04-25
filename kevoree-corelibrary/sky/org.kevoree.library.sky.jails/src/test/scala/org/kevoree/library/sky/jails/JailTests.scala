package org.kevoree.library.sky.jails

import org.junit.Test
import util.matching.Regex
import java.net.InetAddress

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/12/11
 * Time: 11:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class JailTests {

//  @Test
  def testbuildCPUFrequency () {
    val frequency = "2.80GHz"
    var valueFrequency = java.lang.Double.parseDouble(frequency.substring(0, frequency.length() - 3))
    val unit = frequency.substring(frequency.length() - 3)
    if (unit.equalsIgnoreCase("ghz")) {
      valueFrequency = valueFrequency * 1024 * 1024 * 1024
    } else if (unit.equalsIgnoreCase("mhz")) {
      valueFrequency = valueFrequency * 1024 * 1024
    } else if (unit.equalsIgnoreCase("khz")) {
      valueFrequency = valueFrequency * 1024
    }
    valueFrequency = valueFrequency.longValue()

    var valueFrequency4Jail = java.lang.Double.parseDouble("1.40Ghz".substring(0, "1.40Ghz".length() - 3))
    val unit4Jail = frequency.substring(frequency.length() - 3)
    if (unit4Jail.equalsIgnoreCase("ghz")) {
      valueFrequency4Jail = valueFrequency4Jail * 1024 * 1024 * 1024
    } else if (unit4Jail.equalsIgnoreCase("mhz")) {
      valueFrequency4Jail = valueFrequency4Jail * 1024 * 1024
    } else if (unit4Jail.equalsIgnoreCase("khz")) {
      valueFrequency4Jail = valueFrequency4Jail * 1024
    }
    valueFrequency4Jail = valueFrequency4Jail.longValue()

    println(valueFrequency4Jail * 100 / valueFrequency)
    println(((valueFrequency4Jail * 100 / valueFrequency) + 0.5).intValue())


  }

  @Test
  def testJailRegex () {
    val ezjailListPattern =
      "(D.?)\\ \\ *([0-9][0-9]*|N/A)\\ \\ *((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))\\ \\ *([a-zA-Z0-9\\.][a-zA-Z0-9_\\.]*)\\ \\ *((?:(?:/[a-zA-Z0-9_\\.][a-zA-Z0-9_\\.]*)*))"
    val ezjailListRegex = new Regex(ezjailListPattern)

    //    "DS  N/A  10.0.1.2        node0                          /usr/jails/node0"

    "STA JID  IP              Hostname                       Root Directory\n--- ---- --------------- ------------------------------ ------------------------\nDR  1    10.0.21.1       mat1                           /usr/jails/mat1\nDR  2    10.0.0.3        edaubert_node0                 /usr/jails/edaubert_node0"





   /* "STA JID  IP              Hostname                       Root Directory\n--- ---- --------------- ------------------------------ ------------------------\nDR  8    10.0.1.2        node0                          /usr/jails/node0"*/
      .split("\n").foreach {
      line => line match {
        //      case ezjailListRegex() => println("toto")
        case ezjailListRegex(tmp, jid, ip, name, path) => println(path)
        case _ => println("no match")
      }
    }
  }

  //@Test
  def testLookingFornewIp () {
    val subnet = "10.0.0.0"
    val mask = "24"

    val ips = List("10.0.0.2", "10.0.0.4", "10.0.0.3", "10.0.0.6")

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
            println(tmpIp)
            if (!ips.contains(tmpIp)) {
              newIp = tmpIp
              found = true
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
    println(newIp)
  }

  private def checkMask (i: Int, j: Int, k: Int, l: Int, subnet: String, mask: String): Boolean = {
    val maskInt = ~((1 << (32 - Integer.parseInt(mask))) - 1)
    val ipBytes = InetAddress.getByName(i + "." + j + "." + k + "." + l).getAddress
    val subnetBytes = InetAddress.getByName(subnet).getAddress
    val subnetInt = (subnetBytes(0) << 24) | (subnetBytes(1) << 16) | (subnetBytes(2) << 8) | (subnetBytes(3) << 0)
    val ipInt = (ipBytes(0) << 24) | (ipBytes(1) << 16) | (ipBytes(2) << 8) | (ipBytes(3) << 0)
    (subnetInt & maskInt) == (ipInt & maskInt)
  }

  //@Test
  def useMask () {
    val s = "10.1.1.99";
    val a = InetAddress.getByName(s);
    val b = a.getAddress;
    val i = (b(0) << 24) | (b(1) << 16) | (b(2) << 8) | (b(3) << 0);
    val subnetBytes = InetAddress.getByName("10.1.1.0").getAddress
    val subnetInt = (subnetBytes(0) << 24) | (subnetBytes(1) << 16) | (subnetBytes(2) << 8) | (subnetBytes(3) << 0)
    val subnet = 0x0A010100; // 10.1.1.0/24
    println(subnetInt + "\t" + subnet)
    val bits = 24;
    val ip = 0x0A010199; // 10.1.1.99

    // Create bitmask to clear out irrelevant bits. For 10.1.1.0/24 this is
    // 0xFFFFFF00 -- the first 24 bits are 1's, the last 8 are 0's.
    //
    // --> 32 - bits       == 8
    // --> 1 << 8          == 0x00000100
    // --> (1 << 8) - 1    == 0x000000FF
    // --> ~((1 << 8) - 1) == 0xFFFFFF00
    val mask = ~((1 << (32 - bits)) - 1);

    if ((subnet & mask) == (ip & mask)) {
      // IP address is in the subnet.
      println("toto")
    }
  }

}
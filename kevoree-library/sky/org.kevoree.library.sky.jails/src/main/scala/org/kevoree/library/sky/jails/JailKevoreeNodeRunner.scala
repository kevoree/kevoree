package org.kevoree.library.sky.jails

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io._
import java.lang.Thread
import actors.{TIMEOUT, Actor}
import util.matching.Regex
import org.kevoree.library.sky.manager.{Helper, KevoreeNodeRunner}
import java.net.InetAddress

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class JailKevoreeNodeRunner (nodeName: String, bootStrapModel: String, inet: String, subnet: String, mask: String)
  extends KevoreeNodeRunner(nodeName, bootStrapModel) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[JailKevoreeNodeRunner])

  private val resultActor = new ResultManagementActor()

  /*private val createJail = "ezjail-admin create -f kevjail <name> <IP>"
  private val startJail = "ezjail-admin onestart <name>"
  private val stopJail = "ezjail-admin onestop <name>"
  private val deleteJail = "ezjail-admin delete -w <name>"
  private val createIPAlias = "ifconfig <inet> alias <IP>"
  private val deleteIPAlias = "ifconfig <inet> -alias <IP>"*/
  //  private val listJails = Array[String]("/usr/local/bin/ezjail-admin", "list")

  val ezjailListPattern = "(D.?)\\ \\ *([0-9][0-9]*|N/A)\\ \\ *((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))\\ \\ *([a-zA-Z0-9\\.][a-zA-Z0-9\\.]*)\\ \\ *(?:(/[a-zA-Z0-9\\.][a-zA-Z0-9\\.]*)*)"
  val ezjailListRegex = new Regex(ezjailListPattern)

  val ezjailAdmin = "/usr/local/bin/ezjail-admin"
  val jexec = "/usr/sbin/jexec"
  val ifconfig = "/sbin/ifconfig"


  private var listJailsProcessBuilder: ProcessBuilder = null

  def startNode (): Boolean = {
    logger.debug("Start " + nodeName)
    // looking for currently launched jail
    listJailsProcessBuilder = new ProcessBuilder
    listJailsProcessBuilder.command("/usr/local/bin/ezjail-admin", "list")
    resultActor.starting()
    var p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array())).start()
    var result = resultActor.waitingFor(10000)
    var notFound = true
    var ips: List[String] = List[String]()
    result._2.split("\n").foreach {
      line =>
        line match {
          case ezjailListRegex(tmp, jid, ip, name, path) => {
            if (name == nodeName) {
              notFound = false
            }
            ips = ips ++ List(ip)
          }
          case _ =>
        }
    }
    if (result._1 && notFound) {
      // we create a new IP alias according to the existing ones
      val newIp = lookingForNewIp(ips)
      resultActor.starting()
      p = Runtime.getRuntime.exec(Array[String](ifconfig, inet, "alias", newIp))
      new Thread(new
          ProcessStreamManager(p.getInputStream, Array(), Array(new Regex("ifconfig: ioctl \\(SIOCDIFADDR\\): .*"))))
        .start()
      result = resultActor.waitingFor(10000)
      if (result._1) {
        // create the new jail
        resultActor.starting()
        p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "create", "-f", "kevjail", nodeName, newIp))
        new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array(new Regex("Error:.*")))).start()
        result = resultActor.waitingFor(60000)
        if (result._1) {
          // install the model on the jail
          resultActor.starting()
          var p = listJailsProcessBuilder.start()
          new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array())).start()
          var result = resultActor.waitingFor(10000)
          var jailPath = ""
          var jailId = "-1"
          var ips: List[String] = List[String]()
          result._2.split("\n").foreach {
            line =>
              line match {
                case ezjailListRegex(tmp, jid, ip, name, path) => {
                  if (name == nodeName) {
                    jailPath = path
                    jailId = jid
                  }
                }
                case _ =>
              }
          }

          resultActor.starting()
          p = Runtime.getRuntime.exec(Array[String]("cp", bootStrapModel,
                                                     jailPath + File.separator + "root" + File.separator +
                                                       "bootstrapmodel.kev"))
          new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array())).start()
          result = resultActor.waitingFor(10000)
          if (result._1) {
            // get platform runtime and add it into the jail
            resultActor.starting()
            p = Runtime.getRuntime.exec(Array[String]("cp", Helper.getJarPath,
                                                       jailPath + File.separator + "root" + File.separator +
                                                         "kevoree-runtime.jar"))
            new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array())).start()
            result = resultActor.waitingFor(10000)
            // launch the jail
            resultActor.starting()
            p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestart", nodeName))
            new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array())).start()
            result = resultActor.waitingFor(10000)
            if (result._1) {
              resultActor.starting()
              // FIXME set the log level according to current log level
              p = Runtime.getRuntime.exec(Array[String](jexec, jailId,
                                                         "java -Dnode.name=" + nodeName + "-Dnode.bootstrap=" +
                                                           jailPath + File.separator + "root" + File.separator +
                                                           "bootstrapmodel.kev -jar " + jailPath + File.separator +
                                                           "root" + File.separator + "kevoree-runtime.jar"))
              new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array())).start()
              result = resultActor.waitingFor(10000)
              result._1
            } else {
              logger.error("Something wrong happens:\n {}", result._2)
              false
            }
          } else {
            logger.error("Unable to set the model before launching the new jail:\n {}", result._2)
            false
          }
        } else {
          logger.error("Unable to create a new Jail:\n {}", result._2)
          false
        }
      } else {
        logger.error("Unable to define a new alias:\n {}", result._2)
        false
      }
    } else {
      // if an existing one have the same name, then it is not possible to launch this new one (return false)
      logger
        .error("There already exists a jail with the same name or it is not possible to check this:\n {}", result._2)
      false
    }

  }

  def stopNode (): Boolean = {
    logger.debug("stop " + nodeName)
    // looking for the jail that must be at least created
    resultActor.starting()
    resultActor.starting()
    var p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array())).start()
    var result = resultActor.waitingFor(10000)
    var found = false
    result._2.split("\n").foreach {
      line =>
        line match {
          case ezjailListRegex(tmp, jid, ip, name, path) => {
            if (name == nodeName) {
              found = true
            }
          }
          case _ =>
        }
    }
    if (result._1 && found) {
      // stop the jail
      resultActor.starting()
      p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestop", nodeName))
      new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array())).start()
      result = resultActor.waitingFor(10000)
      if (result._1) {
        // delete the jail
        resultActor.starting()
        p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "delete", "-w", nodeName))
        new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array())).start()
        result = resultActor.waitingFor(10000)
        if (result._1) {
          true
        } else {
          logger.error("Unable to delete the jail:\n {}", result._2)
          false
        }
      } else {
        logger.error("Unable to stop the jail:\n {}", result._2)
        false
      }
    } else {
      // if there is no jail corresponding to the nodeName then it is not possible to stop and delete it
      logger.error("Unable to find the corresponding jail:\n {}", result._2)
      false
    }
  }

  def updateNode (model: String): Boolean = {
    logger.error("update command is not available for jailNode")
    false
  }

  private def lookingForNewIp (ips: List[String]): String = {
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

  class ProcessStreamManager (inputStream: InputStream, outputRegexes: Array[Regex], errorRegexes: Array[Regex])
    extends Runnable {

    override def run () {
      val outputBuilder = new StringBuilder
      var errorBuilder = false
      try {
        val reader = new BufferedReader(new InputStreamReader(inputStream))
        var line = reader.readLine()
        while (line != null) {

          outputRegexes.find(regex => line match {
            case regex() => true
            case _ => false
          }) match {
            case Some(regex) => outputBuilder.append(line + "\n")
            case None =>
          }
          errorRegexes.find(regex => line match {
            case regex() => true
            case _ => false
          }) match {
            case Some(regex) => errorBuilder = true; outputBuilder.append(line + "\n")
            case None =>
          }
          println(line)
          line = reader.readLine()
        }
      } catch {
        case _@e => {
        }
      }
      if (errorBuilder) {
        resultActor.error(outputBuilder.toString())
      } else {
        resultActor.output(outputBuilder.toString())

      }
    }
  }

  class ResultManagementActor () extends Actor {

    case class STOP ()

    case class WAITINGFOR (timeout: Int)

    case class WAITINGFORPATH (timeout: Int)

    case class STARTING ()

    sealed abstract case class Result ()

    case class OUTPUT (data: String) extends Result

    case class ERROR (data: String) extends Result

    start()

    def stop () {
      this ! STOP()
    }

    def starting () {
      this ! STARTING()
    }

    def waitingFor (timeout: Int): (Boolean, String) = {
      (this !? WAITINGFOR(timeout)).asInstanceOf[(Boolean, String)]
    }

    def waitingForPath (timeout: Int): (Boolean, String) = {
      (this !? WAITINGFORPATH(timeout)).asInstanceOf[(Boolean, String)]
    }


    def output (data: String) {
      this ! OUTPUT(data)
    }

    def error (data: String) {
      this ! ERROR(data)
    }

    var firstSender = null

    def act () {
      loop {
        react {
          case STOP() => this.exit()
          case ERROR(data) =>
          case OUTPUT(data) =>
          case STARTING() => {
            var firstSender = this.sender
            react {
              case STOP() => this.exit()
              case WAITINGFOR(timeout) => {
                firstSender = this.sender
                reactWithin(timeout) {
                  case STOP() => this.exit()
                  case OUTPUT(data) => firstSender !(true, data)
                  case TIMEOUT => firstSender !
                    (false, "Timeout exceeds.")
                  case ERROR(data) => firstSender !(false, data)
                }
              }
              case WAITINGFORPATH(timeout) => {
                firstSender = this.sender
                reactWithin(timeout) {
                  case STOP() => this.exit()
                  case OUTPUT(data) => firstSender !(true, data)
                  case TIMEOUT => firstSender !
                    (false, "Timeout exceeds.")
                  case ERROR(data) => firstSender !(false, data)
                }
              }
              case OUTPUT(data) => {
                react {
                  case STOP() => this.exit()
                  case WAITINGFOR(timeout) => sender !(true, data)
                  case WAITINGFORPATH(timeout) => sender !(true, data)
                }
              }
              case ERROR(data) => {
                react {
                  case STOP() => this.exit()
                  case WAITINGFOR(timeout) => sender !(false, data)
                  case WAITINGFORPATH(timeout) => sender !(true, data)
                }
              }
            }
          }
        }
      }
    }
  }

}


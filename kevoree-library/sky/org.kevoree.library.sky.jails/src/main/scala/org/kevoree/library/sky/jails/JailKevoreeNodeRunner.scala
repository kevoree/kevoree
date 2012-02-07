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
import java.lang.Thread
import actors.{TIMEOUT, Actor}
import util.matching.Regex
import org.kevoree.library.sky.manager.{Helper, KevoreeNodeRunner}
import java.net.InetAddress
import org.kevoree.ContainerRoot
import scala.Array._
import org.kevoree.framework.KevoreePropertyHelper
import java.io._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class JailKevoreeNodeRunner (nodeName: String, bootStrapModel: String, inet: String, subnet: String, mask: String,
  model: ContainerRoot)
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

  val ezjailListPattern =
    "(D.?)\\ \\ *([0-9][0-9]*|N/A)\\ \\ *((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))\\ \\ *([a-zA-Z0-9\\.][a-zA-Z0-9_\\.]*)\\ \\ *((?:(?:/[a-zA-Z0-9_\\.][a-zA-Z0-9_\\.]*)*))"
  val ezjailListRegex = new Regex(ezjailListPattern)

  val ezjailAdmin = "/usr/local/bin/ezjail-admin"
  val jexec = "/usr/sbin/jexec"
  val ifconfig = "/sbin/ifconfig"


  private var listJailsProcessBuilder: ProcessBuilder = null

  var nodeProcess: Process = null

  def startNode (): Boolean = {
    logger.debug("Start " + nodeName)
    // looking for currently launched jail
    listJailsProcessBuilder = new ProcessBuilder
    listJailsProcessBuilder.command("/usr/local/bin/ezjail-admin", "list")
    resultActor.starting()
    var p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
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
      logger.debug("running {} {} alias {}", Array[AnyRef](ifconfig, inet, newIp))
      p = Runtime.getRuntime.exec(Array[String](ifconfig, inet, "alias", newIp))
      new Thread(new
          ProcessStreamManager(p.getInputStream, Array(), Array(new Regex("ifconfig: ioctl \\(SIOCDIFADDR\\): .*")), p))
        .start()
      result = resultActor.waitingFor(10000)
      if (result._1) {
        // create the new jail
        resultActor.starting()
        logger.debug("running {} create -f kevjail {} {}", Array[AnyRef](ezjailAdmin, nodeName, newIp))
        p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "create", "-f", "kevjail", nodeName, newIp))
        new Thread(new ProcessStreamManager(p.getErrorStream, Array(), Array(new Regex("^Error.*")), p)).start()
        result = resultActor.waitingFor(120000)
        if (result._1) {
          // install the model on the jail
          resultActor.starting()
          var p = listJailsProcessBuilder.start()
          new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
          var result = resultActor.waitingFor(10000)
          var jailPath = ""
          result._2.split("\n").foreach {
            line =>
              line match {
                case ezjailListRegex(tmp, jid, ip, name, path) => {
                  if (name == nodeName) {
                    jailPath = path
                  }
                }
                case _ =>
              }
          }
          logger.debug("trying to copy bootstrap model from {} to {}", bootStrapModel, jailPath + File.separator + "root" + File.separator + "bootstrapmodel.kev")
          logger.debug("trying to copy runtime platform from {} to {}", Helper.getJarPath, jailPath + File.separator + "root" + File.separator + "kevoree-runtime.jar")
          // get platform runtime and add it into the jail
          if (copyFile(bootStrapModel, jailPath + File.separator + "root" + File.separator + "bootstrapmodel.kev") &&
            copyFile(Helper.getJarPath, jailPath + File.separator + "root" + File.separator + "kevoree-runtime.jar")) {

            // specify limitation on jail such as CPU, RAM
            if (specifyConstraints()) {
              // configure ssh access
              configureSSH(model, jailPath, newIp)
              // launch the jail
              resultActor.starting()
              logger.debug("running {} onestart {}", Array[AnyRef](ezjailAdmin, nodeName))
              p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestart", nodeName))
              new Thread(new ProcessStreamManager(p.getErrorStream, Array(), Array(), p)).start()
              result = resultActor.waitingFor(10000)
              if (result._1) {
                resultActor.starting()
                p = listJailsProcessBuilder.start()
                new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
                result = resultActor.waitingFor(10000)
                var jailId = "-1"
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
                val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
                var debug = "ERROR"
                if (root.isWarnEnabled) {
                  debug = "WARN"
                }
                if (root.isInfoEnabled) {
                  debug = "INFO"
                }
                if (root.isDebugEnabled) {
                  debug = "DEBUG"
                }
                //resultActor.starting()
                var exec = Array[String](jexec, jailId, "/usr/local/bin/java", "-Dnode.name=" + nodeName, "-Dnode.bootstrap=" + File.separator + "root" + File.separator + "bootstrapmodel.kev",
                                          "-Dnode.log.level=" + debug)
                exec = exec ++ Array[String]("-jar", File.separator + "root" + File.separator + "kevoree-runtime.jar")
                logger.debug("trying to launch {} {} {} {} {} {} {} {}", exec)
                nodeProcess = Runtime.getRuntime.exec(exec)
                val logFile = System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log"
                outFile = new File(logFile + ".out")
                logger.debug("writing logs about {} on {}", nodeName, outFile.getAbsolutePath)
                new Thread(new
                    ProcessStreamFileLogger(nodeProcess.getInputStream, /*nodeProcess.getErrorStream,*/
                                             outFile)).start()
                errFile = new File(logFile + ".err")
                logger.debug("writing logs about {} on {}", nodeName, errFile.getAbsolutePath)
                new Thread(new ProcessStreamFileLogger(nodeProcess.getErrorStream, /*nodeProcess.getErrorStream,*/ errFile)).start()
                //result = resultActor.waitingFor(10000)
                try {
                  nodeProcess.exitValue
                  false
                } catch {
                  case e: IllegalThreadStateException => {
                    logger.debug("platform " + nodeName + " is started")
                    true
                  }
                }
              } else {
                logger.error("Something wrong happens:\n {}", result._2)
                false
              }
            } else {
              logger.error("Unable to specify jail limitations about CPU and/or RAM:\n {}", result._2)
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
      logger.error("There already exists a jail with the same name or it is not possible to check this:\n {}", result._2)
      false
    }

  }

  def stopNode (): Boolean = {
    logger.debug("stop " + nodeName)
    // looking for the jail that must be at least created
    resultActor.starting()
    var p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
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
      logger.debug("running {} onestop {}", Array[AnyRef](ezjailAdmin, nodeName))
      p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestop", nodeName))
      new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array(), p)).start()
      result = resultActor.waitingFor(10000)
      if (result._1) {
        // delete the jail
        resultActor.starting()
        logger.debug("running {} delete -w {}", Array[AnyRef](ezjailAdmin, nodeName))
        p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "delete", "-w", nodeName))
        new Thread(new ProcessStreamManager(p.getInputStream, Array(), Array(), p)).start()
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

  private def checkMask (i: Int, j: Int, k: Int, l: Int, subnet: String, mask: String): Boolean = {
    val maskInt = ~((1 << (32 - Integer.parseInt(mask))) - 1)
    val ipBytes = InetAddress.getByName(i + "." + j + "." + k + "." + l).getAddress
    val subnetBytes = InetAddress.getByName(subnet).getAddress
    val subnetInt = (subnetBytes(0) << 24) | (subnetBytes(1) << 16) | (subnetBytes(2) << 8) | (subnetBytes(3) << 0)
    val ipInt = (ipBytes(0) << 24) | (ipBytes(1) << 16) | (ipBytes(2) << 8) | (ipBytes(3) << 0)
    (subnetInt & maskInt) == (ipInt & maskInt)
  }

  private def specifyConstraints (): Boolean = {
    logger.debug("try to specify constraints")
    // TODO manage ARCH parameter
    model.getNodes.find(node => node.getName == nodeName) match {
      case None => logger.debug("Unable to find information about the node to start"); false
      case Some(node) => {
        var modeId = "log"
        var property = KevoreePropertyHelper.getPropertyForNode(model, nodeName, "MODE").getOrElse("RELAX").toString
        if (property == "STRICT") {
          modeId = "sigkill"
        } else if (property == "AVOID") {
          modeId = "deny"
        }

        var execResult = true
        var exec = Array[String]()
        logger.debug("asking to node property {}...", "RAM")
        property = KevoreePropertyHelper.getPropertyForNode(model, nodeName, "RAM").getOrElse("N/A").toString
        logger.debug("{} = {}", "RAM", property)
        if (property != "N/A") {
          try {
            val limit = java.lang.Long.parseLong(property.toString) * 1024 * 1024
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":vmem:" + modeId + ":" + limit)
            resultActor.starting()
            logger.debug("running {}", exec)
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(10000)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set RAM limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger
              .warn("Unable to take into account RAM limitation because the value {} is not well defined for {}",
                     property, nodeName)
          }
        }
        property = buildCPUFrequency(KevoreePropertyHelper.getPropertyForNode(model, nodeName, "CPU_FREQUENCY")
          .getOrElse("N/A").toString)
        if (execResult && property != "N/A") {
          try {
            val limit = Integer.parseInt(property.toString)
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":pctcpu:" + modeId + ":" + limit)
            resultActor.starting()
            logger.debug("running {}", exec)
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(10000)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set CPU_FREQUENCY limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger
              .warn("Unable to take into account CPU_FREQUENCY limitation because the value {} is not well defined for {}",
                     property, nodeName)
          }
        }
        /*property = getNodeProperty("CPU_CORE", nodeName)
        if (execResult && property != "N/A") {
          try {
            val limit = Integer.parseInt(property.toString)
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":vmem:" + modeId + ":" + limit) // TODO use cpuset command
            resultActor.starting()
            logger.debug("running {}", exec)
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(p.getErrorStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(10000)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set CPU_CORE limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger
              .warn("Unable to take into account CPU_CORE limitation because the value {} is not well defined for {}",
                     property, nodeName)
          }
        }*/
        property = KevoreePropertyHelper.getPropertyForNode(model, nodeName, "WALLCLOCKTIME").getOrElse("N/A").toString
        if (execResult && property != "N/A") {
          try {
            val limit = Integer.parseInt(property.toString)
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":wallclock:" + modeId + ":" + limit)
            resultActor.starting()
            logger.debug("running {}", exec)
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(10000)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set WALLCLOCKTIME limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger
              .warn("Unable to take into account WALLCLOCKTIME limitation because the value {} is not well defined for {}",
                     property, nodeName)
          }
        }
        property = KevoreePropertyHelper.getPropertyForNode(model, nodeName, "DATA_SIZE").getOrElse("N/A").toString
        if (execResult && property != "N/A") {
          try {
            val limit = Integer.parseInt(property.toString)
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":data:" + modeId + ":" + limit)
            resultActor.starting()
            logger.debug("running {}", exec)
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(10000)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set DATA_SIZE limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger
              .warn("Unable to take into account DATA_SIZE limitation because the value {} is not well defined for {}",
                     property, nodeName)
          }
        }
        logger.debug("specify constraints is done: {}", execResult)
        execResult
      }
    }
  }

  private def buildCPUFrequency (property: String): String = {
    if (property != "N/A") {
      if (property.toLowerCase.endsWith("ghz") && property.toLowerCase.endsWith("Mhz") &&
        property.toLowerCase.endsWith("khz")) {
        val exec = Array[String]("sysctl", "-a", "|", "egrep", "-i", "hw.model")
        resultActor.starting()
        logger.debug("running {}", exec)
        val p = Runtime.getRuntime.exec(exec)
        new Thread(new ProcessStreamManager(p.getInputStream, Array(new Regex("hw.model:.*@ (.*)")), Array(), p))
          .start()
        val result = resultActor.waitingFor(2000)
        if (result._1) {
          val frequency = result._2.trim()
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

          var valueFrequency4Jail = java.lang.Double.parseDouble(property.substring(0, property.length() - 3))
          val unit4Jail = frequency.substring(frequency.length() - 3)
          if (unit4Jail.equalsIgnoreCase("ghz")) {
            valueFrequency4Jail = valueFrequency4Jail * 1024 * 1024 * 1024
          } else if (unit4Jail.equalsIgnoreCase("mhz")) {
            valueFrequency4Jail = valueFrequency4Jail * 1024 * 1024
          } else if (unit4Jail.equalsIgnoreCase("khz")) {
            valueFrequency4Jail = valueFrequency4Jail * 1024
          }
          valueFrequency4Jail = valueFrequency4Jail.longValue()
          ((valueFrequency4Jail * 100 / valueFrequency) + 0.5).intValue() + ""
        } else {
          "N/A"
        }
      } else {
        logger.debug("Unable to take into account CPU_FREQUENCY parameter!")
        "N/A"
      }
    } else {
      "N/A"
    }
  }

  /*private def getNodeProperty (key: String, nodeName: String): Option[String] = {
    model.getNodes.find(node => node.getName == nodeName) match {
      case None => None
      case Some(node) => {
        node.getDictionary match {
          case None => {
            getDefaultValue(node.getTypeDefinition, key)
          }
          case Some(dictionary) => {
            dictionary.getValues.find(dictionaryAttribute => dictionaryAttribute.getAttribute.getName == key) match {
              case None => None
              case Some(dictionaryAttribute) => dictionaryAttribute.getValue
            }
          }
        }
        true
      }
    }
    None
  }

  private def getDefaultValue (typeDefinition: TypeDefinition, key: String): String = {
    typeDefinition.getDictionaryType.get.getDefaultValues
      .find(defaultValue => defaultValue.getAttribute.getName == key) match {
      case None => ""
      case Some(defaultValue) => defaultValue.getValue
    }
  }*/

  private def copyFile (inputFile: String, outputFile: String): Boolean = {
    logger.debug("trying to copy {} to {}", inputFile, outputFile)
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

  class ProcessStreamFileLogger (inputStream: InputStream /*, errorStream: InputStream*/ , file: File)
    extends Runnable {
    override def run () {
      try {
        //        outFile = new File(file)
        //        errFile = new File(file + ".err")
        val outputStream = new FileWriter(file)
        //        val errorOutputStream = new FileWriter(errFile)
        val readerIn = new BufferedReader(new InputStreamReader(inputStream))
        //        val readerErr = new BufferedReader(new InputStreamReader(errorStream))
        var lineIn = readerIn.readLine()
        //        var lineErr = readerErr.readLine()
        while (lineIn != null /*|| lineErr != null*/ ) {
          if (lineIn != null) {
            outputStream.write(lineIn + "\n")
          }
          /*if (lineErr != null) {
            errorOutputStream.write(lineErr + "\n")
          }*/
          lineIn = readerIn.readLine()
          //          lineErr = readerErr.readLine()
        }
      } catch {
        case _@e => e.printStackTrace()
      }
    }
  }


  class ProcessStreamManager (inputStream: InputStream, outputRegexes: Array[Regex], errorRegexes: Array[Regex],
    p: Process)
    extends Runnable {

    override def run () {
      val outputBuilder = new StringBuilder
      var errorBuilder = false
      try {
        val reader = new BufferedReader(new InputStreamReader(inputStream))
        var line = reader.readLine()
        while (line != null) {

          outputRegexes.find(regex => {
            val m = regex.pattern.matcher(line)
            m.find()
          }) match {
            case Some(regex) => outputBuilder.append(line + "\n")
            case none =>
          }
          errorRegexes.find(regex => {
            val m = regex.pattern.matcher(line)
            m.find()
          }) match {
            case Some(regex) => errorBuilder = true; outputBuilder.append(line + "\n")
            case none =>
          }
          line = reader.readLine()
        }
      } catch {
        case _@e =>
      }
      val exitValue = p.waitFor()
      if (errorBuilder || exitValue != 0) {
        resultActor.error(outputBuilder.toString())
      } else {
        resultActor.output(outputBuilder.toString())
      }
    }
  }

  class ResultManagementActor () extends Actor {

    case class STOP ()

    case class WAITINGFOR (timeout: Int)

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
              case OUTPUT(data) => {
                react {
                  case STOP() => this.exit()
                  case WAITINGFOR(timeout) => firstSender !(true, data)
                }
              }
              case ERROR(data) => {
                react {
                  case STOP() => this.exit()
                  case WAITINGFOR(timeout) => firstSender !(false, data)
                }
              }
            }
          }
        }
      }
    }
  }

}


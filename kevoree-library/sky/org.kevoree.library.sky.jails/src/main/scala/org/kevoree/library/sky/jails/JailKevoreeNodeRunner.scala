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

import log.{ProcessStreamManager, ResultManagementActor}
import nodeType.JailNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread
import util.matching.Regex
import org.kevoree.library.sky.manager.KevoreeNodeRunner
import scala.Array._
import java.io._
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class JailKevoreeNodeRunner (iaasNode: JailNode) extends KevoreeNodeRunner(iaasNode.getNodeName) {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val ezjailListPattern =
    "(D.?)\\ \\ *([0-9][0-9]*|N/A)\\ \\ *((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))\\ \\ *([a-zA-Z0-9\\.][a-zA-Z0-9_\\.]*)\\ \\ *((?:(?:/[a-zA-Z0-9_\\.][a-zA-Z0-9_\\.]*)*))"
  val ezjailListRegex = new Regex(ezjailListPattern)

  val ezjailAdmin = "/usr/local/bin/ezjail-admin"
  val jexec = "/usr/sbin/jexec"
  val ifconfig = "/sbin/ifconfig"


  private var listJailsProcessBuilder: ProcessBuilder = null

  var nodeProcess: Process = null

  def startNode (iaasModel: ContainerRoot, jailBootStrapModel: ContainerRoot): Boolean = {
    logger.debug("Start " + nodeName)
    // looking for currently launched jail
    listJailsProcessBuilder = new ProcessBuilder
    listJailsProcessBuilder.command("/usr/local/bin/ezjail-admin", "list")
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    var p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
    var result = resultActor.waitingFor(2000)
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
      var newIp = "127.0.0.1"
      // check if the node have a inet address
      val ipOption = KevoreePropertyHelper.getStringNetworkProperty(iaasModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
      if (ipOption.isDefined) {
        newIp = ipOption.get
      } else {
        // we create a new IP alias according to the existing ones
        newIp = PropertyHelper.lookingForNewIp(ips, iaasNode.getNetwork, iaasNode.getMask)
      }

      val resultActor2 = new ResultManagementActor()
      resultActor2.starting()
      logger.debug("running {} {} alias {}", Array[AnyRef](ifconfig, iaasNode.getNetworkInterface, newIp))
      p = Runtime.getRuntime.exec(Array[String](ifconfig, iaasNode.getNetworkInterface, "alias", newIp))
      new Thread(new
          ProcessStreamManager(resultActor2, p.getInputStream, Array(), Array(new Regex("ifconfig: ioctl \\(SIOCDIFADDR\\): .*")), p))
        .start()
      result = resultActor2.waitingFor(1000)
      if (result._1) {
        // create the new jail

        val resultActor = new ResultManagementActor()
        resultActor.starting()
        logger.debug("running {} create -f {} {} {}", Array[AnyRef](ezjailAdmin, iaasNode.getFlavor, nodeName, newIp))
        p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "create", "-f", iaasNode.getFlavor, nodeName, newIp))
        new Thread(new ProcessStreamManager(resultActor, p.getErrorStream, Array(), Array(new Regex("^Error.*")), p)).start()
        result = resultActor.waitingFor(120000)
        if (result._1) {
          // install the model on the jail
          val resultActor = new ResultManagementActor()
          resultActor.starting()
          var p = listJailsProcessBuilder.start()
          new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
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

          val platformFile = iaasNode.getBootStrapperService.resolveKevoreeArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", KevoreeFactory.getVersion);
          KevoreeXmiHelper.save(jailPath + File.separator + "root" + File.separator + "bootstrapmodel.kev", jailBootStrapModel)
          if (PropertyHelper.copyFile(platformFile.getAbsolutePath, jailPath + File.separator + "root" + File.separator + "kevoree-runtime.jar")) {

            // specify limitation on jail such as CPU, RAM
            if (JailsConstraintsConfiguration.applyJailConstraints(iaasModel, nodeName)) {
              // configure ssh access
              configureSSHServer(iaasModel, jailPath, newIp)
              // launch the jail
              val resultActor = new ResultManagementActor()
              resultActor.starting()
              logger.debug("running {} onestart {}", Array[AnyRef](ezjailAdmin, nodeName))
              p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestart", nodeName))
              new Thread(new ProcessStreamManager(resultActor, p.getErrorStream, Array(), Array(), p)).start()
              result = resultActor.waitingFor(10000)
              if (result._1) {
                val resultActor = new ResultManagementActor()
                resultActor.starting()
                p = listJailsProcessBuilder.start()
                new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
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
                /*  val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
                var debug = "ERROR"
                if (root.isWarnEnabled) {
                  debug = "WARN"
                }
                if (root.isInfoEnabled) {
                  debug = "INFO"
                }
                if (root.isDebugEnabled) {
                  debug = "DEBUG" // TODO must  be use to set the log level of the kevoree core
                }*/
                var exec = Array[String](jexec, jailId, "/usr/local/bin/java", "-Dnode.name=" + nodeName, "-Dnode.bootstrap=" + File.separator + "root" + File.separator + "bootstrapmodel.kev",
                                          "-Dnode.log.level=INFO" /* + debug*/)
                exec = exec ++ Array[String]("-jar", File.separator + "root" + File.separator + "kevoree-runtime.jar")
                logger.debug("trying to launch {} {} {} {} {} {} {} {}", exec)
                nodeProcess = Runtime.getRuntime.exec(exec)
                val logFile = System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log"
                outFile = new File(logFile + ".out")
                logger.debug("writing logs about {} on {}", nodeName, outFile.getAbsolutePath)
                new Thread(new
                    ProcessStreamFileLogger(nodeProcess.getInputStream, outFile)).start()
                errFile = new File(logFile + ".err")
                logger.debug("writing logs about {} on {}", nodeName, errFile.getAbsolutePath)
                new Thread(new ProcessStreamFileLogger(nodeProcess.getErrorStream, errFile)).start()
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
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    var p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
    var result = resultActor.waitingFor(10000)
    var found = false
    var oldIP = ""
    result._2.split("\n").foreach {
      line =>
        line match {
          case ezjailListRegex(tmp, jid, ip, name, path) => {
            if (name == nodeName) {
              found = true
              oldIP = ip
            }
          }
          case _ =>
        }
    }
    if (result._1 && found) {
      // stop the jail
      val resultActor = new ResultManagementActor()
      resultActor.starting()
      logger.debug("running {} onestop {}", Array[AnyRef](ezjailAdmin, nodeName))
      p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestop", nodeName))
      new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(), p)).start()
      result = resultActor.waitingFor(10000)
      if (result._1) {
        // delete the jail
        val resultActor = new ResultManagementActor()
        resultActor.starting()
        logger.debug("running {} delete -w {}", Array[AnyRef](ezjailAdmin, nodeName))
        p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "delete", "-w", nodeName))
        new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(), p)).start()
        result = resultActor.waitingFor(10000)
        if (result._1) {
          // release IP alias to allow next IP select to use this one
          val resultActor = new ResultManagementActor()
          resultActor.starting()
          p = Runtime.getRuntime.exec(Array[String](ifconfig, iaasNode.getNetworkInterface, "-alias", oldIP))
          new Thread(new
              ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(new Regex("ifconfig: ioctl \\(SIOCDIFADDR\\): .*")), p))
            .start()
          result = resultActor.waitingFor(1000)
          if (!result._1) {
            logger.debug("unable to release ip alias {} for the network interface {}", oldIP, iaasNode.getNetworkInterface)
          }
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

  class ProcessStreamFileLogger (inputStream: InputStream, file: File)
    extends Runnable {
    override def run () {
      try {
        val outputStream = new FileWriter(file)
        val readerIn = new BufferedReader(new InputStreamReader(inputStream))
        var lineIn = readerIn.readLine()
        while (lineIn != null) {
          if (lineIn != null) {
            outputStream.write(lineIn + "\n")
          }
          lineIn = readerIn.readLine()
        }
      } catch {
        case _@e => e.printStackTrace()
      }
    }
  }


}


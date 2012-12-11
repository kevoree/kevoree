package org.kevoree.library.sky.jails.process

import scala.Array._
import util.matching.Regex
import java.io.File
import org.kevoree.library.sky.api.KevoreeNodeRunner
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.library.sky.api.property.PropertyConversionHelper
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.kevoree.library.sky.api.nodeType.helper.SubnetUtils


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/03/12
 * Time: 09:32
 *
 * @author Erwan Daubert
 * @version 1.0
 **/

class ProcessExecutor(creationTimeout: Int, startTimeout: Int) {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val listJailsProcessBuilder = new ProcessBuilder
  listJailsProcessBuilder.command("/usr/local/bin/ezjail-admin", "list")

  val ezjailListPattern = "(D.?)\\ \\ *([0-9][0-9]*|N/A)\\ \\ *((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))\\ \\ *([a-zA-Z0-9\\.][a-zA-Z0-9_\\.]*)\\ \\ *((?:(?:/[a-zA-Z0-9_\\.][a-zA-Z0-9_\\.]*)*))"
  val ezjailListRegex = new Regex(ezjailListPattern)


  val ifconfig = "/sbin/ifconfig"
  val ezjailAdmin = "/usr/local/bin/ezjail-admin"
  val jexec = "/usr/sbin/jexec"

  def listIpJails(nodeName: String): (Boolean, List[String]) = {
    // looking for currently launched jail
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    val p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
    val result = resultActor.waitingFor(2000)
    var notFound = true
    var ips: List[String] = List[String]()
    if (result._1) {
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
    } else {
      logger.debug(result._2)
    }

    (result._1 && notFound, ips)
  }

  def addNetworkAlias(networkInterface: String, newIp: String, mask: String = 24): Boolean = {
    val fromCidrNotation = SubnetUtils.fromCidrNotation(newIp + "/" + mask)
    logger.debug("running {} {} alias {} netmask {}", Array[AnyRef](ifconfig, networkInterface, fromCidrNotation(0), fromCidrNotation(1)))
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    val p = Runtime.getRuntime.exec(Array[String](ifconfig, networkInterface, "alias", fromCidrNotation(0), "netmask", fromCidrNotation(1)))
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(new Regex("ifconfig: ioctl \\(SIOCDIFADDR\\): .*")), p)).start()
    val result = resultActor.waitingFor(1000)
    if (!result._1) {
      logger.debug("Unable to configure alias: {}", result._2)
    }
    result._1
  }

  def createJail(flavor: String, nodeName: String, newIp: String, archive: Option[String]): Boolean = {
    // TODO add archive attribute and use it to save the jail => the archive must be available from all nodes of the network
    logger.debug("running {} create -f {} {} {}", Array[AnyRef](ezjailAdmin, flavor, nodeName, newIp))
    val exec = Array[String](ezjailAdmin, "create", "-f") ++ Array[String](flavor) ++ Array[String](nodeName, newIp)
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    val p = Runtime.getRuntime.exec(exec)
    new Thread(new ProcessStreamManager(resultActor, p.getErrorStream, Array(), Array(new Regex("^Error.*")), p)).start()
    val result = resultActor.waitingFor(creationTimeout)
    if (!result._1) {
      logger.debug(result._2)
    }
    result._1
  }


  def findPathForJail(nodeName: String): String = {
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    val p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
    val result = resultActor.waitingFor(1000)
    var jailPath = ""
    if (result._1) {
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
    } else {
      logger.debug(result._2)
    }
    jailPath
  }

  def startJail(nodeName: String): Boolean = {
    logger.debug("running {} onestart {}", Array[AnyRef](ezjailAdmin, nodeName))
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    val p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestart", nodeName))
    new Thread(new ProcessStreamManager(resultActor, p.getErrorStream, Array(), Array(), p)).start()
    val result = resultActor.waitingFor(startTimeout)
    if (!result._1) {
      logger.debug(result._2)
    }
    result._1
  }

  def findJail(nodeName: String): (String, String, String) = {
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    val p = listJailsProcessBuilder.start()
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(ezjailListRegex), Array(), p)).start()
    val result = resultActor.waitingFor(1000)
    var jailPath = "-1"
    var jailId = "-1"
    var jailIP = "-1"
    if (result._1) {
      result._2.split("\n").foreach {
        line =>
          line match {
            case ezjailListRegex(tmp, jid, ip, name, path) => {
              if (name == nodeName) {
                jailPath = path
                jailId = jid
                jailIP = ip
              }
            }
            case _ =>
          }
      }
    } else {
      logger.debug(result._2)
    }
    (jailPath, jailId, jailIP)
  }

  def startKevoreeOnJail(jailId: String, ram: String, nodeName: String /*, outFile: File, errFile: File*/ , runner: KevoreeNodeRunner, iaasNode: AbstractHostNode): Boolean = {
    logger.debug("trying to start Kevoree node on jail {} ", nodeName)
    // FIXME java memory properties must define as Node properties
    // Currently the kloud provider only manages PJavaSeNode that hosts the software user configuration
    // It will be better to add a new node hosted by the PJavaSeNode
    var exec = Array[String](jexec, jailId, "/usr/local/bin/java")
    if (ram != null && ram != "N/A") {
      var limit = 0l
      try {
        limit = PropertyConversionHelper.getRAM(ram)
        exec = exec ++ Array[String](/*"-Xms512m", */ "-Xmx" + limit + "m")
      } catch {
        case e: NumberFormatException => logger.warn("Unable to take into account RAM limitation because the value {} is not well defined for {}. Default value used.", ram, nodeName)
      }

    }
    exec = exec ++ Array[String](/*"-XX:PermSize=256m", "-XX:MaxPermSize=512m", */ "-Djava.awt.headless=true",
      "-Dnode.name=" + nodeName, "-Dnode.update.timeout=" + System.getProperty("node.update.timeout"),
      "-Dnode.bootstrap=" + File.separator + "root" + File.separator + "bootstrapmodel.kev", "-jar",
      File.separator + "root" + File.separator + "kevoree-runtime.jar")
    logger.debug("trying to launch {} {} {} {} {} {} {} {}", exec.asInstanceOf[Array[AnyRef]])
    val nodeProcess = Runtime.getRuntime.exec(exec)
    /*logger.debug("writing logs about {} on {}", nodeName, outFile.getAbsolutePath)
    new Thread(new ProcessStreamFileLogger(nodeProcess.getInputStream, outFile)).start()
    logger.debug("writing logs about {} on {}", nodeName, errFile.getAbsolutePath)
    new Thread(new ProcessStreamFileLogger(nodeProcess.getErrorStream, errFile)).start()*/
    runner.configureLogFile(iaasNode, nodeProcess)
    try {
      nodeProcess.exitValue
      false
    } catch {
      case e: IllegalThreadStateException => {
        logger.debug("platform " + nodeName + " is started")
        true
      }
    }
  }

  def stopJail(nodeName: String): Boolean = {
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    logger.debug("running {} onestop {}", Array[AnyRef](ezjailAdmin, nodeName))
    val p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "onestop", nodeName))
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(), p)).start()
    val result = resultActor.waitingFor(startTimeout)
    if (!result._1) {
      logger.debug(result._2)
    }
    result._1
  }

  def deleteJail(nodeName: String): Boolean = {
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    logger.debug("running {} delete -w {}", Array[AnyRef](ezjailAdmin, nodeName))
    val p = Runtime.getRuntime.exec(Array[String](ezjailAdmin, "delete", "-w", nodeName))
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(), p)).start()
    val result = resultActor.waitingFor(creationTimeout)
    if (!result._1) {
      logger.debug(result._2)
    }
    result._1
  }

  def deleteNetworkAlias(networkInterface: String, oldIP: String): Boolean = {
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    logger.debug("running {} {} -alias {}", Array[AnyRef](ezjailAdmin, networkInterface, oldIP))
    val p = Runtime.getRuntime.exec(Array[String](ifconfig, networkInterface, "-alias", oldIP))
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(), Array(new Regex("ifconfig: ioctl \\(SIOCDIFADDR\\): .*")), p)).start()
    val result = resultActor.waitingFor(1000)
    if (!result._1) {
      logger.debug(result._2)
    }
    result._1
  }
}

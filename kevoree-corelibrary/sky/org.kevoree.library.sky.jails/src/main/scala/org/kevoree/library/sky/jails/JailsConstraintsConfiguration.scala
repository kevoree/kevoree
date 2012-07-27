package org.kevoree.library.sky.jails

import process.{ProcessStreamManager, ResultManagementActor}
import util.matching.Regex
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreePropertyHelper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 10:51
 */

object JailsConstraintsConfiguration {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def applyJailConstraints (model: ContainerRoot, nodeName: String): Boolean = {
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
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":vmemoryuse:" + modeId + "=" + limit)
            val resultActor = new ResultManagementActor()
            resultActor.starting()
            logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(500)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set RAM limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger.warn("Unable to take into account RAM limitation because the value {} is not well defined for {}", property, nodeName)
          }
        }
        /*property = getComputedSystemCPUFrequency(KevoreePropertyHelper.getPropertyForNode(model, nodeName, "CPU_FREQUENCY") // TODO seems to be not implemented
          .getOrElse("N/A").toString)
        if (execResult && property != "N/A") {
          try {
            val limit = Integer.parseInt(property.toString)
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":pctcpu:" + modeId + "=" + limit)
            val resultActor = new ResultManagementActor()
            resultActor.starting()
            logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(resultActor,p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(500)
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
        }*/
        /*property = getNodeProperty("CPU_CORE", nodeName)
        if (execResult && property != "N/A") {
          try {
            val limit = Integer.parseInt(property.toString)
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":vmem:" + modeId + "=" + limit) // TODO use cpuset command
            resultActor.starting()
            logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
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
            exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":wallclock:" + modeId + "=" + limit)
            val resultActor = new ResultManagementActor()
            resultActor.starting()
            logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
            val p = Runtime.getRuntime.exec(exec)
            new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
            val result = resultActor.waitingFor(500)
            if (result._1) {
              execResult = true
            } else {
              logger.debug("unable to set WALLCLOCKTIME limitation:\n{}", result._2)
              execResult = false
            }
          } catch {
            case e: NumberFormatException => logger.warn("Unable to take into account WALLCLOCKTIME limitation because the value {} is not well defined for {}", property, nodeName)
          }
        }
        property = KevoreePropertyHelper.getPropertyForNode(model, nodeName, "DATA_SIZE").getOrElse("N/A").toString
        if (execResult && property != "N/A") {
          var limit = 5 * 1024 * 1024
          if (property.toLowerCase.endsWith("gb") || property.toLowerCase.endsWith("g")) {
            limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024 * 1024
          } else if (property.toLowerCase.endsWith("mb") || property.toLowerCase.endsWith("m")) {
            limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024
          } else if (property.toLowerCase.endsWith("kb") || property.toLowerCase.endsWith("k")) {
            limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024
          } else {
            try {
              limit = Integer.parseInt(property.toString)
            } catch {
              case e: NumberFormatException => logger.warn("Unable to take into account DATA_SIZE limitation because the value {} is not well defined for {}. Default value used.", property, nodeName)
            }
          }

          exec = Array[String]("rctl", "-a", "jail:" + nodeName + ":datasize:" + modeId + "=" + limit)
          val resultActor = new ResultManagementActor()
          resultActor.starting()
          logger.debug("running {}", exec)
          val p = Runtime.getRuntime.exec(exec)
          new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
          val result = resultActor.waitingFor(500)
          if (result._1) {
            execResult = true
          } else {
            logger.debug("unable to set DATA_SIZE limitation:\n{}", result._2)
            execResult = false
          }
        }
        logger.debug("specify constraints is done: {}", execResult)
        execResult
      }
    }
  }

  private def getComputedSystemCPUFrequency (property: String): String = {
    if (property != "N/A") {
      if (property.toLowerCase.endsWith("ghz") && property.toLowerCase.endsWith("mhz") &&
        property.toLowerCase.endsWith("khz")) {
        val exec = Array[String]("sysctl", "-a", "|", "egrep", "-i", "hw.model")
        val resultActor = new ResultManagementActor()
        resultActor.starting()
        logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
        val p = Runtime.getRuntime.exec(exec)
        new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex("hw.model:.*@ (.*)")), Array(), p))
          .start()
        val result = resultActor.waitingFor(500)
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


  def removeJailConstraints (nodeName: String): Boolean = {
    val exec = Array[String]("rctl", "-r", "jail:" + nodeName)
    val resultActor = new ResultManagementActor()
    resultActor.starting()
    logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
    val p = Runtime.getRuntime.exec(exec)
    new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
    val result = resultActor.waitingFor(500)
    if (!result._1) {
      logger.debug("unable to remove jail limitations:\n{}", result._2)
    }
    result._1
  }


}

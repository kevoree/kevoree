package org.kevoree.library.sky.jails

import process.{ProcessStreamManager, ResultManagementActor}
import util.matching.Regex
import org.slf4j.LoggerFactory
import org.kevoree.{ContainerNode, ContainerRoot}
import org.kevoree.framework.KevoreePropertyHelper
import org.kevoree.library.sky.api.property.PropertyConversionHelper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 10:51
 */

object JailsConstraintsConfiguration {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def applyJailConstraints(model: ContainerRoot, node: ContainerNode): Boolean = {
    logger.debug("try to specify constraints")
    // TODO manage ARCH parameter
    /*model.getNodes.find(node => node.getName == nodeName) match {
      case None => logger.debug("Unable to find information about the node to start"); false
      case Some(node) => {*/
    var modeId = "log"
    var property = KevoreePropertyHelper.getProperty(node, "MODE").getOrElse("RELAX")
    if (property == "STRICT") {
      modeId = "sigkill"
    } else if (property == "AVOID") {
      modeId = "deny"
    }
    var execResult = true
    var exec = Array[String]()
    logger.debug("asking to node property {}...", "RAM")
    property = KevoreePropertyHelper.getProperty(node, "RAM").getOrElse("N/A")
    logger.debug("{} = {}", "RAM", property)
    if (property != "N/A") {
      try {
        //            var limit = 0
        /*if (property.toLowerCase.endsWith("gb") || property.toLowerCase.endsWith("g")) {
          limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024 * 1024
        } else if (property.toLowerCase.endsWith("mb") || property.toLowerCase.endsWith("m")) {
          limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024
        } else if (property.toLowerCase.endsWith("kb") || property.toLowerCase.endsWith("k")) {
          limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024
        } else {
          try {
            limit = Integer.parseInt(property)
          } catch {
            case e: NumberFormatException => logger
              .warn("Unable to take into account RAM limitation because the value {} is not well defined for {}. Default value used.", property, nodeName)
          }
        }*/
        val limit = PropertyConversionHelper.getRAM(property)
        // set RAM limitation if the attribute can be used
        if (limit > 0) {
          exec = Array[String]("rctl", "-a", "jail:" + node.getName + ":vmemoryuse:" + modeId + "=" + limit)
          val resultActor = new ResultManagementActor()
          resultActor.starting()
          logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
          val p = Runtime.getRuntime.exec(exec)
          new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
          val result = resultActor.waitingFor(500)
          if (result._1) {
            execResult = true
          } else {
            logger.debug("unable to set RAM limitation (empty line maybe mean the BSD kernel is not implemented rctl):\n{}", result._2)
            execResult = false
          }
        }
      } catch {
        case e: NumberFormatException => logger.warn("Unable to take into account RAM limitation because the value {} is not well defined for {}", property, node.getName)
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
          logger.debug("unable to set CPU_FREQUENCY limitation (empty line maybe mean the BSD kernel is not implemented rctl):\n{}", result._2)
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
          logger.debug("unable to set CPU_CORE limitation (empty line maybe mean the BSD kernel is not implemented rctl):\n{}", result._2)
          execResult = false
        }
      } catch {
        case e: NumberFormatException => logger
          .warn("Unable to take into account CPU_CORE limitation because the value {} is not well defined for {}",
                 property, nodeName)
      }
    }*/
    property = KevoreePropertyHelper.getProperty(node, "WALLCLOCKTIME").getOrElse("N/A")
    if (execResult && property != "N/A") {
      try {
        val limit = Integer.parseInt(property)
        exec = Array[String]("rctl", "-a", "jail:" + node.getName + ":wallclock:" + modeId + "=" + limit)
        val resultActor = new ResultManagementActor()
        resultActor.starting()
        logger.debug("running {}", exec.asInstanceOf[Array[AnyRef]])
        val p = Runtime.getRuntime.exec(exec)
        new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
        val result = resultActor.waitingFor(500)
        if (result._1) {
          execResult = true
        } else {
          logger.debug("unable to set WALLCLOCKTIME limitation (empty line maybe mean the BSD kernel is not implemented rctl):\n{}", result._2)
          execResult = false
        }
      } catch {
        case e: NumberFormatException => logger.warn("Unable to take into account WALLCLOCKTIME limitation because the value {} is not well defined for {}", property, node.getName)
      }
    }
    property = KevoreePropertyHelper.getProperty(node, "DISK_SIZE").getOrElse("N/A")
    if (execResult && property != "N/A") {
      //          var limit = 5 * 1024 * 1024
      /*if (property.toLowerCase.endsWith("gb") || property.toLowerCase.endsWith("g")) {
        limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024 * 1024
      } else if (property.toLowerCase.endsWith("mb") || property.toLowerCase.endsWith("m")) {
        limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024
      } else if (property.toLowerCase.endsWith("kb") || property.toLowerCase.endsWith("k")) {
        limit = Integer.parseInt(property.substring(0, property.length - 2)) * 1024 * 1024
      } else {
        try {
          limit = Integer.parseInt(property)
        } catch {
          case e: NumberFormatException => logger.warn("Unable to take into account DATA_SIZE limitation because the value {} is not well defined for {}. Default value used.", property, nodeName)
        }
      }*/
      try {
        val limit = PropertyConversionHelper.getDataSize(property)
        exec = Array[String]("rctl", "-a", "jail:" + node.getName + ":datasize:" + modeId + "=" + limit)
        val resultActor = new ResultManagementActor()
        resultActor.starting()
        logger.debug("running {}", exec)
        val p = Runtime.getRuntime.exec(exec)
        new Thread(new ProcessStreamManager(resultActor, p.getInputStream, Array(new Regex(".*")), Array(), p)).start()
        val result = resultActor.waitingFor(500)
        if (result._1) {
          execResult = true
        } else {
          logger.debug("unable to set DATA_SIZE limitation (empty line maybe mean the BSD kernel is not implemented rctl):\n{}", result._2)
          execResult = false
        }
      } catch {
        case e: NumberFormatException => logger.warn("Unable to take into account DATA_SIZE limitation because the value {} is not well defined for {}. Default value used.", property, node.getName)
      }
    }
    logger.debug("specify constraints is done: {}", execResult)
    execResult
  }

  private def getComputedSystemCPUFrequency(property: String): String = {
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
//          val frequency = result._2.trim()
          val valueFrequency = PropertyConversionHelper.getCPUFrequency(property)
          val valueFrequency4Jail = PropertyConversionHelper.getCPUFrequency(property)
          //          valueFrequency4Jail = valueFrequency4Jail.longValue()
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


  def removeJailConstraints(nodeName: String): Boolean = {
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

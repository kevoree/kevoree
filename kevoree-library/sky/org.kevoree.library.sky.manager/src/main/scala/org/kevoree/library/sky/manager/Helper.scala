package org.kevoree.library.sky.manager

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.aether.framework.AetherUtil
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.slf4j.{LoggerFactory, Logger}
import java.io.File

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 12:00
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object Helper {
  private val logger: Logger = LoggerFactory.getLogger(Helper.getClass)

  private var nodeName: String = null
  private var modelHandlerService: KevoreeModelHandlerService = null
  private var platformJARPath: String = null

  def getNodeName: String = {
    nodeName
  }

  def setNodeName (name: String) {
    nodeName = name
  }

  def getModelHandlerService: KevoreeModelHandlerService = {
    modelHandlerService
  }

  def setModelHandlerService (handler: KevoreeModelHandlerService) {
    modelHandlerService = handler
  }

  def saveModelOnFile (bootStrapModel: ContainerRoot): String = {
    val file = File.createTempFile("kevoreeTemp", "bootstrap.kev")

    KevoreeXmiHelper.save(file.getAbsolutePath, bootStrapModel)
    file.getAbsolutePath
  }

  def getJarPath: String = {
    logger.debug("trying to get the platform jar...")
    if (platformJARPath == null) {
      val model = KevoreeFactory.eINSTANCE.createContainerRoot

      // define repositories
      var repository = KevoreeFactory.eINSTANCE.createRepository
      repository.setUrl("http://maven.kevoree.org/release")
      model.addRepositories(repository)
      repository = KevoreeFactory.eINSTANCE.createRepository
      repository.setUrl("http://maven.kevoree.org/snapshots")
      model.addRepositories(repository)


      val deployUnit = KevoreeFactory.eINSTANCE.createDeployUnit
      deployUnit.setGroupName("org.kevoree.platform")
      deployUnit.setUnitName("org.kevoree.platform.osgi.standalone")
      logger.debug("before trying to get the version platform jar...")
      deployUnit.setVersion(getVersion)
      model.addDeployUnits(deployUnit)
      logger.debug("before trying to get the platform jar on maven...")
      val jarFile: File = AetherUtil.resolveDeployUnit(deployUnit)
      logger.debug("after trying to get the platform jar on maven...")

      if (jarFile.exists) {
        platformJARPath = jarFile.getAbsolutePath
      }
      else {
        logger.error(jarFile.getAbsolutePath + " doesn't exist")
      }
    }
    platformJARPath
  }

  private def getVersion: String = {
    logger.debug("Kevoree version: {}", KevoreeFactory.getVersion)
    KevoreeFactory.getVersion
  }

}
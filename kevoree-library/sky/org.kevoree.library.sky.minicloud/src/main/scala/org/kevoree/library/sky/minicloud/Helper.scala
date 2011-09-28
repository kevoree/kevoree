package org.kevoree.library.sky.minicloud

import org.kevoree.framework.KevoreeXmiHelper
import java.io.File
import org.kevoree.tools.aether.framework.AetherUtil
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService

import scala.collection.JavaConversions._
import org.slf4j.{LoggerFactory, Logger}

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

  def setModelHandlerServvice (handler: KevoreeModelHandlerService) {
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
      val model = KevoreeFactory.eINSTANCE.createContainerRoot()

      // define repositories
      var repository = KevoreeFactory.eINSTANCE.createRepository()
      repository.setUrl("http://maven.kevoree.org/release")
      model.getRepositories.add(repository)
      repository = KevoreeFactory.eINSTANCE.createRepository()
      repository.setUrl("http://maven.kevoree.org/snapshots")
      model.getRepositories.add(repository)


      val deployUnit = KevoreeFactory.eINSTANCE.createDeployUnit()
      deployUnit.setGroupName("org.kevoree.platform")
      deployUnit.setUnitName("org.kevoree.platform.osgi.standalone")
      logger.debug("before trying to get the version platform jar...")
      deployUnit.setVersion(getVersion())
      model.getDeployUnits.add(deployUnit)
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

  private def getVersion (): String = {
    /*val stream: InputStream = this.getClass.getClassLoader
      .getResourceAsStream("META-INF/maven/org.kevoree.platform/org.kevoree.platform.agent/pom.properties")
    val prop: Properties = new Properties
    prop.load(stream)
    prop.getProperty("version")*/

    /* logger.debug("WTF0")
    modelHandlerService.getLastModel.getNodes.find(n => n.getName == nodeName) match {
      case None => "1.0.0"
      case Some(n) => n.getTypeDefinition.getDeployUnits.get(0).getVersion
    }*/
    "1.3.0-SNAPSHOT"
  }

}
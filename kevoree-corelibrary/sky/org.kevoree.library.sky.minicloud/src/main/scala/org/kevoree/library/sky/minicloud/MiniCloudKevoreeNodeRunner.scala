package org.kevoree.library.sky.minicloud

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
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
import util.matching.Regex
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.kevoree.{ContainerRoot, KevoreeFactory}
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.library.sky.api.{ProcessStreamFileLogger, KevoreeNodeRunner}
import java.util.concurrent.Callable

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class MiniCloudKevoreeNodeRunner (nodeName: String, iaasNode: AbstractHostNode) extends KevoreeNodeRunner(nodeName) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[MiniCloudKevoreeNodeRunner])
  private var nodePlatformProcess: Process = null

  val backupRegex = new Regex(".*<saveRes(.*)/>.*")
  val deployRegex = new Regex(".*<deployRes(.*)/>.*")
  val errorRegex = new Regex(".*Error while update.*")
  val starting = true

  case class DeployResult (uuid: String)

  case class BackupResult (uuid: String)

  case class ErrorResult ()

  def startNode (iaasModel: ContainerRoot, childBootstrapModel: ContainerRoot): Boolean = {
    try {
      logger.debug("Start " + nodeName)
      val version = findVersionForChildNode(nodeName, childBootstrapModel, iaasModel.getNodes.find(n => n.getName == iaasNode.getNodeName).get)

      if (version == KevoreeFactory.getVersion) {
        logger.debug("try to start child node with the same Kevoree version")
        // find the classpath of the current node
        // if classpath is null then we download the jar with aether
        // else we start the child node with the same classpath as its parent.
        // main class  = org.kevoree.platform.standalone.app.Main
        logger.debug(System.getProperty("java.class.path"))
        if (System.getProperty("java.class.path") != null) {
          logger.debug("trying to start child node with parent's classpath")
          if (!startWithClassPath(childBootstrapModel)) {
            logger.debug("Unable to start child node with parent's classpath, try to use aether bootstrap")
            startWithAether(childBootstrapModel, version)
          } else {
            true
          }
        } else {
          logger.debug("trying to start child node with aether bootstrap")
          startWithAether(childBootstrapModel, version)
        }
      } else {
        logger.debug("try to start child node with {} as Kevoree version", version)
        startWithAether(childBootstrapModel, version)
      }
    } catch {
      case e: IOException => {
        logger.error("Unexpected error while trying to start " + nodeName, e)
        false
      }
    }
  }

  def stopNode (): Boolean = {
    logger.debug("Kill " + nodeName)
    try {
      if (nodePlatformProcess != null) {
        nodePlatformProcess.destroy()
      }
      true
    }
    catch {
      case _@e => {
        logger.debug(nodeName + " cannot be killed. Try to force kill...")
        if (nodePlatformProcess != null) {
          nodePlatformProcess.destroy()
        }
        logger.debug(nodeName + " has been forcibly killed")
        true
      }
    }
  }

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }

  private def startWithClassPath (childBootStrapModel: ContainerRoot): Boolean = {
    try {
      val java: String = getJava
      val tempFile = File.createTempFile("bootModel" + nodeName, ".kev")
      KevoreeXmiHelper.save(tempFile.getAbsolutePath, childBootStrapModel)


      if (System.getProperty("java.class.path").contains("plexus-classworlds")) {
        return false //maven use case
      }

      //TRY THE CURRENT CLASSLOADER
      try {
        Thread.currentThread().getContextClassLoader.loadClass("org.kevoree.platform.standalone.App")
      } catch {
        case e: Throwable => {
          logger.info("Can't find bootstrap class {}", "org.kevoree.platform.standalone.App")
          return false
        }
      }

      val vmargsObject = iaasNode.getDictionary.get()
      var exec = Array[String](java)
      if (vmargsObject != null) {
        exec = Array[String](java, vmargsObject.toString)
      }
      exec = exec ++ List[String]("-Dnode.headless=true", "-Dnode.bootstrap=" + tempFile.getAbsolutePath, "-Dnode.name=" + nodeName, "-classpath", System.getProperty("java.class.path"),
                                   "org.kevoree.platform.standalone.App")
      logger.debug("Start node with command: {}", exec.mkString(" "))
      nodePlatformProcess = Runtime.getRuntime.exec(exec)

      configureLogFile()

      nodePlatformProcess.exitValue
      false
    } catch {
      case e: IllegalThreadStateException => {
        logger.debug("platform " + nodeName + " is started")
        true
      }
    }
  }

  private def startWithAether (childBootStrapModel: ContainerRoot, kevoreeVersion: String): Boolean = {
    try {
      val platformFile = iaasNode.getBootStrapperService.resolveKevoreeArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", kevoreeVersion)
      val java: String = getJava
      if (platformFile != null) {

        val tempFile = File.createTempFile("bootModel" + nodeName, ".kev")
        KevoreeXmiHelper.save(tempFile.getAbsolutePath, childBootStrapModel)

        val vmargsObject = iaasNode.getDictionary.get()
        var exec = Array[String](java)
        if (vmargsObject != null) {
          exec = Array[String](java, vmargsObject.toString)
        }
        exec = exec ++ List[String]("-Dnode.headless=true", "-Dnode.bootstrap=" + tempFile.getAbsolutePath, "-Dnode.name=" + nodeName, "-jar", platformFile.getAbsolutePath)


        logger.debug("Start node with command: {}", exec.mkString(" "))
        nodePlatformProcess = Runtime.getRuntime.exec(exec)

        configureLogFile()

        nodePlatformProcess.exitValue
        false
      } else {
        logger.error("Unable to start node because the platform jar file is not available")
        false
      }
    } catch {
      case e: IllegalThreadStateException => {
        logger.debug("platform " + nodeName + " is started")
        true
      }
    }
  }

  private def configureLogFile () {
    val logFile = System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log"
    outFile = new File(logFile + ".out")
    logger.debug("writing logs about {} on {}", nodeName, outFile.getAbsolutePath)
    new Thread(new ProcessStreamFileLogger(nodePlatformProcess.getInputStream, outFile)).start()
    errFile = new File(logFile + ".err")
    logger.debug("writing logs about {} on {}", nodeName, errFile.getAbsolutePath)
    new Thread(new ProcessStreamFileLogger(nodePlatformProcess.getErrorStream, errFile)).start()
  }
}


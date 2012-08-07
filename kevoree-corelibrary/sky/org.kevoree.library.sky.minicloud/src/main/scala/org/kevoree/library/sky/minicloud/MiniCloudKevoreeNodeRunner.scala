package org.kevoree.library.sky.minicloud

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
import util.matching.Regex
import org.kevoree.library.sky.api.nodeType.IaaSNode
import org.kevoree.{ContainerRoot, KevoreeFactory}
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.library.sky.api.{ProcessStreamFileLogger, KevoreeNodeRunner}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class MiniCloudKevoreeNodeRunner (nodeName: String, iaasNode: IaaSNode) extends KevoreeNodeRunner(nodeName) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[MiniCloudKevoreeNodeRunner])
  private var nodePlatformProcess: Process = null
//  private var outputStreamReader: Thread = null
//  private var errorStreamReader: Thread = null

  val backupRegex = new Regex(".*<saveRes(.*)/>.*")
  val deployRegex = new Regex(".*<deployRes(.*)/>.*")
  val errorRegex = new Regex(".*Error while update.*")

  case class DeployResult (uuid: String)

  case class BackupResult (uuid: String)

  case class ErrorResult ()

  //val actor = new UpdateManagementActor(10000)
  // actor.start()

  def startNode (iaasModel: ContainerRoot, jailBootStrapModel: ContainerRoot): Boolean = {
    try {
      logger.debug("Start " + nodeName)

      val platformFile = iaasNode.getBootStrapperService.resolveKevoreeArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", KevoreeFactory.getVersion)
      val java: String = getJava
      if (platformFile != null) {

        val tempFile = File.createTempFile("bootModel" + nodeName, ".kev")
        KevoreeXmiHelper.save(tempFile.getAbsolutePath, jailBootStrapModel)
        // FIXME java memory properties must define as Node properties
          // Currently the kloud provider only manages PJavaSeNode that hosts the software user configuration
          // It will be better to add a new node hosted by the PJavaSeNode
        nodePlatformProcess = Runtime.getRuntime.exec(Array[String](java, "-Dnode.bootstrap=" + tempFile.getAbsolutePath, "-Dnode.name=" + nodeName, "-jar", platformFile.getAbsolutePath))

        /*outputStreamReader = new Thread {
          outFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "sysout" + nodeName + ".log")
          val logStream: OutputStream = new FileOutputStream(outFile)
          logger.debug(outFile.getAbsolutePath + " is used as a log file")

          override def run () {
            try {
              val reader = new BufferedReader(new InputStreamReader(stream))
              var line = reader.readLine()
              while (line != null) {
                line = line + "\n"
                logStream.write(line.getBytes)
                logStream.flush()
                line = reader.readLine()
              }
            } catch {
              case _@e => {
                logger.debug("Stream has been closed, we close " + outFile.getAbsolutePath + " too")
              }
            } finally {
              logStream.flush()
              logStream.close()
            }
          }

          private val stream: InputStream = nodePlatformProcess.getInputStream
        }

        errorStreamReader = new Thread {
          errFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "syserr" + nodeName + ".log")
          val logStream: OutputStream = new FileOutputStream(errFile)
          logger.debug(errFile.getAbsolutePath + " is used as a log file")

          override def run () {
            try {
              val bytes: Array[Byte] = new Array[Byte](512)
              var length = 0;
              while (true) {
                length = stream.read(bytes)
                logStream.write(bytes, 0, length)

              }
            } catch {
              case _@e => {
                logger.debug("Stream has been closed, we close " + errFile.getAbsolutePath + " too")
              }
            } finally {
              logStream.flush()
              logStream.close()
            }
          }

          private val stream: InputStream = nodePlatformProcess.getErrorStream
        }
        outputStreamReader.start()
        errorStreamReader.start()*/
        val logFile = System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log"
        outFile = new File(logFile + ".out")
        logger.debug("writing logs about {} on {}", nodeName, outFile.getAbsolutePath)
        new Thread(new ProcessStreamFileLogger(nodePlatformProcess.getInputStream, outFile)).start()
        errFile = new File(logFile + ".err")
        logger.debug("writing logs about {} on {}", nodeName, errFile.getAbsolutePath)
        new Thread(new ProcessStreamFileLogger(nodePlatformProcess.getErrorStream, errFile)).start()
        nodePlatformProcess.exitValue
        false
      } else {
        logger.error("Unable to start node because the platform jar file is not available")
        false
      }
    } catch {
      case e: IOException => {
        //        e.printStackTrace()
        logger.error("Unexpected error while trying to start " + nodeName, e)
        false
      }
      case e: IllegalThreadStateException => {
        logger.debug("platform " + nodeName + " is started")
        true
      }
    }
  }

  def stopNode (): Boolean = {
    logger.debug("Kill " + nodeName)
    try {
      /*val watchdog = new KillWatchDog(nodePlatformProcess, 20000)
      nodePlatformProcess.getOutputStream.write("shutdown\n".getBytes)
      nodePlatformProcess.getOutputStream.flush()

      watchdog.start()
      nodePlatformProcess.waitFor()
      watchdog.stop()*/
      nodePlatformProcess.destroy()
      true
    }
    catch {
      case _@e => {
        logger.debug(nodeName + " cannot be killed. Try to force kill...")
        nodePlatformProcess.destroy()
        logger.debug(nodeName + " has been forcibly killed")
        true
      }
    }
  }

  /*
  def updateNode (model: String): Boolean = {
    val uuid = UUID.randomUUID()
    actor.manage(DeployResult(uuid.toString))
    nodePlatformProcess.getOutputStream.write(("sendModel " + model + " " + uuid.toString + "\n").getBytes)
    nodePlatformProcess.getOutputStream.flush()

    actor.waitFor()
  }*/

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }

}


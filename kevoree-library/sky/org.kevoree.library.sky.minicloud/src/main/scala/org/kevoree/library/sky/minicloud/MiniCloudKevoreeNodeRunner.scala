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
import actors.{TIMEOUT, Actor}
import util.matching.Regex
import java.util.UUID
import org.kevoree.library.sky.manager.{KevoreeNodeRunner, Helper}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class MiniCloudKevoreeNodeRunner (nodeName: String, bootStrapModel: String) extends KevoreeNodeRunner(nodeName, bootStrapModel) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[MiniCloudKevoreeNodeRunner])
  private var nodePlatformProcess: Process = null
  private var outputStreamReader: Thread = null
  private var errorStreamReader: Thread = null

  val backupRegex = new Regex(".*<saveRes(.*)/>.*")
  val deployRegex = new Regex(".*<deployRes(.*)/>.*")
  val errorRegex = new Regex(".*Error while update.*")

  sealed abstract case class Result ()

  case class DeployResult (uuid: String) extends Result

  case class BackupResult (uuid: String) extends Result

  case class ErrorResult () extends Result

  val actor = new UpdateManagementActor(10000)
  actor.start()

  def startNode (): Boolean = {
    try {
      logger.debug("Start " + nodeName)
      val java: String = getJava

      if (Helper.getJarPath != null) {

        logger.debug("use bootstrap model path => " + bootStrapModel)

        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
        var debug = "ERROR"
        if (root.isWarnEnabled) {
          debug = "WARN"
        }
        if (root.isInfoEnabled) {
          debug = "INFO"
        }
        if (root.isDebugEnabled)  {
          debug = "DEBUG"
        }
        logger.debug("child node log level will be set to {}", debug)

        nodePlatformProcess = Runtime.getRuntime
          .exec(Array[String](java, "-Dnode.bootstrap=" + bootStrapModel, "-Dnode.name=" + nodeName,
                               "-Dnode.log.level=" + debug, "-jar", Helper.getJarPath))

        outputStreamReader = new Thread {
          outFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "sysout" + nodeName + ".log")
          val logStream: OutputStream = new FileOutputStream(outFile)
          logger.debug(outFile.getAbsolutePath + " is used as a log file")

          override def run () {
            try {
              val reader = new BufferedReader(new InputStreamReader(stream))
              var line = reader.readLine()
              while (line != null) {
                line match {
                  case deployRegex(uuid) => {
                    actor ! DeployResult(uuid)
                  }
                  case backupRegex(uuid) => {
                    actor ! BackupResult(uuid)
                  }
                  case errorRegex(uuid) => actor ! ErrorResult()
                  case _ =>
                }
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
        errorStreamReader.start()
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
      actor.stop()
      val watchdog = new KillWatchDog(nodePlatformProcess, 20000)
      nodePlatformProcess.getOutputStream.write("shutdown\n".getBytes)
      nodePlatformProcess.getOutputStream.flush()
      nodePlatformProcess.getOutputStream.close()
      watchdog.start()
      nodePlatformProcess.waitFor()
      watchdog.stop()
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

  def updateNode (model: String): Boolean = {
    val uuid = UUID.randomUUID()
    actor.manage(DeployResult(uuid.toString))
    nodePlatformProcess.getOutputStream.write(("sendModel " + model + " " + uuid.toString + "\n").getBytes)
    nodePlatformProcess.getOutputStream.flush()

    actor.waitFor()
  }

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }

  class UpdateManagementActor (timeout: Int) extends Actor {

    case class STOP ()

    case class WAITINFOR ()

    def stop () {
      this ! STOP()
    }

    def manage (res: Result) {
      this !? res
    }

    def waitFor (): Boolean = {
      (this !? WAITINFOR()).asInstanceOf[Option[Boolean]].get
    }

    var firstSender = null

    def act () {
      loop {
        react {
          case STOP() => this.exit()
          case ErrorResult() =>
          case DeployResult(uuid) => {
            var firstSender = this.sender
            reply()
            react {
              case STOP() => this.exit()
              case WAITINFOR() => {
                firstSender = this.sender
                reactWithin(timeout) {
                  case STOP() => this.exit()
                  case DeployResult(uuid2) if (uuid == uuid2) => {
                    firstSender ! Some(true)
                  }
                  case TIMEOUT => firstSender ! Some(false)
                  case ErrorResult() => {
                    firstSender ! Some(false)
                  }
                }
              }
              case DeployResult(uuid2) if (uuid == uuid2) => {
                react {
                  case STOP() => this.exit()
                  case WAITINFOR() => sender ! Some(true)
                }
              }
              case ErrorResult() => {
                react {
                  case STOP() => this.exit()
                  case WAITINFOR() => sender ! Some(false)
                }
              }
            }
          }
        }
      }
    }
  }

}


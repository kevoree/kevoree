package org.kevoree.library.sky.virtualCloud

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

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class KevoreeNodeRunner (var nodeName: String, bootStrapModel: String) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[KevoreeNodeRunner])
  private var nodePlatformProcess: Process = null
  private var outputStreamReader: Thread = null
  private var errorStreamReader: Thread = null
  private val platformClass: String = "org.kevoree.platform.osgi.standalone.App"

  private val LogFileErr: File = null
  private val LogFileout: File = null

  def startNode (): Boolean = {
    try {
      logger.debug("Start " + nodeName)
      val java: String = getJava

      val classPath = System.getProperty("java.class.path")

      nodePlatformProcess = Runtime.getRuntime
        .exec(Array[String](java, "-Dnode.bootstrap=" + bootStrapModel, "-Dnode.name=" + nodeName, "-cp", classPath, platformClass))
      outputStreamReader = new Thread {
        val file = new File(System.getProperty("java.io.tmpdir") + File.separator + "sysout" + nodeName + ".log")

        override def run () {
          try {
            val logStream : OutputStream = new FileOutputStream(file)
            val bytes: Array[Byte] = new Array[Byte](512)
            var length = 0;
            while (true) {
              length = stream.read(bytes)
              logStream.write(bytes, 0, length)

            }
          }
          catch {
            case e: IOException => {
            }
          }
        }

        private val stream: InputStream = nodePlatformProcess.getInputStream
      }
      errorStreamReader = new Thread {
        val file = new File(System.getProperty("java.io.tmpdir") + File.separator + "syserr" + nodeName + ".log")
        override def run () {
          try {
            val logStream : OutputStream = new FileOutputStream(file)
            val bytes: Array[Byte] = new Array[Byte](512)
            var length = 0;
            while (true) {
              length = stream.read(bytes)
              logStream.write(bytes, 0, length)

            }
          }
          catch {
            case e: IOException => {
            }
          }
        }

        private val stream: InputStream = nodePlatformProcess.getErrorStream
      }
      outputStreamReader.start()
      errorStreamReader.start()
      nodePlatformProcess.exitValue
      true
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

  def stopKillNode (): Boolean = {
    logger.debug("Kill " + nodeName)
    try {
      nodePlatformProcess.getOutputStream.write("stop 0\n".getBytes)
      nodePlatformProcess.getOutputStream.flush()
      Thread.sleep(10000)
      nodePlatformProcess.exitValue
      true
    }
    catch {
      case e: IOException => {
        logger.debug(nodeName + " cannot be killed. Try to force kill...")
        nodePlatformProcess.destroy()
        logger.debug(nodeName + " has been forcibly killed")
        true
      }
      case e: InterruptedException => {
        logger.debug(nodeName + " cannot be killed. Try to force kill...")
        nodePlatformProcess.destroy()
        logger.debug(nodeName + " has been forcibly killed")
        true
      }
      case e: IllegalThreadStateException => {
        logger.debug(nodeName + " cannot be killed. Try to force kill...")
        nodePlatformProcess.destroy()
        logger.debug(nodeName + " has been forcibly killed")
        true
      }
    }
  }

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }
}


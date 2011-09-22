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
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class KevoreeNodeRunner (var nodeName: String, basePort: Int, bootStrapModel: String) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[KevoreeNodeRunner])
  private var nodePlatformProcess: Process = null
  private var outputStreamReader: Thread = null
  private var errorStreamReader: Thread = null
  private val platformClass: String = "org.kevoree.platform.osgi.standalone.App"

  def startNode (): Boolean = {
    try {
      logger.debug("Start " + nodeName)
      val java: String = getJava

      val classPath = System.getProperty("java.class.path")

      nodePlatformProcess = Runtime.getRuntime
        .exec(Array[String](java, "-Dnode.bootstrap=" + bootStrapModel, "-Dnode.name=" + nodeName,
                             "-Dnode.port=" + basePort, "-cp", classPath, platformClass))
      outputStreamReader = new Thread {
        override def run () {
          try {
            val bytes: Array[Byte] = new Array[Byte](512)
            while (true) {
              stream.read(bytes)
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
        override def run() {
          try {
            val bytes: Array[Byte] = new Array[Byte](512)
            while (true) {
              stream.read(bytes)
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
        logger.debug("platform " + nodeName + ":" + basePort + " is started")
        false
      }
    }
  }

  def stopKillNode () : Boolean = {
    logger.debug("Kill " + nodeName)
    try {
      nodePlatformProcess.getOutputStream.write("stop 0".getBytes)
      nodePlatformProcess.getOutputStream.flush()
      Thread.sleep(1000)
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


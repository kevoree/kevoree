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
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Properties

class KevoreeNodeRunner {
  private val logger: Logger = LoggerFactory.getLogger(classOf[KevoreeNodeRunner])
  private var nodePlatformProcess: Process = null
  private var outputStreamReader: Thread = null
  private var errorStreamReader: Thread = null
  private var nodeName: String = null
  private var basePort: Int = 7000
  private var bootStrapModel: String = null
  private var platformJARPath: String = null


  def this (nodeName: String, basePort: Int, bootStrapModel: String) {
    this ()
    this.nodeName = nodeName
    this.basePort = basePort
    this.bootStrapModel = bootStrapModel
  }

  def startNode() {
    try {
      System.out.println("StartNodeCommand")
      if (platformJARPath == null) {
        findJar()
      }
      val java: String = getJava
      nodePlatformProcess = Runtime.getRuntime
        .exec(Array[String](java, "-Dnode.bootstrap=" + bootStrapModel, "-Dnode.name=" + nodeName,
                             "-Dnode.port=" + basePort, "-jar", platformJARPath))
      outputStreamReader = new Thread {
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

        private val stream: InputStream = nodePlatformProcess.getInputStream
      }
      errorStreamReader = new Thread {
        override def run: Unit = {
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
      System.out.println(nodePlatformProcess.exitValue)
    }
    catch {
      case e: IOException => {
        e.printStackTrace()
      }
      case e: IllegalThreadStateException => {
        System.out.println("platform " + nodeName + ":" + basePort + " is running")
      }
    }
  }

  def stopKillNode() {
    System.out.println("KillNodeCommand")
    try {
      nodePlatformProcess.getOutputStream.write("stop 0".getBytes)
      nodePlatformProcess.getOutputStream.flush()
      Thread.sleep(1000)
      nodePlatformProcess.exitValue
    }
    catch {
      case e: IOException => {
        logger.error("The node cannot be killed. Try to force kill")
        nodePlatformProcess.destroy()
      }
      case e: InterruptedException => {
        logger.error("The node cannot be killed. Try to force kill")
        nodePlatformProcess.destroy()
      }
      case e: IllegalThreadStateException => {
        logger.error("The node cannot be killed. Try to force kill")
        nodePlatformProcess.destroy()
      }
    }
  }

  private def findJar() {
    System.out.println("Init jar platform")
    var jarLocation: String = System.getProperty("kevoree.location")
    if (jarLocation == null) {
      jarLocation = System.getProperty("user.dir") + File.separatorChar + "org.kevoree.platform.osgi.standalone" + "-" +
        getVersion + ".jar"
    }
    if (new File(jarLocation).exists) {
      platformJARPath = jarLocation
    }
    else {
      throw new FileNotFoundException(jarLocation + " doesn't exist")
    }
    System.out.println("Init jar platform: " + platformJARPath + " => OK")
  }

  private def getVersion: String = {
    val stream: InputStream = this.getClass.getClassLoader
      .getResourceAsStream("META-INF/maven/org.kevoree.platform/org.kevoree.platform.agent/pom.properties")
    val prop: Properties = new Properties
    prop.load(stream)
    prop.getProperty("version")
  }

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }
}


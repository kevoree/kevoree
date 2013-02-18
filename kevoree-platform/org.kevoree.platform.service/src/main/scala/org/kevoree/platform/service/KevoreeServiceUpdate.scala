/*

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
package org.kevoree.platform.service

import java.util.jar.{JarEntry, JarFile}
import java.io._
import java.net.URL
import java.util
import org.kevoree.framework.KevoreeXmiHelper

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/12/11
 * Time: 14:35
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevoreeServiceUpdate extends App {

  val defaultLocation = args(0)

  var version = "LATEST"
  var getFromModel = false
  var model = ""
  args.filter(arg => arg.startsWith("version=")).foreach {
    arg => version = arg.substring("version=".size, arg.size)
  }

  args.filter(arg => arg.startsWith("model=")).foreach {
    arg => getFromModel = true; model = arg.substring("model=".size, arg.size)
  }

  var repos = new util.ArrayList[String]()


  if (getFromModel) {
    var jar: JarFile = null
    var entry: JarEntry = null
    try {
      // look if its a file, a http url or a mvn url
      if (model.startsWith("mvn:")) {
        val splittedUrl = model.substring("mvn:".length).split("/")
        if (splittedUrl(2) == "RELEASE") {
          //repos = List[String]("http://maven.kevoree.org/release/")
          repos.add("http://maven.kevoree.org/release/")
          splittedUrl(2) = "LATEST"
        } else {
          repos.add("http://maven.kevoree.org/snapshots/")
          repos.add("http://maven.kevoree.org/release/")
        }

        val modelJarFile = AetherResolver.resolve(splittedUrl(1), splittedUrl(0), splittedUrl(2), repos)
        val jar: JarFile = new JarFile(modelJarFile)
        val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
        if (entry != null) {
          version = findVersionFromModel(jar.getInputStream(entry))
        }
      } else if (model.startsWith("http:")) {
        val url = new URL(model)
        val in = new DataInputStream(url.openStream())
        val file = File.createTempFile("modelFile", "")
        val out = new FileOutputStream(file)

        val bytes = new Array[Byte](2048)
        var length = in.read(bytes)
        while (length != -1) {
          out.write(bytes, 0, length)
          length = in.read(bytes)
        }
        in.close()
        out.flush()
        out.close()
        try {
          jar = new JarFile(file)
          entry = jar.getJarEntry("KEV-INF/lib.kev")
          version = findVersionFromModel(jar.getInputStream(entry))
        } catch {
          case _@e => {
            // the file is not a JAR but a Kevoree model or a Kevoree script
            version = findVersionFromModel(new FileInputStream(file))
          }
        }

      } else {
        try {
          jar = new JarFile(new File(model))
          entry = jar.getJarEntry("KEV-INF/lib.kev")
          version = findVersionFromModel(jar.getInputStream(entry))
        } catch {
          case _@e => {
            // the file is not a JAR but a Kevoree model or a Kevoree script
            version = findVersionFromModel(new FileInputStream(new File(model)))
          }
        }
      }
    } catch {
      case _@e => e.printStackTrace(); println("Unable to get model")
    }

    val jarFile: File = AetherResolver.resolve("org.kevoree.platform.standalone", "org.kevoree.platform", version, repos)

    if (jarFile.exists) {
      val p = Runtime.getRuntime.exec(Array[String]("cp", jarFile.getAbsolutePath, defaultLocation))
      p.waitFor()
    }
  }

  private def findVersionFromModel(stream: InputStream): String = {
    val model = KevoreeXmiHelper.$instance.loadStream(stream)

    import scala.collection.JavaConversions._
    model.getDeployUnits.find(dp => dp.getGroupName == "org.kevoree" && dp.getUnitName == "org.kevoree.framework") match {
      case None => println("LATEST"); "LATEST"
      case Some(deployUnit) => println(deployUnit.getVersion); deployUnit.getVersion
    }
  }
}*/

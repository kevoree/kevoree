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
import java.util.Random
import java.io._
import util.matching.Regex
import java.net.URL
import java.util

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
  repos.add("http://maven.kevoree.org/snapshots/")
  repos.add("http://maven.kevoree.org/release/")


  if (getFromModel) {
    try {
      // look if its a file, a http url or a mvn url
      if (model.startsWith("mvn:")) {
        val splittedUrl = model.substring("mvn:".length).split("/")
        if (splittedUrl(2) == "RELEASE") {
          //repos = List[String]("http://maven.kevoree.org/release/")
          splittedUrl(2) = "LATEST"
        }

        val modelJarFile = AetherResolver.resolve(splittedUrl(1), splittedUrl(0), splittedUrl(2), repos)
        val jar: JarFile = new JarFile(modelJarFile)
        val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
        if (entry != null) {
          model = convertStreamToString(jar.getInputStream(entry))
          version = findVersionFromModel(model)
        }
      } else if (model.startsWith("http:")) {
        val url = new URL(model)
        model = convertStreamToString(url.openConnection().getInputStream)
        version = findVersionFromModel(model)
      } else {
         model = convertStreamToString(new FileInputStream(model))
        version = findVersionFromModel(model)
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

  private def convertStreamToString (inputStream: InputStream): String = {
    val rand: Random = new Random
    val temp: File = File.createTempFile("kevoreeloaderLib" + rand.nextInt, ".xmi")
    temp.deleteOnExit()
    val out = new ByteArrayOutputStream()
    var read: Int = 0
    val bytes: Array[Byte] = new Array[Byte](1024)
    while ((({
      read = inputStream.read(bytes);
      read
    })) != -1) {
      out.write(bytes, 0, read)
    }
    inputStream.close()
    new String(out.toByteArray, "UTF-8")
  }

  private def findVersionFromModel (path: String): String = {
    //<deployUnits type="jar" unitName="org.kevoree.framework" xsi:type="kevoree:DeployUnit" groupName="org.kevoree" version="1.7.1-SNAPSHOT" targetNodeType="//@typeDefinitions.17"></deployUnits>
    val frameworkRegex = new
        //Regex("<deployUnits targetNodeType=\"//@typeDefinitions.[0-9][0-9]*\" type=\".*\" version=\"(.*)\" unitName=\"org.kevoree.framework\" groupName=\"org.kevoree\" xsi:type=\"kevoree:DeployUnit\"></deployUnits>")
        Regex("<deployUnits\\s(?:(?:(?:version=\"([^\\s]*)\")|(?:targetNodeType=\"//@typeDefinitions.[0-9][0-9]*\")|(?:type=\"[^\\s]*\")|(?:unitName=\"org.kevoree.framework\")|(?:groupName=\"org.kevoree\")|(?:xsi:type=\"kevoree:DeployUnit\"))(\\s)*){6}></deployUnits>")
    var frameworkVersion = "LATEST"
    path.lines.forall(l => {
      val m = frameworkRegex.pattern.matcher(l)
      if (m.find()) {
        frameworkVersion = m.group(1)
        false
      } else {
        true
      }
    })
    println(frameworkVersion)
    frameworkVersion
  }
}
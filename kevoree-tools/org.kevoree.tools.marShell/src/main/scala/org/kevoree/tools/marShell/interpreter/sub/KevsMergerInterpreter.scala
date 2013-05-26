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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.ast.MergeStatement
import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.framework.KevoreeXmiHelper
import java.util.jar.{JarEntry, JarFile}
import java.io.File
import java.net.URL
import java.util
import scala.collection.JavaConversions._
import org.kevoree.log.Log

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/12/11
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */

case class KevsMergerInterpreter (mergeStatement: MergeStatement) extends KevsAbstractInterpreter {

  private val mergerComponent = new KevoreeMergerComponent()

  def interpret (context: KevsInterpreterContext): Boolean = {

    if (mergeStatement.url.startsWith("mvn:")) {
      val mavenurl = mergeStatement.url.substring(4)
      var file: File = null
      if (file == null && mavenurl.startsWith("http://")) {
        val repourl = mavenurl.substring(0, mavenurl.indexOf("!"))
        val urlids = mavenurl.substring(mavenurl.indexOf("!") + 1)
        val part = urlids.split("/")
        if (part.size == 3) {
          val repos = new util.ArrayList[String]()
          repos.add(repourl)

          context.model.getRepositories.foreach { repo =>
             repos.add(repo.getUrl)
          }

          file = context.getBootstraper.resolveArtifact(part(1), part(0), part(2), repos)
        } else {
          Log.warn("Kevscript merger : Bad MVN URL <mvn:[repourl!]groupID/artefactID/version>")
        }
      }
      if (file == null) {
        val part = mavenurl.split("/")
        if (part.size == 3) {
          val repos = new util.ArrayList[String]()
          context.model.getRepositories.foreach { repo =>
            repos.add(repo.getUrl)
          }
          repos.add("http://maven.kevoree.org/release")
          repos.add("http://maven.kevoree.org/snapshots")
          file = context.getBootstraper.resolveArtifact(part(1), part(0), part(2),repos)

          println("Resolved File = "+file+"-"+part(0)+"-"+part(1)+"-"+part(2))
        } else {
          Log.warn("Kevscript merger : Bad MVN URL <mvn:[repourl!]groupID/artefactID/version>")
        }
      }
      if (file != null) {
        var jar: JarFile = null
        jar = new JarFile(new File(file.getAbsolutePath))
        val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
        if (entry != null) {
          val newModel = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry))
          mergerComponent.merge(context.model, newModel)
          true
        } else {
          try {
            val newModel = KevoreeXmiHelper.instance$.load(mergeStatement.url)
            mergerComponent.merge(context.model, newModel)
            true
          } catch {
            case _@e => Log.warn("Unable to load library from {}. Maybe it's not a Kevoree model nor a Kevoree DeployUnit", mergeStatement.url); true
          }
        }
      } else {
        false
      }
    } else if (mergeStatement.url.startsWith("http://")) {
      val newModel = KevoreeXmiHelper.instance$.loadStream(new URL(mergeStatement.url).openStream)
      mergerComponent.merge(context.model, newModel)
      true
    } else {
      try {
        val newModel = KevoreeXmiHelper.instance$.load(mergeStatement.url)
        mergerComponent.merge(context.model, newModel)
        true
      } catch {
        case _@e => {
          Log.warn("KevScript error while merging from url " + mergeStatement.url)
          false
        }
      }

    }

  }

}
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
package org.kevoree.platform.mavenrunner

import java.io.{BufferedReader, FileReader, File}
import org.kevoree.tools.marShell.KevScriptOfflineEngine
import org.kevoree.{ContainerRoot, KevoreeFactory}
import org.apache.maven.project.MavenProject
import org.kevoree.tools.modelsync.FakeBootstraperService

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/02/12
 * Time: 17:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevScriptHelper extends App {

  def generate (scriptFile: File, mavenSource: MavenProject): ContainerRoot = {
    val kevEngine = new KevScriptOfflineEngine(KevoreeFactory.createContainerRoot, new FakeBootstraperService().getBootstrap)
    kevEngine.addVariable("kevoree.version", KevoreeFactory.getVersion)

    val propEnum = mavenSource.getProperties.propertyNames()
    val propName = "project.version"
    val propVal = mavenSource.getVersion
    kevEngine.addVariable(propName.toString, propVal.toString)
    while (propEnum.hasMoreElements) {
      val propName = propEnum.nextElement()
      val propVal = mavenSource.getProperties.get(propName)
      kevEngine.addVariable(propName.toString, propVal.toString)
    }
    kevEngine.addVariable("basedir", mavenSource.getBasedir.getAbsolutePath)
    kevEngine.append(loadScript(scriptFile))
    kevEngine.interpret()
  }


  private def loadScript (file: File): String = {
    val fileReader = new BufferedReader(new FileReader(file))
    val scriptBuilder = new StringBuilder
    var line = fileReader.readLine()

    while (line != null) {
      scriptBuilder append line + "\n"
      line = fileReader.readLine()
    }

    fileReader.close()
    scriptBuilder.toString()
  }
}
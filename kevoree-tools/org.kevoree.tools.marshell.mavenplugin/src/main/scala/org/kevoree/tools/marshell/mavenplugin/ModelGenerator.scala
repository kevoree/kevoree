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
package org.kevoree.tools.marshell.mavenplugin

import java.io.{BufferedReader, FileReader, File}
import org.kevoree.tools.marShell.KevScriptOfflineEngine
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.{ContainerRoot, KevoreeFactory}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/02/12
 * Time: 17:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object ModelGenerator extends App {

  def generate (scriptPath: String) : ContainerRoot = {
    val kevEngine = new KevScriptOfflineEngine(KevoreeFactory.createContainerRoot)
    kevEngine.addVariable("kevoree.version", KevoreeFactory.getVersion)
    kevEngine.append(loadScript(new File(scriptPath)))
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
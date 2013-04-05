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
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.{ModelHelper, ArduinoModelGetHelper, KevoreeUIKernel}
import org.kevoree.tools.aether.framework.AetherUtil
import java.util.jar.{JarEntry, JarFile}
import org.kevoree.framework.KevoreeXmiHelper
import org.slf4j.LoggerFactory
import org.kevoree.KevoreeFactory
import org.kevoree.merger.RootMerger
import org.kevoree.extra.kserial.Utils.KHelpers
import org.kevoree.extra.kserial.KevoreeSharedCom
import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/06/12
 * Time: 14:39
 */

class OpenArduinoNode extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  val loadModelCMD = new LoadModelCommand()
  val clearCMD = new ClearModelCommand()

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
    clearCMD.setKernel(k)
    loadModelCMD.setKernel(k)
  }

  def execute(p: Any) {

    val du  = ModelHelper.kevoreeFactory.createDeployUnit
    du.setUnitName("org.kevoree.library.model.arduino")
    du.setGroupName("org.kevoree.corelibrary.model")
    du.setVersion(ModelHelper.kevoreeFactory.getVersion)
    val file = AetherUtil.instance$.resolveDeployUnit(du)

    file match {
      case file : File => {
        val jar = new JarFile(file)
        val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
        val newmodel = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry))
        if (newmodel != null) {
          val merger = new RootMerger
          import scala.collection.JavaConversions._
          KHelpers.getPortIdentifiers.foreach{ pi =>
            try {
              merger.merge(newmodel,ArduinoModelGetHelper.getCurrentModel(newmodel,pi))
            } catch {
              case _ @ e=>
            }
          }
          KevoreeSharedCom.killAll()
          clearCMD.execute(null)
          loadModelCMD.execute(newmodel)
        }
      }
      case _ => logger.warn("Could not get the deploy unit from Aether.")
    }

  }
}

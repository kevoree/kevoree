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

import org.kevoree.framework.kaspects.ContainerNodeAspect
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.aether.framework.AetherUtil
import java.util.jar.{JarEntry, JarFile}
import org.kevoree.framework.KevoreeXmiHelper
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/03/12
 * Time: 14:55
 */

class AutoUpdateCommand extends Command {

  var kernel: KevoreeUIKernel = null
  val containerNodeAspect = new ContainerNodeAspect()

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: AnyRef) {

    val currentModel = kernel.getModelHandler.getActualModel
    currentModel.getNodes.foreach {
      node =>
        containerNodeAspect.getUsedTypeDefinition(node).foreach {
          typeDef =>
            typeDef.getDeployUnits.foreach {
              du => {
                try {
                  val file = AetherUtil.$instance.resolveDeployUnit(du)
                  val jar = new JarFile(file)
                  val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
                  val newmodel = KevoreeXmiHelper.$instance.loadStream(jar.getInputStream(entry))
                  if (newmodel != null) {
                    kernel.getModelHandler.merge(newmodel);
                    LoggerFactory.getLogger(this.getClass).info("AutoMerge => " + du.getUnitName)
                  }
                } catch {
                  case _@e =>
                }
              }

            }
        }


    }


  }

}

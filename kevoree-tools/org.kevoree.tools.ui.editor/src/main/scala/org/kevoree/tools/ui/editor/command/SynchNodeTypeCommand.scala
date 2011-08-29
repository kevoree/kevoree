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

import reflect.BeanProperty
import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor._
import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper
import java.lang.Thread

class SynchNodeTypeCommand extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  @BeanProperty
  var destNodeName: String = null

  val bootstrap = new NodeTypeBootstrapHelper

  def execute(p: AnyRef) {

    try {
      PositionedEMFHelper.updateModelUIMetaData(kernel);
      val model: ContainerRoot = kernel.getModelHandler.getActualModel
      new Thread() {
        override def run() {
          bootstrap.bootstrapNodeType(model, destNodeName, EmbeddedOSGiEnv.getFwk.getBundleContext) match {
            case Some(nodeTypeInstance) => {
              nodeTypeInstance.push(destNodeName, model, bootstrap.getNodeTypeBundle.getBundleContext)
            }
            case None => logger.error("Error while bootstraping node type")
          }
        }
      }.start()
    } catch {
      case _@e => logger.error("", e)
    }
  }

}
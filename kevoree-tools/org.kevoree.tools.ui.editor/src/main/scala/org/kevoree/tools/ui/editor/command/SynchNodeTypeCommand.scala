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
import java.lang.Thread
import org.kevoree.tools.aether.framework.{NodeTypeBootstrapHelper}
import javax.swing.{JProgressBar, JLabel}

class SynchNodeTypeCommand extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  @BeanProperty
  var destNodeName: String = null

  @BeanProperty
  var viaGroupName: String = null

  val bootstrap = new NodeTypeBootstrapHelper

  @BeanProperty
  var resultLabel : JLabel = null

  @BeanProperty
  var progressBar : JProgressBar = null

  def execute(p: AnyRef) {
    try {
      bootstrap.clear
      PositionedEMFHelper.updateModelUIMetaData(kernel);
      resultLabel.setText("Update model...")
      val autoUpdate = new AutoUpdateCommand
      autoUpdate.setKernel(kernel)
      autoUpdate.execute(null)

      val model: ContainerRoot = kernel.getModelHandler.getActualModel

      new Thread() {
        override def run() {
          bootstrap.bootstrapGroupType(model, viaGroupName, new ModelHandlerServiceWrapper(kernel)) match {
            case Some(groupTypeInstance) => {
              groupTypeInstance.push(model,destNodeName)
              progressBar.setIndeterminate(false);
              progressBar.setEnabled(false);
              resultLabel.setText("Pushed ;-)")
            }

            case None => {
              logger.error("Error while bootstraping group type")
              progressBar.setIndeterminate(false);
              progressBar.setEnabled(false);
              resultLabel.setText("Error boot Group, bad merge ?")
            }
          }
        }
      }.start()
    } catch {
      case _@e => {
        logger.error("", e)
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        resultLabel.setText(e.getMessage)
      }
    }
  }

}
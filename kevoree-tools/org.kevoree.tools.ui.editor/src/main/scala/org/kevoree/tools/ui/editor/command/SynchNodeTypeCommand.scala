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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.command

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor._
import java.lang.Thread
import org.kevoree.tools.aether.framework.{NodeTypeBootstrapHelper}
import javax.swing.{JProgressBar, JLabel}
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.tools.modelsync.ModelSyncBean
import org.kevoree.framework.KevoreeXmiHelper

class SynchNodeTypeCommand (isPush: Boolean) extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  @BeanProperty
  var autoMerge: Boolean = true

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  @BeanProperty
  var destNodeName: String = null

  @BeanProperty
  var viaGroupName: String = null

  val modelSyncBean = new ModelSyncBean

  @BeanProperty
  var resultLabel: JLabel = null

  @BeanProperty
  var progressBar: JProgressBar = null

  var threadCommand: Thread = null

  def execute (p: AnyRef) {

    threadCommand = new Thread() {
      override def run () {

        if (isPush) {
          try {
            modelSyncBean.clear()
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            if (autoMerge) {
              resultLabel.setText("Update model...")
              val autoUpdate = new AutoUpdateCommand
              autoUpdate.setKernel(kernel)
              autoUpdate.execute(null)
            }
            modelSyncBean.pushTo(kernel.getModelHandler.getActualModel, destNodeName, viaGroupName)
            resultLabel.setText("Pushed ;-)")
          } catch {
            case _@e => {
              logger.error("", e)
              resultLabel.setText(e.getMessage)
            }
          } finally {
            progressBar.setIndeterminate(false)
            progressBar.setEnabled(false)
          }
        } else {
          try {
            modelSyncBean.clear()
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            if (autoMerge) {
              resultLabel.setText("Asking model...")
              val autoUpdate = new AutoUpdateCommand
              autoUpdate.setKernel(kernel)
              autoUpdate.execute(null)
            }
            val model = modelSyncBean.pullTo(kernel.getModelHandler.getActualModel, destNodeName, viaGroupName)
            val lcommand = new LoadModelCommand()
            try {
              kernel.getModelHandler.merge(model)
              PositionedEMFHelper.updateModelUIMetaData(kernel)
              lcommand.setKernel(kernel)
              lcommand.execute(kernel.getModelHandler.getActualModel)
              resultLabel.setText("Received ;-)")
              true
            } catch {
              case _@e => logger.debug("Unable to load model", e)
              resultLabel.setText("Unable to receive or use model ;-(")
            }
          } catch {
            case _@e => {
              logger.error("", e)
              resultLabel.setText(e.getMessage)
            }
          } finally {
            progressBar.setIndeterminate(false)
            progressBar.setEnabled(false)
          }
        }
      }
    }
    threadCommand.start()
  }
}
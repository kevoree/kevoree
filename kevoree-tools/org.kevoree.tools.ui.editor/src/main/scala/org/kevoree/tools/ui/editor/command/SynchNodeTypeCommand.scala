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

import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor._
import javax.swing.{SwingUtilities, JProgressBar, JLabel}
import org.kevoree.tools.ui.editor.ws.WebSocketClient
import java.util

class SynchNodeTypeCommand(isPush: Boolean) extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  var autoMerge: Boolean = true

  var kernel: KevoreeUIKernel = null

  var destNodeName: String = null

  var viaGroupName: String = null

  var resultLabel: JLabel = null

  var progressBar: JProgressBar = null

  var threadCommand: Thread = null

  def execute(p: AnyRef) {

    threadCommand = new Thread() {
      override def run() {

        if (isPush) {
          try {
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            if (autoMerge) {
              resultLabel.setText("Update model...")
              resultLabel.setToolTipText("Update model...")
              val autoUpdate = new AutoUpdateCommand
              autoUpdate.setKernel(kernel)
              autoUpdate.execute(null)
            }

            val model = kernel.getModelHandler.getActualModel
            val group = model.findGroupsByID(viaGroupName)

            var ip = "127.0.0.1";
            var port = "9000";

            if (group != null) {
              if (group.getDictionary != null) {
                val tempPort = group.getDictionary.findValuesByID("port")
                if (tempPort != null) {
                  port = tempPort.getValue
                }
              }
              val fragDep = group.findFragmentDictionaryByID(destNodeName)
              if (fragDep != null) {
                val tempPort = fragDep.findValuesByID("port")
                if (tempPort != null) {
                  port = tempPort.getValue
                }
              }

            }
            //Lookup first IP i can ping ...
            val containerNode = model.findNodesByID(destNodeName)
            val ipInfo = containerNode.findNetworkInformationByID("ip")
            val ips = new util.ArrayList[String]()
            if (ipInfo != null) {
              import scala.collection.JavaConversions._
              ipInfo.getValues.foreach {
                v =>
                  ips.add(v.getValue)
              }
            }
            if (!ips.isEmpty) {
              //TODO
              import scala.collection.JavaConversions._
              ips.foreach {
                lip =>
                  if (!lip.contains(":")) {
                    ip = lip;
                  }
              }
            }

            println("IP taken for push : " + ip)

            WebSocketClient.push(ip, port, kernel.getModelHandler.getActualModel);

            //modelSyncBean.pushTo(kernel.getModelHandler.getActualModel, destNodeName, viaGroupName)
            resultLabel.setText("Pushed ;-)")
            resultLabel.setToolTipText("Pushed ;-)")
          } catch {
            case _@e => {
              logger.error("", e)
              resultLabel.setText(e.getMessage)
              resultLabel.setToolTipText(e.getMessage)
            }
          } finally {
            SwingUtilities.invokeLater(new Runnable {
              override def run(): Unit = {
                progressBar.setIndeterminate(false)
                progressBar.setEnabled(false)
              }
            })

          }
        } else {
          try {
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            if (autoMerge) {
              resultLabel.setText("Asking model...")
              resultLabel.setToolTipText("Asking model...")
              val autoUpdate = new AutoUpdateCommand
              autoUpdate.setKernel(kernel)
              autoUpdate.execute(null)
            }
            val lcommand = new LoadModelCommand()
            try {
              PositionedEMFHelper.updateModelUIMetaData(kernel)
              lcommand.setKernel(kernel)
              lcommand.execute(kernel.getModelHandler.getActualModel)
              resultLabel.setText("Received ;-)")
              resultLabel.setToolTipText("Received ;-)")
              true
            } catch {
              case _@e => logger.debug("Unable to load model", e)
                resultLabel.setText("Unable to receive or use model ;-( " + e.getMessage)
                resultLabel.setToolTipText("Unable to receive or use model ;-( " + e.getMessage)
            }
          } catch {
            case _@e => {
              logger.error("", e)
              resultLabel.setText(e.getMessage)
              resultLabel.setToolTipText(e.getMessage)
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
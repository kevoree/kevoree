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

import javax.swing.JOptionPane
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot
import org.kevoree.tools.ui.editor.ws.{ModelCallBack, WebSocketClient}

object LoadRemoteModelUICommand {
  var lastRemoteNodeAddress: String = "localhost:9000"
}

class LoadRemoteModelUICommand extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  private val lcommand = new LoadModelCommand();

  var logger = LoggerFactory.getLogger(this.getClass)

  def remoteWebSocket(ip: String, port: String) = {
    try {

      WebSocketClient.pull(ip, port, new ModelCallBack {
        def run(model: ContainerRoot) {
          PositionedEMFHelper.updateModelUIMetaData(kernel)
          lcommand.setKernel(kernel)
          lcommand.execute(model)
        }
      })
    } catch {
      case _@e => {
        logger.debug("Pull failed to " + ip + ":" + port)
        false
      }
    }
  }


  def execute(p: Object) = {
    try {
      val result = JOptionPane.showInputDialog("Remote target node <ip:port>", LoadRemoteModelUICommand.lastRemoteNodeAddress)
      if (result != null && result != "") {
        LoadRemoteModelUICommand.lastRemoteNodeAddress = result
        val results = result.split(":").toList
        if (results.size >= 2) {
          val ip = results(0)
          val port = results(1)
          remoteWebSocket(ip, port)
        }
        true
      }
    } catch {
      case _@e => {
        logger.error("Bad Input , ip@port needed")
        false
      }
    }


  }

}

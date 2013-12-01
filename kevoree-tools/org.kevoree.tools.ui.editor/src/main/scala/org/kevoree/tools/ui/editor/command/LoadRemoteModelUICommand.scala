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

import java.net.URI
import javax.swing.JOptionPane
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import java.util.concurrent.{TimeUnit, Exchanger}
import org.kevoree.ContainerRoot
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.kevoree.loader.JSONModelLoader

object LoadRemoteModelUICommand {
  var lastRemoteNodeAddress: String = "localhost:9000"
}

class LoadRemoteModelUICommand extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  private val lcommand = new LoadModelCommand();

  var logger = LoggerFactory.getLogger(this.getClass)

  def tryRemoteWebSocket(ip: String, port: String): Boolean = {
    try {
      val exchanger = new Exchanger[ContainerRoot]()

      val client = new WebSocketClient(URI.create("ws://" + ip + ":" + port + "/")) {
        def onError(p1: Exception) {
          p1.printStackTrace()
          exchanger.exchange(null)
        }

        def onMessage(p1: String) {
          val root = jsonModelLoader.loadModelFromString(p1).get(0).asInstanceOf[ContainerRoot]
          try {
            exchanger.exchange(root);
          } catch {
            case _@e => //logger.error("", e)
          } finally {
            close()
          }
        }

        def onClose(p1: Int, p2: String, p3: Boolean) {}

        def onOpen(p1: ServerHandshake) {
        }

        var jsonModelLoader = new JSONModelLoader()

      }
      // instead of using connectBlocking method which lock the current thread (which is the one that represent the complete editor) we just wait 2s after initializing the connection
      client.connect()
      Thread.sleep(2000)
      if (client.getConnection.isOpen) {
        client.send("get");
      }
      val root = exchanger.exchange(null, 2000, TimeUnit.MILLISECONDS)
      if (root == null) {
        false
      } else {
        PositionedEMFHelper.updateModelUIMetaData(kernel)
        lcommand.setKernel(kernel)
        lcommand.execute(root)
        true
      }
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
          if (!tryRemoteWebSocket(ip, port)) {
            logger.error("Can't load model from node")
          }
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

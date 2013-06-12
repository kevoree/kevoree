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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.ui.editor.command

import java.net.URI
import javax.swing.JOptionPane
import org.kevoree.loader.JSONModelLoader
import org.kevoree.serializer.JSONModelSerializer
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

object LoadContinuousRemoteModelUICommand {
  var lastRemoteNodeAddress: String = "localhost:8080"
}

class LoadContinuousRemoteModelUICommand extends Command {

  var kernel: KevoreeUIKernel = null

  var jsonLoader = new JSONModelLoader()

  def setKernel(k: KevoreeUIKernel) = kernel = k

  private val lcommand = new LoadModelCommand();

  var logger = LoggerFactory.getLogger(this.getClass)

  var client : WebSocketClient = null
  var thread : Thread = null
  var saver = new JSONModelSerializer();

  def send(){
    val oo = new ByteArrayOutputStream()
    saver.serialize(kernel.getModelHandler.getActualModel,oo)
    client.send(oo.toByteArray)
  }

  def tryRemoteWebSocket(ip: String, port: String): Boolean = {
    try {
      System.out.println(ip+":"+port);
      client = new WebSocketClient(URI.create("ws://" + ip + ":" + port )) {
        def onError(p1: Exception) {
          p1.printStackTrace();
        }

        def onMessage(p1: String) {
          val root = jsonLoader.loadModelFromStream(new ByteArrayInputStream(p1.getBytes())).get(0)
          try {
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            lcommand.setKernel(kernel)
            lcommand.execute(root)
          } catch {
            case _@e => logger.error("", e)
          }
        }

        def onClose(p1: Int, p2: String, p3: Boolean) {
          System.out.println("Close");
        }

        def onOpen(p1: ServerHandshake) {
          System.out.println(p1.getHttpStatusMessage);
        }

      }
      client.connectBlocking();
      true
    } catch {
      case _@e => {
        logger.debug("Pull failed to " + ip + ":" + port)
        false
      }
    }
  }


  def close(){
    if(thread!=null){
      client.close();
      thread.stop();
    }
  }

  def execute(p: Object) = {

     close();

    try {
      val result = JOptionPane.showInputDialog("Remote target node <ip:port>", LoadContinuousRemoteModelUICommand.lastRemoteNodeAddress)
      if (result != null && result != "") {
        LoadContinuousRemoteModelUICommand.lastRemoteNodeAddress = result
        val results = result.split(":").toList
        if (results.size >= 2) {
          val ip = results(0)
          val port = results(1)
          thread = new Thread(){
            override def run() {
              if (!tryRemoteWebSocket(ip, port)) {
                logger.error("Can't load model from node")
              }
            }
          }
          thread.start();
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

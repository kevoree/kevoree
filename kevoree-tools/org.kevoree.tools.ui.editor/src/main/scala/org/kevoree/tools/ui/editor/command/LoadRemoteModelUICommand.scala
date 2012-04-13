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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.ui.editor.command

import java.net.URL
import javax.swing.JOptionPane
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory

object LoadRemoteModelUICommand {
  var lastRemoteNodeAddress : String = "localhost:8000"
}

class LoadRemoteModelUICommand extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k
  
  private val lcommand = new LoadModelCommand();

  var logger = LoggerFactory.getLogger(this.getClass)

  def tryRemoteLoad(zip : Boolean) : Boolean ={
    try{
      val result = JOptionPane.showInputDialog("Remote target node : ip@port", LoadRemoteModelUICommand.lastRemoteNodeAddress)
      if (result != null && result != "") {
        LoadRemoteModelUICommand.lastRemoteNodeAddress = result
        val results = result.split(":").toList
        if(results.size >= 2){
          val ip = results(0)
          val port = results(1)
          //CALL POST REMOTE URL
          if (zip){
            val url = new URL("http://"+ip+":"+port+"/model/current/zip");
            val conn = url.openConnection();
            conn.setConnectTimeout(2000);
            val inputStream = conn.getInputStream
            kernel.getModelHandler.merge(KevoreeXmiHelper.loadCompressedStream(inputStream))
            logger.debug("Load model from zip stream")
          } else {
            val url = new URL("http://"+ip+":"+port+"/model/current");
            val conn = url.openConnection();
            conn.setConnectTimeout(2000);
            val inputStream = conn.getInputStream
            kernel.getModelHandler.merge(KevoreeXmiHelper.loadStream(inputStream))
            logger.debug("Load model from xml stream")
          }
          PositionedEMFHelper.updateModelUIMetaData(kernel)
          lcommand.setKernel(kernel)
          lcommand.execute(kernel.getModelHandler.getActualModel)
        }
      }
      true
    } catch {
      case _ @ e => {e.printStackTrace ; false }
    }
  }

  def execute(p: Object) = {

    //ASK USER FOR ADRESS & PORT
    if (!tryRemoteLoad(true)){
      tryRemoteLoad(false)
    }
  }
  
}

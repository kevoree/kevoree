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

import javax.jmdns.{ServiceInfo, JmDNS}

import org.kevoree.KevoreeFactory
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import java.io.File
import util.Random
import org.kevoree.framework.{KevoreeXmiHelper, KevoreePlatformHelper}

/**
 * User: ffouquet
 * Date: 14/09/11
 * Time: 11:18
 */

class JmDnsLookup extends Command {
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
  }

  def execute(p: AnyRef) {

    new Thread() {
      override def run() {
        JmDNSListener.lookup().foreach {
          info =>

            kernel.getModelHandler.getActualModel.getTypeDefinitions.find(td => td.getName == info.getNiceTextString) match {
              case Some(nodeTypeDef) => {
                if (!kernel.getModelHandler.getActualModel.getNodes.exists(node => node.getName == info.getName.trim())) {
                  val newnode = KevoreeFactory.eINSTANCE.createContainerNode
                  newnode.setName(info.getName.trim())
                  newnode.setTypeDefinition(nodeTypeDef)
                  kernel.getModelHandler.getActualModel.addNodes(newnode)
                }
                KevoreePlatformHelper.updateNodeLinkProp(kernel.getModelHandler.getActualModel, info.getName.trim(), info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, info.getInet4Addresses()(0).getHostAddress, "LAN", 100)
              //  KevoreePlatformHelper.updateNodeLinkProp(kernel.getModelHandler.getActualModel, info.getName.trim(), info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT, info.getPort.toString, "LAN", 100)

                //TODO CALL & MERGE MODEL IF IP COMMUNICATION AVAILABLE
              }
              case None => println(info.getNiceTextString+" type definition not found")
            }


        }
        PositionedEMFHelper.updateModelUIMetaData(kernel)
        val file = File.createTempFile("kev", new Random().nextInt + "")
        KevoreeXmiHelper.save(file.getAbsolutePath, kernel.getModelHandler.getActualModel);
        val loadCMD = new LoadModelCommand
        loadCMD.setKernel(kernel)
        loadCMD.execute(file.getAbsolutePath)
      }
    }.start()

  }


}

object JmDNSListener {
  val REMOTE_TYPE: String = "_kevoree-remote._tcp.local."
  val jmdns = JmDNS.create("KevoreeEditor")
  Runtime.getRuntime.addShutdownHook(new Thread("KevoreeJmDNSStop") {
    override def run() {
      try {
        jmdns.close()
      } catch {
        case _@ex =>
      }
    }
  });

  def lookup(): Array[ServiceInfo] = {
    jmdns.list(REMOTE_TYPE)
  }

}
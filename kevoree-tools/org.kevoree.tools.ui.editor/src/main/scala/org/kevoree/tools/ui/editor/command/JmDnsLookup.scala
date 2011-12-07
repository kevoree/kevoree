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
import java.io.File
import util.Random
import org.kevoree.framework.{KevoreeXmiHelper, KevoreePlatformHelper}
import org.kevoree.tools.aether.framework.GroupTypeBootstrapHelper
import org.kevoree.tools.ui.editor.{EmbeddedOSGiEnv, PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory

/**
 * User: ffouquet
 * Date: 14/09/11
 * Time: 11:18
 */

class JmDnsLookup extends Command {
  var kernel: KevoreeUIKernel = null
  private val logger = LoggerFactory.getLogger(this.getClass)

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
  }

  def execute(p: AnyRef) {

    new Thread() {
      override def run() {
        JmDNSListener.lookup().foreach {
          info =>
            val nodeName = info.getName.trim()
            val groupName = info.getSubtype.trim()
            val port = info.getPort.toString.trim()
            val typeNames = new String(info.getTextBytes, "UTF-8");
            val typeNamesArray = typeNames.split("/")

            logger.debug("nodeName "+nodeName+" groupName "+groupName+" port "+port+" typeNames "+typeNames)

            kernel.getModelHandler.getActualModel.getTypeDefinitions.find(td => td.getName == typeNamesArray(0)) match {
              case Some(groupTypeDef) => {
                if (!kernel.getModelHandler.getActualModel.getGroups.exists(group => group.getName == groupName)) {
                  val newgroup = KevoreeFactory.eINSTANCE.createGroup
                  newgroup.setName(groupName)
                  newgroup.setTypeDefinition(groupTypeDef)
                  kernel.getModelHandler.getActualModel.addGroups(newgroup)
                }

                val remoteNode = kernel.getModelHandler.getActualModel.getNodes.find(n => n.getName == nodeName).getOrElse {
                  val newnode = KevoreeFactory.eINSTANCE.createContainerNode
                  newnode.setName(nodeName)
                  kernel.getModelHandler.getActualModel.getTypeDefinitions.find(td => td.getName == typeNamesArray(1)).map {
                    nodeType =>
                      newnode.setTypeDefinition(nodeType)
                  }
                  kernel.getModelHandler.getActualModel.addNodes(newnode)
                  newnode
                }

                kernel.getModelHandler.getActualModel.getGroups.find(group => group.getName == groupName).map {
                  group =>
                    group.getTypeDefinition.getDictionaryType.map {
                      dicTypeDef =>
                        dicTypeDef.getAttributes.find(att => att.getName == "port").map {
                          attPort =>
                            val dic = group.getDictionary.getOrElse(KevoreeFactory.createDictionary)
                            val dicValue = dic.getValues.find(dicVal => dicVal.getAttribute == attPort && dicVal.getTargetNode.isDefined && dicVal.getTargetNode.get.getName == nodeName).getOrElse {
                              val newDicVal = KevoreeFactory.createDictionaryValue
                              newDicVal.setAttribute(attPort)
                              newDicVal.setTargetNode(Some(remoteNode))
                              dic.addValues(newDicVal)
                              newDicVal
                            }
                            dicValue.setValue(port)
                            group.setDictionary(Some(dic))
                        }
                    }
                    if (group.getSubNodes.find(subNode => subNode.getName == groupName).isEmpty) {
                      group.addSubNodes(remoteNode)
                    }
                }


                val bootHelper = new GroupTypeBootstrapHelper
                bootHelper.bootstrapGroupType(kernel.getModelHandler.getActualModel, groupName, EmbeddedOSGiEnv.getFwk(kernel).getBundleContext) match {
                  case Some(groupTypeInstance) => {
                    val model = groupTypeInstance.pull(nodeName)
                    kernel.getModelHandler.merge(model)
                    PositionedEMFHelper.updateModelUIMetaData(kernel)
                    val file = File.createTempFile("kev", new Random().nextInt + "")
                    KevoreeXmiHelper.save(file.getAbsolutePath, kernel.getModelHandler.getActualModel);
                    val loadCMD = new LoadModelCommand
                    loadCMD.setKernel(kernel)
                    loadCMD.execute(file.getAbsolutePath)

                    KevoreePlatformHelper.updateNodeLinkProp(kernel.getModelHandler.getActualModel, nodeName, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, info.getInet4Addresses()(0).getHostAddress, "LAN", 100)


                  }
                  case None => logger.error("Error while bootstraping group type")
                }
              }
              case None => println(info.getNiceTextString + " type definition not found")
            }


        }
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
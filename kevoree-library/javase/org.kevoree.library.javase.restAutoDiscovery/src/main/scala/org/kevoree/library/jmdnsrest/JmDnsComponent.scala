package org.kevoree.library.jmdnsrest

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

import java.util.HashMap
import javax.jmdns.{ServiceEvent, ServiceListener, ServiceInfo, JmDNS}
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import actors.Actor
import org.slf4j.LoggerFactory
import java.net.InetAddress
import org.kevoree.tools.marShell.KevsEngine
import org.kevoree.{KevoreeFactory, ContainerRoot}

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 17:42
 */

class JmDnsComponent(nodeName: String, groupName: String, modelPort: Int, modelHandler: KevoreeModelHandlerService, groupTypeName: String) {

  val logger = LoggerFactory.getLogger(this.getClass)
  var servicelistener: ServiceListener = null
  final val REMOTE_TYPE: String = "_kevoree-remote._tcp.local."
  val values = new HashMap[String, String]

  def updateModelNetwork(nodeName: String, nodeType: String, groupName: String, groupType: String, groupPort: String): Option[ContainerRoot] = {
    val currentModel = modelHandler.getLastModel //GET & CLONE

    val groupTypeDef = currentModel.getTypeDefinitions.find(td => td.getName == groupType)
    val nodeTypeDef = currentModel.getTypeDefinitions.find(td => td.getName == nodeType)
    if (groupTypeDef.isEmpty || nodeTypeDef.isEmpty) {
      return None
    }
    //CREATE GROUP IF NOT EXIST
    val currentGroup = currentModel.getGroups.find(group => group.getName == groupName).getOrElse {
      val newgroup = KevoreeFactory.eINSTANCE.createGroup
      newgroup.setName(groupName)
      newgroup.setTypeDefinition(groupTypeDef.get)
      currentModel.addGroups(newgroup)
      newgroup
    }
    val remoteNode = currentModel.getNodes.find(n => n.getName == nodeName).getOrElse {
      val newnode = KevoreeFactory.eINSTANCE.createContainerNode
      newnode.setName(nodeName)
      currentModel.getTypeDefinitions.find(td => td.getName == nodeType).map {
        nodeType => newnode.setTypeDefinition(nodeType)
      }
      currentModel.addNodes(newnode)
      newnode
    }
    currentGroup.getTypeDefinition.getDictionaryType.map {
      dicTypeDef =>
        dicTypeDef.getAttributes.find(att => att.getName == "port").map {
          attPort =>
            val dic = currentGroup.getDictionary.getOrElse(KevoreeFactory.createDictionary)
            val dicValue = dic.getValues.find(dicVal => dicVal.getAttribute == attPort && dicVal.getTargetNode.isDefined && dicVal.getTargetNode.get.getName == nodeName).getOrElse {
              val newDicVal = KevoreeFactory.createDictionaryValue
              newDicVal.setAttribute(attPort)
              newDicVal.setTargetNode(Some(remoteNode))
              dic.addValues(newDicVal)
              newDicVal
            }
            dicValue.setValue(groupPort)
            currentGroup.setDictionary(Some(dic))
        }
    }
    if (currentGroup.getSubNodes.find(subNode => subNode.getName == groupName).isEmpty) {
      currentGroup.addSubNodes(remoteNode)
    }
    Some(currentModel)
  }

  def addService(p1:ServiceEvent)
  {
    val typeNames = new String(p1.getInfo.getTextBytes, "UTF-8");
    val typeNamesArray = typeNames.split("/")
    val resultModel = updateModelNetwork(p1.getInfo.getName.trim(), typeNamesArray(1), groupName, typeNamesArray(0), p1.getInfo().getPort.toString)
    resultModel.map {
      goodModel => {

        modelHandler.updateModel(goodModel)
      }
    }
  }

  // TODO Listen interfaces ?
  val jmdns = JmDNS.create()
  logger.debug(" JmDNS listen on " + jmdns.getInterface());
  servicelistener = new ServiceListener() {

    def serviceAdded(p1: ServiceEvent) {
      jmdns.requestServiceInfo(p1.getType(), p1.getName(), 1);
      addService(p1)
    }

    def serviceResolved(p1: ServiceEvent) {

      logger.debug("Service resolved: " + p1.getInfo().getQualifiedName() + " port:" + p1.getInfo().getPort());
      addService(p1)
    }



    def serviceRemoved(p1: ServiceEvent) {
      logger.debug("Service removed " + p1.getName)
      //TODO REMOVE NODE FROM JMDNS GROUP INSTANCES SUBNODES
    }
  };

  jmdns.addServiceListener(REMOTE_TYPE, servicelistener)


  new Thread() {
    override def run() {
      val nodeType = modelHandler.getLastModel.getNodes.find(n => n.getName == nodeName).get.getTypeDefinition.getName


      val pairservice: ServiceInfo = ServiceInfo.create(REMOTE_TYPE, nodeName, groupName, modelPort, "")
      pairservice.setText((groupTypeName + "/" + nodeType).getBytes("UTF-8"))

      logger.debug("nodeName " + nodeName + " groupName " + groupName + " modelPort " + modelPort + " groupTypeName " + groupTypeName + " nodeType " + nodeType)

      jmdns.registerService(pairservice)

    }
  }.start()

  def close() {
    new Thread() {
      override def run() {
        if (servicelistener != null) {
          jmdns.removeServiceListener(REMOTE_TYPE, servicelistener)
        }
        jmdns.close()
      }

    }.start()
  }
}
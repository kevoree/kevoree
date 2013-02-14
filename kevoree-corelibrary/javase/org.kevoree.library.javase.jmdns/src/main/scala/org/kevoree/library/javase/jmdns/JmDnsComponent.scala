package org.kevoree.library.javase.jmdns

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

import org.slf4j.LoggerFactory
import java.net.{Inet4Address, InetAddress}
import java.util
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import org.kevoree.framework.{AbstractGroupType, KevoreePlatformHelper}
import javax.jmdns.{ServiceInfo, ServiceEvent, JmDNS, ServiceListener}

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 17:42
 */

class JmDnsComponent(group: AbstractGroupType, jmDNSListener : JmDNSListener, modelPort: Int, interface: InetAddress, val ipv4Only: Boolean = false) {
  val logger = LoggerFactory.getLogger(this.getClass)
  var serviceListener: ServiceListener = null
  var jmdns : JmDNS = null
  final val REMOTE_TYPE: String = "_kevoree-remote._tcp.local."
  val nodeAlreadyDiscovered = new util.ArrayList[String]

  val factory = new DefaultKevoreeFactory

  def start() {
    jmdns = JmDNS.create(interface, group.getNodeName + "." + interface.getAddress.toString)
    logger.info("JmDNS listen on {}",interface.getAddress.toString)

    serviceListener = new ServiceListener() {

      def serviceAdded(p1: ServiceEvent) {
        if (p1.getInfo.getSubtype == group.getName) {
          jmdns.requestServiceInfo(p1.getType, p1.getName, 1)
          addNodeDiscovered(p1.getInfo)
        }
      }

      def serviceResolved(p1: ServiceEvent) {
        if (p1.getInfo.getSubtype == group.getName) {
          logger.info("Node discovered: {} port: {}", Array[Object](p1.getInfo.getName, new Integer(p1.getInfo.getPort)))
          addNodeDiscovered(p1.getInfo)
        }
      }

      def serviceRemoved(p1: ServiceEvent) {
        if (p1.getInfo.getSubtype == group.getName) {
          logger.info("Node disappeared ", p1.getInfo.getName)
          // REMOVE NODE FROM JMDNS GROUP INSTANCES SUBNODES
          if (group.getName == p1.getInfo.getSubtype && nodeAlreadyDiscovered.contains(p1.getInfo.getName)) {
            nodeAlreadyDiscovered.remove(p1.getInfo.getName)
          }
        }
      }
    }

    jmdns.addServiceListener(REMOTE_TYPE, serviceListener)


    new Thread() {
      override def run() {
        val model = group.getModelService.getLastModel
        // register the local group fragment on jmdns
        val localServiceInfo: ServiceInfo = ServiceInfo.create(REMOTE_TYPE, group.getNodeName, group.getName, modelPort, "")

        val props = new util.HashMap[String, String](3)
        props.put("groupType", group.getModelElement.getTypeDefinition.getName)
        props.put("nodeType", model.findNodesByID(group.getNodeName).getTypeDefinition.getName)
        localServiceInfo.setText(props)

        jmdns.registerService(localServiceInfo)
      }
    }.start()
  }

  def stop() {
    new Thread() {
      override def run() {
        if (serviceListener != null) {
          jmdns.removeServiceListener(REMOTE_TYPE, serviceListener)
        }
        jmdns.close()
      }
    }.start()
  }

  private def addNode(model: ContainerRoot, nodeName: String, nodeType: String) {
    var remoteNode = model.findNodesByID(nodeName)
    if (remoteNode == null) {
      var nodeTypeDef = model.findTypeDefinitionsByID(nodeType)
      if (nodeTypeDef == null) {
        nodeTypeDef = model.findNodesByID(group.getNodeName).getTypeDefinition
      }
      remoteNode = factory.createContainerNode
      remoteNode.setName(nodeName)
      remoteNode.setTypeDefinition(nodeTypeDef)
      model.addNodes(remoteNode)
    }
  }

  private def updateNetworkProperties(model: ContainerRoot, remoteNodeName: String, addresses: Array[InetAddress]) {
    addresses.filter(a => (ipv4Only && a.isInstanceOf[Inet4Address]) || !ipv4Only).foreach {
      address => KevoreePlatformHelper.updateNodeLinkProp(model, group.getNodeName, remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, address.getHostAddress, "LAN-" + address.getHostAddress, 100)
    }

  }

  private def updateGroup(model: ContainerRoot, remoteNodeName: String, port: Int) {
    val currentGroup = model.findGroupsByID(group.getName)
    val remoteNode = model.findNodesByID(remoteNodeName)
    if (remoteNode != null) {
      val dicTypeDef = currentGroup.getTypeDefinition.getDictionaryType
      if (dicTypeDef != null) {
        val attPort = dicTypeDef.findAttributesByID("port")
        if (attPort != null) {
          val dic = currentGroup.getDictionary match {
            case null => {
              val dic = factory.createDictionary
              currentGroup.setDictionary(dic)
              dic
            }
            case d: org.kevoree.Dictionary => d
          }
          val dicValue = dic.getValues.find(dicVal => dicVal.getAttribute == attPort && dicVal.getTargetNode != null && dicVal.getTargetNode.getName == remoteNodeName).getOrElse {
            val newDicVal = factory.createDictionaryValue
            newDicVal.setAttribute(attPort)
            newDicVal.setTargetNode(remoteNode)
            dic.addValues(newDicVal)
            newDicVal
          }
          dicValue.setValue(port.toString)
        }
      }
      if (currentGroup.findSubNodesByID(remoteNodeName) == null) {
        currentGroup.addSubNodes(remoteNode)
      }
    }
  }

  /**
   * add a discovered node in the model and request an update
   */
  def addNodeDiscovered(p1: ServiceInfo) {
    logger.info(p1.getInet4Addresses.mkString(", "))
    logger.info(p1.getInet6Addresses.mkString(", "))
    if (p1.getInetAddresses.length > 0 && p1.getPort != 0) {
      if (!nodeAlreadyDiscovered.contains(p1.getName)) {
        val nodeType = p1.getPropertyString("nodeType")
        val groupType = p1.getPropertyString("groupType")
        val groupName = p1.getSubtype

        // TODO do we need to clone the model ?
        val model = group.getModelService.getLastModel
        addNode(model, p1.getName, nodeType)
        updateNetworkProperties(model, p1.getName, p1.getInetAddresses)
        if (groupName == group.getName && groupType == group.getModelElement.getTypeDefinition.getName) {
          updateGroup(model, p1.getName, p1.getPort)
          nodeAlreadyDiscovered.add(p1.getName)
        } else {
          logger.warn("{} discovers a node using a group which is not the same as the local one:{}.", Array[String](group.getName, p1.toString))
        }
        if (updateModel(model)) {
          jmDNSListener.notifyNewSubNode(p1.getName)
        }
        logger.debug("List of discovered nodes <{}>", nodeAlreadyDiscovered.mkString(", "))
      }
    } else {
      logger.error("Unable to get address or port from {} and {}", Array[Object](p1.getInetAddresses.mkString(","), new Integer(p1.getPort)))
    }
  }

  private def updateModel(model: ContainerRoot) : Boolean = {
    var created: Boolean = false
    var i = 1
    while (!created) {
      try {
        group.getModelService.unregisterModelListener(group)
        group.getModelService.atomicUpdateModel(model)
        group.getModelService.registerModelListener(group)
        created = true
      } catch {
        case e: Exception => {
          logger.warn("Error while trying to update model due to {}, try number {}", Array[Object](e.getMessage, new Integer(i)))
        }
      }
      if (i == 20) {
        logger.warn("Unable to update model after {} tries. Update aborted !", i)
      }
      i = i + 1
    }
    created
  }

  /*
  Request from the user to scan the network
  */
  def requestUpdateList(time: Int) {
    for (ser <- jmdns.list(REMOTE_TYPE, time)) {
      addNodeDiscovered(ser)
    }
  }
}
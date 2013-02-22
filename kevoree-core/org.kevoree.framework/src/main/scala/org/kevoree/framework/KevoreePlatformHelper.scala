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

package org.kevoree.framework

import org.slf4j.LoggerFactory
import org.kevoree.{ContainerNode, ContainerRoot}
import org.kevoree.impl.DefaultKevoreeFactory
import scala.collection.JavaConversions._

object KevoreePlatformHelper {

  val logger = LoggerFactory.getLogger(this.getClass)
  val factory = new DefaultKevoreeFactory()
  def updateNodeLinkProp(actualModel: ContainerRoot, currentNodeName: String, targetNodeName: String, key: String, value: String, networkType: String, weight: Int): ContainerNode = {

    var thisNodeFound: ContainerNode = null
    /* SEARCH THE NODE NETWORK */
    val nodenetwork = actualModel.getNodeNetworks.find({
      nn =>
        nn.getInitBy.getName == currentNodeName && nn.getTarget.getName == targetNodeName
    }) getOrElse {
      val newNodeNetwork = factory.createNodeNetwork

      thisNodeFound = actualModel.findNodesByID(currentNodeName)
      if (thisNodeFound == null) {
        thisNodeFound = factory.createContainerNode
        thisNodeFound.setName(currentNodeName)
        actualModel.addNodes(thisNodeFound)
      }

      var targetNode = actualModel.findNodesByID(targetNodeName)
      if (targetNode == null) {
        logger.debug("Unknown node " + targetNodeName + " add to model")
        targetNode = factory.createContainerNode
        targetNode.setName(targetNodeName)
        actualModel.addNodes(targetNode)
      }
      newNodeNetwork.setTarget(targetNode)
      newNodeNetwork.setInitBy(thisNodeFound)
      actualModel.addNodeNetworks(newNodeNetwork)
      newNodeNetwork
    }

    /* Found node link */
    val nodelink = nodenetwork.getLink.find(l => l.getNetworkType == networkType).getOrElse {
      val newlink = factory.createNodeLink
      newlink.setNetworkType(networkType)
      nodenetwork.addLink(newlink)
      newlink
    }
    try {
      nodelink.setEstimatedRate(weight)
    } catch {
      case _@e => logger.debug("Unexpected estimate rate", e)
    }

    /* Found Property and SET remote IP */
    var prop = nodelink.findNetworkPropertiesByID(key)
    if (prop == null) {
      prop = factory.createNetworkProperty
      prop.setName(key)
      nodelink.addNetworkProperties(prop)
    }
    prop.setValue(value)
    prop.setLastCheck(new java.util.Date().getTime.toString)
    logger.debug("New node link prop registered = " + targetNodeName + "," + key + "," + value)
    thisNodeFound
  }

  def getProperty(model: ContainerRoot, targetNodeName: String, key: String): String = {
    val filteredNodeNetwork = model.getNodeNetworks.filter(lNN => lNN.getTarget.getName == targetNodeName)
    var bestResultProp = ""
    filteredNodeNetwork.foreach {
      fnn =>
        fnn.getLink.foreach {
          fnl =>
            val networkProperty = fnl.findNetworkPropertiesByID(key)
            if (networkProperty != null) {
              bestResultProp = networkProperty.getValue
            }
        }
    }
    bestResultProp
  }


}

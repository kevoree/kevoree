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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}

import org.kevoree.tools.marShell.ast.NetworkPropertyStatement
import org.kevoree.{ContainerNode}
import scala.collection.JavaConversions._
import org.kevoree.log.Log

case class KevsNetworkInterpreter(networkStatement: NetworkPropertyStatement) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    import scala.collection.JavaConversions._
    networkStatement.props.foreach {
      prop =>
        networkStatement.srcNodeName match {
          case Some(srcNode) => updateNodeLinkProp(context, srcNode, networkStatement.targetNodeName, prop._1, prop._2, "", 100)
          case None => updateNodeLinkProp(context, networkStatement.targetNodeName, networkStatement.targetNodeName, prop._1, prop._2, "", 100)
        }
    }
    true
  }

  def updateNodeLinkProp(context: KevsInterpreterContext, currentNodeName: String, targetNodeName: String, key: String, value: String, networkType: String, weight: Int) {
    /* SEARCH THE NODE NETWORK */
    val actualModel = context.model
    val nodenetwork = actualModel.getNodeNetworks.find({
      nn =>
        nn.getInitBy.getName == currentNodeName && nn.getTarget.getName == targetNodeName
    }) getOrElse {
      val newNodeNetwork = context.kevoreeFactory.createNodeNetwork
      var thisNode = actualModel.findByPath("nodes[" + currentNodeName + "]", classOf[ContainerNode])
      var targetNode = actualModel.findByPath("nodes[" + targetNodeName + "]", classOf[ContainerNode])

      if (thisNode == null){
        val newnode = context.kevoreeFactory.createContainerNode
        newnode.setName(currentNodeName)
        actualModel.addNodes(newnode)
        thisNode= newnode
      }
      val thisNodeFound = thisNode
      if (targetNode == null){
        val newnode = context.kevoreeFactory.createContainerNode
        newnode.setName(targetNodeName)
        actualModel.addNodes(newnode)
        targetNode=newnode
      }

      newNodeNetwork.setTarget(targetNode)
      newNodeNetwork.setInitBy(thisNodeFound)
      actualModel.addNodeNetworks(newNodeNetwork)
      newNodeNetwork
    }

    /* Found node link */
    val nodelink = nodenetwork.getLink.find(loopLink => loopLink.getNetworkType == networkType).getOrElse {
      val newlink = context.kevoreeFactory.createNodeLink
      newlink.setNetworkType(networkType)
      nodenetwork.addLink(newlink)
      newlink
    }
    try {
      nodelink.setEstimatedRate(weight)
    } catch {
      case _@e => Log.error("Unexpected estimate rate", e)
    }
    /* Found Property and SET remote IP */
    val prop = nodelink.getNetworkProperties.find({
      networkProp => networkProp.getName == key
    }).getOrElse {
      val newprop = context.kevoreeFactory.createNetworkProperty
      newprop.setName(key)
      nodelink.addNetworkProperties(newprop)
      newprop
    }
    prop.setValue(value)
    prop.setLastCheck(new java.util.Date().getTime.toString)
  }
}
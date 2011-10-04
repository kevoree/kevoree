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

package org.kevoree.framework

import org.kevoree._
 import org.slf4j.LoggerFactory

object KevoreePlatformHelper {

  val logger = LoggerFactory.getLogger(this.getClass)

  def updateNodeLinkProp(actualModel : ContainerRoot,currentNodeName : String,targetNodeName:String,key:String,value:String,networkType : String,weight:Int) = {

    /* SEARCH THE NODE NETWORK */
    val nodenetwork = actualModel.getNodeNetworks.find({nn =>
        nn.getInitBy.get.getName == currentNodeName && nn.getTarget.getName == targetNodeName }) getOrElse {
      val newNodeNetwork = KevoreeFactory.eINSTANCE.createNodeNetwork
      val thisNode = actualModel.getNodes.find({loopNode => loopNode.getName == currentNodeName })
      val thisNodeFound = thisNode.getOrElse{
        val newnode = KevoreeFactory.eINSTANCE.createContainerNode
        newnode.setName(currentNodeName)
        actualModel.addNodes(newnode)
        newnode
      }
      val targetNode = actualModel.getNodes.find({loopNode => loopNode.getName == targetNodeName })
      newNodeNetwork.setTarget(targetNode.getOrElse{
          logger.debug("Unknow node "+targetNodeName+" add to model")
          val newnode =KevoreeFactory.eINSTANCE.createContainerNode
          newnode.setName(targetNodeName)
          actualModel.addNodes(newnode)
          newnode
        })
      newNodeNetwork.setInitBy(thisNodeFound)
      actualModel.addNodeNetworks(newNodeNetwork)
      newNodeNetwork
    }

    /* Found node link */
    val nodelink = nodenetwork.getLink.find(loopLink => loopLink.getNetworkType == networkType).getOrElse{
      val newlink = KevoreeFactory.eINSTANCE.createNodeLink
      newlink.setNetworkType(networkType)
      nodenetwork.addLink(newlink)
      newlink
    }
    try { nodelink.setEstimatedRate(weight) } catch {
      case _ @ e => logger.debug("Unexpected estimate rate",e)
    }

    /* Found Property and SET remote IP */
    val prop = nodelink.getNetworkProperties.find({networkProp => networkProp.getName == key }).getOrElse{
      val newprop = KevoreeFactory.eINSTANCE.createNetworkProperty
      newprop.setName(key)
      nodelink.addNetworkProperties(newprop)
      newprop
    }
    prop.setValue(value)
    prop.setLastCheck(new java.util.Date().getTime.toString)

    logger.debug("New node link prop registred = "+targetNodeName+","+key+","+value)

  }



  def getProperty(model:ContainerRoot,targetNodeName : String,key:String) : String = {
    val filteredNodeNetwork = model.getNodeNetworks.filter(lNN=> lNN.getTarget.getName == targetNodeName)
    var bestResultProp = ""
    filteredNodeNetwork.foreach{fnn=>
      fnn.getLink.foreach{fnl=>
        fnl.getNetworkProperties.find(p=> p.getName == key) match {
          case None =>
          case Some(prop)=> bestResultProp = prop.getValue
        }
      }
    }
    bestResultProp
  }





}

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
import scala.collection.JavaConversions._

object KevoreePlatformHelper {

  def updateNodeLinkProp(actualModel : ContainerRoot,currentNodeName : String,targetNodeName:String,key:String,value:String,networkType : String,weight:Int) = {
    var logger = LoggerFactory.getLogger(this.getClass);

    /* SEARCH THE NODE NETWORK */
    var nodenetwork = actualModel.getNodeNetworks.find({nn =>
        nn.getInitBy.getName == currentNodeName && nn.getTarget.getName == targetNodeName }) getOrElse {
      var newNodeNetwork = KevoreeFactory.eINSTANCE.createNodeNetwork
      var thisNode = actualModel.getNodes.find({loopNode => loopNode.getName == currentNodeName })
      var targetNode = actualModel.getNodes.find({loopNode => loopNode.getName == targetNodeName })
      var thisNodeFound = thisNode.getOrElse{
        var newnode = KevoreeFactory.eINSTANCE.createContainerNode
        newnode.setName(currentNodeName)
        actualModel.getNodes.add(newnode)
        newnode
      }

      newNodeNetwork.setTarget(targetNode.getOrElse{
          logger.warn("Unknow node "+targetNodeName+" add to model")
          var newnode =KevoreeFactory.eINSTANCE.createContainerNode
          newnode.setName(targetNodeName)
          actualModel.getNodes.add(newnode)
          newnode
        })
      newNodeNetwork.setInitBy(thisNodeFound)
      actualModel.getNodeNetworks.add(newNodeNetwork)
      newNodeNetwork
    }

    /* Found node link */
    var nodelink = nodenetwork.getLink.find(loopLink => loopLink.getNetworkType == networkType).getOrElse{
      var newlink = KevoreeFactory.eINSTANCE.createNodeLink
      newlink.setNetworkType(networkType)
      nodenetwork.getLink.add(newlink)
      newlink
    }
    try { nodelink.setEstimatedRate(weight) } catch {
      case _ @ e => logger.error("Unexpected estimate rate",e)
    }

    /* Found Property and SET remote IP */
    var prop = nodelink.getNetworkProperties.find({networkProp => networkProp.getName == key }).getOrElse{
      var newprop = KevoreeFactory.eINSTANCE.createNetworkProperty
      newprop.setName(key)
      nodelink.getNetworkProperties.add(newprop)
      newprop
    }
    prop.setValue(value)
    prop.setLastCheck(new java.util.Date().getTime.toString)

    logger.info("New node link prop registred = "+targetNodeName+","+key+","+value)

  }



  def getProperty(model:ContainerRoot,targetNodeName : String,key:String) : String = {
    var filteredNodeNetwork = model.getNodeNetworks.filter(lNN=> lNN.getTarget.getName == targetNodeName)
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

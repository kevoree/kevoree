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

package org.kevoree.framework.aspects

import org.kevoree._
 import KevoreeAspects._

case class ChannelAspect(cself : Channel) {

  /**
   * Returns true if the node in parameter hosts a component bound to this channel.
   */
  def usedByNode(nodeName:String) : Boolean = {
    getRelatedBindings.find(mb => mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName == nodeName  ) match {
      case None => false
      case Some(b)=> true
    }
  }

  /**
   * Returns a list of node's names the channel is linked with, except the nodeName given in parameter.
   */
  def getOtherFragment(nodeName : String) : List[String] = {
    var result : List[String] = List()
    getRelatedBindings.filter(mb=> mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName != nodeName  ).foreach{
      mb=>
        if(!result.contains(mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName)){
          result = result ++ List(mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName)
        }
    }
    result
  }

   /**
   * Returns a list of nodes the channel is linked with, except the node given in parameter.
   */
  def getConnectedNode(nodeName : String) : List[ContainerNode] = {
    var result : List[ContainerNode] = List()
    getRelatedBindings.filter(mb=> mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName != nodeName).foreach{
      mb=>
        if(!result.contains(mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode])){
          result = result ++ List(mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode])
        }
    }
    result
  }

  /**
   * Returns the list of all bindings belonging to this channel
   */
  def getRelatedBindings : List[MBinding] = {
    cself.eContainer.asInstanceOf[ContainerRoot].getMBindings.filter(b => b.getHub == cself)
  }

  /**
   * Returns the list of bindings belonging to this channel on the given node
   */
  def getRelatedBindings(node : ContainerNode) : List[MBinding] = {
    val res = new java.util.ArrayList[MBinding]();
    cself.eContainer.asInstanceOf[ContainerRoot].getMBindings.filter{b=>
      b.getHub == cself && b.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName == node.getName
    }
  }

  /**
   * Returns the list of all ContainerNode this binding is connected to.
   */
  def getRelatedNodes : List[ContainerNode] = {
    var result : List[ContainerNode] = List()
    getRelatedBindings.foreach{binding =>
      if(!result.contains(binding.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode])){
          result = result ++ List(binding.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode])
        }
    }
    result
  }

}

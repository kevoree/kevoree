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

package org.kevoree.framework.aspects

import org.kevoree._
 import KevoreeAspects._
import scala.collection.JavaConversions._

case class ChannelAspect(cself : Channel) {

  /**
   * Returns true if the node in parameter hosts a component bound to this channel.
   */
  def usedByNode(nodeName:String) : Boolean = {
    cself.getBindings().find(mb => mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == nodeName  ) match {
      case None => false
      case Some(b)=> true
    }
  }

  /**
   * Returns a list of node's names the channel is linked with, except the nodeName given in parameter.
   */
  def getOtherFragment(nodeName : String) : List[String] = {
    var result : List[String] = List()
    cself.getBindings().filter(mb=> mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName != nodeName  ).foreach{
      mb=>
        if(!result.contains(mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)){
          result = result ++ List(mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
        }
    }
    result
  }

   /**
   * Returns a list of nodes the channel is linked with, except the node given in parameter.
   */
  def getConnectedNode(nodeName : String) : List[ContainerNode] = {
    var result : List[ContainerNode] = List()
    cself.getBindings().filter(mb=> mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName != nodeName).foreach{
      mb=>
        if(!result.contains(mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode])){
          result = result ++ List(mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode])
        }
    }
    result
  }


  /**
   * Returns the list of bindings belonging to this channel on the given node
   */
  def getRelatedBindings(node : ContainerNode) : List[MBinding] = {
    cself.getBindings.filter{b=>
      b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == node.getName
    }.toList
  }

  /**
   * Returns the list of all ContainerNode this binding is connected to.
   */
  def getRelatedNodes : List[ContainerNode] = {
    var result : List[ContainerNode] = List()
    cself.getBindings.foreach{binding =>
      if(!result.contains(binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode])){
          result = result ++ List(binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode])
        }
    }
    result
  }

}

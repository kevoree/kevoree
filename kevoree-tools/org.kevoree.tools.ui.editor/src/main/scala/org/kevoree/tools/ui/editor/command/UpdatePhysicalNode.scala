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

import org.kevoree.tools.ui.editor.KevoreeUIKernel

import org.kevoree.{KevoreeFactory, ContainerNode}

/**
 * User: ffouquet
 * Date: 29/07/11
 * Time: 10:28
 */

class UpdatePhysicalNode extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  var targetCNode: ContainerNode = null

  def setTargetCNode(c: ContainerNode) {
    targetCNode = c
  }

  def execute(p: AnyRef) {
    p match {
      case "nohost" => {
        //CLEAN REMOTE HOST
        val model = kernel.getModelHandler.getActualModel
        model.getNodes.filter(node => node.getHosts.contains(targetCNode)).foreach{ r =>
           r.getHosts.remove(targetCNode)
        }
      }
      case physNodeName: String if (physNodeName != null && physNodeName!="") => {

        val model = kernel.getModelHandler.getActualModel
        //CLEAN PREVIOUS RELATIONSHIP
        model.getNodes.find(n => n.getName == physNodeName) match {
          case Some(host)=> {
            if(host.getHosts.contains(targetCNode)){host.getHosts.clear()}
            host.getHosts.add(targetCNode)
          }
          case None => println("Error : Node host not linked => "+physNodeName)
        }

      }
      case _ =>
    }
  }

}
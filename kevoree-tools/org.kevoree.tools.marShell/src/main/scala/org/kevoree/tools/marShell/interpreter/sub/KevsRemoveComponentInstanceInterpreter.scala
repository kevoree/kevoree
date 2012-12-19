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

import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.tools.marShell.ast.RemoveComponentInstanceStatment
import org.kevoree.{ContainerNode, ContainerRoot, ComponentInstance, MBinding}
import org.slf4j.LoggerFactory
import collection.mutable.ListBuffer

case class KevsRemoveComponentInstanceInterpreter(removeComponent: RemoveComponentInstanceStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def deleteComponent(targetNode: ContainerNode, targetComponent: ComponentInstance): Boolean = {
    val root = targetComponent.eContainer.eContainer.asInstanceOf[ContainerRoot]
    getRelatedBindings(targetComponent).foreach(rb => {
      root.removeMBindings(rb)
    })
    targetNode.removeComponents(targetComponent)
    true
  }


  def interpret(context: KevsInterpreterContext): Boolean = {
    //SEARCH NODE
    removeComponent.cid.nodeName match {
      case Some(nodeID) => {
        context.model.findByQuery("nodes[" + nodeID + "]", classOf[ContainerNode]) match {
          case Some(targetNode) => {
            //SEARCH COMPONENT
            targetNode.findByQuery("components[" + removeComponent.cid.componentInstanceName + "]/", classOf[ComponentInstance]) match {
              case Some(targetComponent) => deleteComponent(targetNode, targetComponent)
              case None => {
                logger.error("Component not found " + removeComponent.cid.componentInstanceName)
                false
              }
            }
          }
          case None => {
            logger.error("Node not found " + nodeID)
            false
          }
        }
      }
      case None => false //TODO solve ambiguity
    }
  }


  def getRelatedBindings(cself: ComponentInstance): List[MBinding] = {
    var res = ListBuffer[MBinding]()
    cself.getProvided.foreach(p => res = res ++ p.getBindings)
    cself.getRequired.foreach(p => res = res ++ p.getBindings)
    res.toList
  }

}

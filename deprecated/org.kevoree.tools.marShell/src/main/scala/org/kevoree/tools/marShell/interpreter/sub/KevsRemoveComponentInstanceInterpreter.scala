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
import collection.mutable.ListBuffer

case class KevsRemoveComponentInstanceInterpreter(removeComponent: RemoveComponentInstanceStatment) extends KevsAbstractInterpreter {

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
        context.model.findByPath("nodes[" + nodeID + "]", classOf[ContainerNode]) match {
          case targetNode : ContainerNode => {
            //SEARCH COMPONENT
            targetNode.findByPath("components[" + removeComponent.cid.componentInstanceName + "]/", classOf[ComponentInstance]) match {
              case targetComponent : ComponentInstance => deleteComponent(targetNode, targetComponent)
              case null => {
                context.appendInterpretationError("Could not remove instance '"+removeComponent.cid.componentInstanceName+"' from node '"+removeComponent.cid.nodeName+"' : ComponentInstance not found.")
                false
              }
            }
          }
          case null => {
            context.appendInterpretationError("Could not remove instance '"+removeComponent.cid.componentInstanceName+"' from node '"+removeComponent.cid.nodeName+"' : Node not found.")
            false
          }
        }
      }
      case None => {
        //TODO solve ambiguity
        context.appendInterpretationError("Could not remove instance '"+removeComponent.cid.componentInstanceName+"' from node '"+removeComponent.cid.nodeName+"' : NodeName not specified.")
        false
      }
    }
  }


  def getRelatedBindings(cself: ComponentInstance): List[MBinding] = {
    var res = ListBuffer[MBinding]()
    import scala.collection.JavaConversions._
    cself.getProvided.foreach(p => res = res ++ p.getBindings)
    cself.getRequired.foreach(p => res = res ++ p.getBindings)
    res.toList
  }

}

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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.MoveComponentInstanceStatment
import scala.collection.JavaConversions._

case class KevsMoveComponentInstanceInterpreter(moveComponent: MoveComponentInstanceStatment) extends KevsAbstractInterpreter {
  def interpret(context: KevsInterpreterContext): Boolean = {

    moveComponent.cid.nodeName match {
      case Some(nodeID) => {
        context.model.getNodes.find(node => node.getName == nodeID) match {
          case Some(sourceNode) => {
            //SEARCH COMPONENT
            sourceNode.getComponents.find(c => c.getName == moveComponent.cid.componentInstanceName) match {
              case Some(targetComponent) => {
                context.model.getNodes.find(node => node.getName == moveComponent.targetNodeName) match {
                  case Some(targetNode) => {
                    sourceNode.getComponents.remove(targetComponent)
                    targetNode.getComponents.add(targetComponent)
                    true
                  }
                  case None => {
                    println("Target node not found " + moveComponent.cid.componentInstanceName);
                    false
                  }
                }
              }
              case None => {
                println("Component not found " + moveComponent.cid.componentInstanceName);
                false
              }
            }
          }
          case None => {
            println("Source Node not found " + nodeID);
            false
          }
        }
      }
      case None => false //TODO solve ambiguity
    }

  }
}
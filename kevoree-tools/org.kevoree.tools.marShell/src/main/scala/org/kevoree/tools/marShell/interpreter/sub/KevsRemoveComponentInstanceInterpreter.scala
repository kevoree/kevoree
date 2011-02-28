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

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast.{RemoveComponentInstanceStatment}
import org.kevoree.{ContainerNode, ContainerRoot, ComponentInstance, MBinding}

case class KevsRemoveComponentInstanceInterpreter(removeComponent: RemoveComponentInstanceStatment) extends KevsAbstractInterpreter {

  def deleteComponent(targetNode: ContainerNode, targetComponent: ComponentInstance): Boolean = {
    val root = targetComponent.eContainer.eContainer.asInstanceOf[ContainerRoot]
    getRelatedBindings(targetComponent).foreach(rb => {
      root.getMBindings.remove(rb)
    })
    targetNode.getComponents.remove(targetComponent)
    true
  }


  def interpret(context: KevsInterpreterContext): Boolean = {
    //SEARCH NODE
    removeComponent.cid.nodeName match {
      case Some(nodeID) => {
        context.model.getNodes.find(n => n.getName == nodeID) match {
          case Some(targetNode) => {
            //SEARCH COMPONENT
            targetNode.getComponents.find(c => c.getName == removeComponent.cid.componentInstanceName) match {
              case Some(targetComponent) => deleteComponent(targetNode,targetComponent)
              case None => {
                println("Component not found " + removeComponent.cid.componentInstanceName);
                false
              }
            }
          }
          case None => {
            println("Node not found " + nodeID);
            false
          }
        }
      }
      case None => false //TODO solve ambiguity
    }
  }


  def getRelatedBindings(cself: ComponentInstance): List[MBinding] = {
    var res = new java.util.ArrayList[MBinding]();
    cself.eContainer.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach {
      b =>
        cself.getProvided.find({
          p => b.getPort == p
        }).map(e => res.add(b))
        cself.getRequired.find({
          p => b.getPort == p
        }).map(e => res.add(b))
    }
    res.toList
  }

}

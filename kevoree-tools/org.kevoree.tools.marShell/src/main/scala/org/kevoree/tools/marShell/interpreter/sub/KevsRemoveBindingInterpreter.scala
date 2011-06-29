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

import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast.{RemoveBindingStatment, AddBindingStatment}
import org.slf4j.LoggerFactory

case class KevsRemoveBindingInterpreter(removeBinding: RemoveBindingStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    removeBinding.cid.nodeName match {
      case Some(searchNodeName) => {
        context.model.getNodes.find(node => node.getName == searchNodeName) match {
          case Some(targetNode) => {
            targetNode.getComponents.find(component => component.getName == removeBinding.cid.componentInstanceName) match {
              case Some(targetComponent) => {
                context.model.getHubs.find(hub => hub.getName == removeBinding.bindingInstanceName) match {
                  case Some(targetHub) => {
                    val cports = targetComponent.getProvided.toList ++ targetComponent.getRequired.toList
                    cports.find(port => port.getPortTypeRef.getName == removeBinding.portName) match {
                      case Some(port) => {
                        //LOOK for previous binding
                        context.model.getMBindings.find(mb => mb.getHub == targetHub && mb.getPort == port) match {
                          case Some(previousMB) => context.model.getMBindings.remove(previousMB); true
                          case None => logger.error("Previous binding not found => " + removeBinding.bindingInstanceName); false
                        }
                      }
                      case None => logger.error("Port not found => " + removeBinding.portName); false
                    }
                  }
                  case None => logger.error("Hub not found => " + removeBinding.bindingInstanceName); false
                }
              }
              case None => logger.error("Component not found => " + removeBinding.cid.componentInstanceName); false
            }
          }
          case None => logger.error("Node not found => " + removeBinding.cid.nodeName); false
        }
      }
      case None => logger.error("NodeName is mandatory !"); false
    }


  }

}

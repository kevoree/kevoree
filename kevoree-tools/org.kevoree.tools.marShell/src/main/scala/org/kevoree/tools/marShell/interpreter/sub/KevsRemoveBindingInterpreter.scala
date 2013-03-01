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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
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

import org.kevoree.{Channel, ComponentInstance, ContainerNode}
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.tools.marShell.ast.RemoveBindingStatment
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

case class KevsRemoveBindingInterpreter(removeBinding: RemoveBindingStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    removeBinding.cid.nodeName match {
      case Some(searchNodeName) => {
        context.model.findByPath("nodes[" + searchNodeName + "]", classOf[ContainerNode]) match {
          case targetNode:ContainerNode => {
            targetNode.findByPath("components[" + removeBinding.cid.componentInstanceName + "]", classOf[ComponentInstance]) match {
              case targetComponent:ComponentInstance => {
                context.model.findByPath("hubs[" + removeBinding.bindingInstanceName + "]", classOf[Channel]) match {
                  case targetHub:Channel => {
                    val cports = targetComponent.getProvided.toList ++ targetComponent.getRequired.toList
                    cports.find(port => port.getPortTypeRef.getName == removeBinding.portName) match {
                      case Some(port) => {
                        //LOOK for previous binding
                        port.getBindings.find(mb => mb.getHub == targetHub) match {
                          case Some(previousMB) => context.model.removeMBindings(previousMB); previousMB.setPort(null); previousMB.setHub(null); true
                          case None => {
                            context.appendInterpretationError("Could not remove binding from port '"+removeBinding.portName+"' of component '"+removeBinding.cid.componentInstanceName+"' on node '"+removeBinding.cid.nodeName+"' to channel '"+removeBinding.bindingInstanceName+"'. Binding not found.", logger)
                            //logger.error("Previous binding not found => {}", removeBinding.bindingInstanceName)
                            false
                          }
                        }
                      }
                      case None => {
                        context.appendInterpretationError("Could not remove binding from port '"+removeBinding.portName+"' of component '"+removeBinding.cid.componentInstanceName+"' on node '"+removeBinding.cid.nodeName+"' to channel '"+removeBinding.bindingInstanceName+"'. Port not found.", logger)
                        //logger.error("Port not found => {}", removeBinding.portName)
                        false
                      }
                    }
                  }
                  case null => {
                    context.appendInterpretationError("Could not remove binding from port '"+removeBinding.portName+"' of component '"+removeBinding.cid.componentInstanceName+"' on node '"+removeBinding.cid.nodeName+"' to channel '"+removeBinding.bindingInstanceName+"'. Channel not found.", logger)
                    //logger.error("Hub not found => {}",removeBinding.bindingInstanceName)
                    false
                  }
                }
              }
              case null => {
                context.appendInterpretationError("Could not remove binding from port '"+removeBinding.portName+"' of component '"+removeBinding.cid.componentInstanceName+"' on node '"+removeBinding.cid.nodeName+"' to channel '"+removeBinding.bindingInstanceName+"'. Component not found.", logger)
                //logger.error("Component not found => {}", removeBinding.cid.componentInstanceName)
                false
              }
            }
          }
          case null => {
            context.appendInterpretationError("Could not remove binding from port '"+removeBinding.portName+"' of component '"+removeBinding.cid.componentInstanceName+"' on node '"+removeBinding.cid.nodeName+"' to channel '"+removeBinding.bindingInstanceName+"'. Node not found.", logger)
            //logger.error("Node not found => {}", removeBinding.cid.nodeName)
            false
          }
        }
      }
      case None => {
        context.appendInterpretationError("Could not remove binding from port '"+removeBinding.portName+"' of component '"+removeBinding.cid.componentInstanceName+"' on node '"+removeBinding.cid.nodeName+"' to channel '"+removeBinding.bindingInstanceName+"'. Node name not specified, but mandatory.", logger)
        //logger.error("NodeName is mandatory !")
        false
      }
    }
  }

}

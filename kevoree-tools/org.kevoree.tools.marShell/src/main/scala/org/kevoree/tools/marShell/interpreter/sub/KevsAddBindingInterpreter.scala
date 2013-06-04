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

import org.kevoree.tools.marShell.ast.AddBindingStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._
import org.kevoree.{Channel, ComponentInstance, ContainerNode}
import org.kevoree.log.Log

case class KevsAddBindingInterpreter(addBinding: AddBindingStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    addBinding.cid.nodeName match {
      case Some(searchNodeName) => {
        context.model.findByPath("nodes[" + addBinding.cid.nodeName.get + "]", classOf[ContainerNode]) match {
          //.getNodes.find(n => n.getName == addBinding.cid.nodeName.get) match {
          case node: ContainerNode =>
            node.findByPath("components[" + addBinding.cid.componentInstanceName + "]", classOf[ComponentInstance]) match {
              //getComponents.find(c => c.getName == addBinding.cid.componentInstanceName) match {
              case component: ComponentInstance =>
                context.model.findByPath("hubs[" + addBinding.bindingInstanceName + "]", classOf[Channel]) match {
                  case channel: Channel => {
                    val port = component.getProvided.find(p => p.getPortTypeRef.getName == addBinding.portName) match {
                      case Some(p) => p
                      case None => {
                        component.getRequired.find(p => p.getPortTypeRef.getName == addBinding.portName) match {
                          case Some(p) => p
                          case None => null
                        }
                      }
                    }
                    if (port != null) {
                      port.getBindings.find(mb => mb.getPort.getPortTypeRef.getName == addBinding.portName
                        && mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == addBinding.cid.nodeName.get
                        && mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName == addBinding.cid.componentInstanceName
                        && mb.getHub.getName == addBinding.bindingInstanceName) match {
                        case Some(binding) => {
                          Log.warn("Binding {}.{}@{} => {} already exists",
                            addBinding.cid.componentInstanceName, addBinding.portName, addBinding.cid.nodeName.get, addBinding.bindingInstanceName)
                          true
                        }
                        case None => {
                          val newbinding = context.kevoreeFactory.createMBinding
                          newbinding.setHub(channel)
                          newbinding.setPort(port)
                          context.model.addMBindings(newbinding)
                          true
                        }
                      }
                    } else {
                      context.appendInterpretationError("Could not add binding from port '"+addBinding.portName+"' of component '"+addBinding.cid.componentInstanceName+"' on node '"+addBinding.cid.nodeName+"' to channel '"+addBinding.bindingInstanceName+"'. Port not found.")
                      false
                    }
                    /*val ports = new util.ArrayList[Port](component.getProvided.size() + component.getRequired.size())
                    ports.addAll(component.getProvided)
                    ports.addAll(component.getRequired)
                    ports.find(p => p.getPortTypeRef.getName == addBinding.portName) match {
                      case Some(port) => {
                        port.getBindings.find(mb => mb.getPort.getPortTypeRef.getName == addBinding.portName
                          && mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == addBinding.cid.nodeName.get
                          && mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName == addBinding.cid.componentInstanceName
                          && mb.getHub.getName == addBinding.bindingInstanceName) match {
                          case Some(binding) => {
                            logger.warn("Binding {}.{}@{} => {} already exists",
                              Array[AnyRef](addBinding.cid.componentInstanceName, addBinding.portName, addBinding.cid.nodeName.get, addBinding.bindingInstanceName))
                            true
                          }
                          case None => {
                            val newbinding = context.kevoreeFactory.createMBinding
                            newbinding.setHub(channel)
                            newbinding.setPort(port)
                            context.model.addMBindings(newbinding)
                            true
                          }
                        }
                      }
                      case None => logger.error("Port not found => " + addBinding.portName); false
                    }*/
                  }
                  case null => {
                    context.appendInterpretationError("Could not add binding from port '"+addBinding.portName+"' of component '"+addBinding.cid.componentInstanceName+"' on node '"+addBinding.cid.nodeName.get+"' to channel '"+addBinding.bindingInstanceName+"'. Channel not found.")
                    false
                  }
                }
              case null => {
                context.appendInterpretationError("Could not add binding from port '"+addBinding.portName+"' of component '"+addBinding.cid.componentInstanceName+"' on node '"+addBinding.cid.nodeName.get+"' to channel '"+addBinding.bindingInstanceName+"'. Component not found.")
                false
              }
            }
          case null => {
            context.appendInterpretationError("Could not add binding from port '"+addBinding.portName+"' of component '"+addBinding.cid.componentInstanceName+"' on node '"+addBinding.cid.nodeName.get+"' to channel '"+addBinding.bindingInstanceName+"'. Node not found.")
            false
          }
        }
      }
      case None => {

        context.appendInterpretationError("Could not add binding from port '"+addBinding.portName+"' of component '"+addBinding.cid.componentInstanceName+"' on node '"+addBinding.cid.nodeName.get+"' to channel '"+addBinding.bindingInstanceName+"'. Node name not specified, but mandatory.")
        false
      }
    }
  }



}

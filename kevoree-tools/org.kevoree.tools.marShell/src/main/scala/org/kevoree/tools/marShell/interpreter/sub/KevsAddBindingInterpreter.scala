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

import org.kevoree.tools.marShell.ast.AddBindingStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.slf4j.LoggerFactory
import org.kevoree.{ComponentInstance, ContainerNode, KevoreeFactory}

case class KevsAddBindingInterpreter (addBinding: AddBindingStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass);

  def interpret (context: KevsInterpreterContext): Boolean = {


    addBinding.cid.nodeName match {
      case Some(searchNodeName) => {
        context.model.getMBindings.find(
          mb =>
            mb.getPort.getPortTypeRef.getName == addBinding.portName
            && mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == addBinding.cid.nodeName.get
            && mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName == addBinding.cid.componentInstanceName
            && mb.getHub.getName == addBinding.bindingInstanceName
          ) match {
          case None => {
            context.model.getNodes.find(node => node.getName == searchNodeName) match {
              case Some(targetNode) => {
                targetNode.getComponents.find(component => component.getName == addBinding.cid.componentInstanceName) match {
                  case Some(targetComponent) => {
                    context.model.getHubs.find(hub => hub.getName == addBinding.bindingInstanceName) match {
                      case Some(targetHub) => {


                        val newbinding = KevoreeFactory.eINSTANCE.createMBinding
                        newbinding.setHub(targetHub)
                        //SEARCH TARGET PORT
                        targetComponent.getProvided.find(port => port.getPortTypeRef.getName == addBinding.portName) match {
                          case Some(targetPort) => newbinding.setPort(targetPort)
                          case None =>
                        }
                        targetComponent.getRequired.find(port => port.getPortTypeRef.getName == addBinding.portName) match {
                          case Some(targetPort) => newbinding.setPort(targetPort)
                          case None =>
                        }
                        if (newbinding.getPort != null) {
                          context.model.addMBindings(newbinding);
                          true
                        } else {
                          logger.error("Port not found => " + addBinding.portName)
                          false
                        }

                      }
                      case None => logger.error("Hub not found => " + addBinding.bindingInstanceName); false
                    }

                  }
                  case None => logger.error("Component not found => " + addBinding.cid.componentInstanceName); false
                }


              }
              case None => logger.error("Node not found => " + addBinding.cid.nodeName); false
            }
          }
          case Some(binding) => {
            logger.warn("Binding {}.{}@{} => {} already exists",
                         Array[AnyRef](addBinding.cid.componentInstanceName, addBinding.portName, addBinding.cid.nodeName.get, addBinding.bindingInstanceName))
            true
          }
        }
      }
      case None => logger.error("NodeName is mandatory !"); false
    }

  }

}

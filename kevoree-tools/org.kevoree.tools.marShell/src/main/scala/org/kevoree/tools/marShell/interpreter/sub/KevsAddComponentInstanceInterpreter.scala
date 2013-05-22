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

import org.kevoree.tools.marShell.ast.AddComponentInstanceStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.utils.Merger
import org.kevoree._
import scala.collection.JavaConversions._
import org.kevoree.log.Log

case class KevsAddComponentInstanceInterpreter(addCompo: AddComponentInstanceStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    addCompo.cid.nodeName match {
      case Some(nodeID) => {
        //SEARCH NODE
        context.model.findByPath("nodes[" + nodeID + "]", classOf[ContainerNode]) match {
          case targetNode: ContainerNode => {
            targetNode.findByPath("components[" + addCompo.cid.componentInstanceName + "]", classOf[ComponentInstance]) match {
              case previousComponent: ComponentInstance => {
                Log.warn("Component already exist with name " + previousComponent.getName)
                if (previousComponent.getTypeDefinition.getName == addCompo.typeDefinitionName) {
                  Merger.mergeDictionary(previousComponent, addCompo.props, null)
                  true
                } else {
                  context.appendInterpretationError("Could add component instance '" + addCompo.cid.componentInstanceName + "' of type '" + addCompo.typeDefinitionName + "' on node '" + nodeID + "'. A component instance already exists with the same name, but with a different type: '" + previousComponent.getTypeDefinition.getName + "'.")
                  false
                }
              }
              case null => {
                //SEARCH TYPE
                context.model.findByPath("typeDefinitions[" + addCompo.typeDefinitionName + "]", classOf[TypeDefinition]) match {
                  case typeDef: ComponentType => {
                    val componentDefinition = typeDef
                    val newcomponent = context.kevoreeFactory.createComponentInstance
                    newcomponent.setTypeDefinition(typeDef)
                    newcomponent.setName(addCompo.cid.componentInstanceName)

                    //ADD PORTS
                    for (ref <- componentDefinition.getProvided) {
                      val port: Port = context.kevoreeFactory.createPort
                      newcomponent.addProvided(port)
                      port.setPortTypeRef(ref)
                    }
                    for (ref <- componentDefinition.getRequired) {
                      val port: Port = context.kevoreeFactory.createPort
                      newcomponent.addRequired(port)
                      port.setPortTypeRef(ref)
                    }

                    //MERGE DICTIONARY
                    Merger.mergeDictionary(newcomponent, addCompo.props, null)
                    targetNode.addComponents(newcomponent)
                    true
                  }
                  case typeDef: TypeDefinition if (!typeDef.isInstanceOf[ComponentType]) => {
                    context.appendInterpretationError("Could add component instance '" + addCompo.cid.componentInstanceName + "' of type '" + addCompo.typeDefinitionName + "' on node '" + nodeID ++ "'. Type of the new channel is not a ChannelType: '" + typeDef.getClass.getName + "'.")
                    false
                  }
                  case _ => {
                    context.appendInterpretationError("Could add component instance '" + addCompo.cid.componentInstanceName + "' of type '" + addCompo.typeDefinitionName + "' on node '" + nodeID + "'. Type of the new instance not found.")
                    false
                  }
                }
              }
            }
          }
          case null => {
            context.appendInterpretationError("Could add component instance '" + addCompo.cid.componentInstanceName + "' of type '" + addCompo.typeDefinitionName + "' on node '" + nodeID + "'. Node not found.")
            false
          }
        }
      }
      case None => {
        //TODO search to solve ambiguity
        context.appendInterpretationError("Could add component instance '" + addCompo.cid.componentInstanceName + "' of type '" + addCompo.typeDefinitionName + "' on node '" + addCompo.cid.nodeName + "'. Node not found.")
        false
      }
    }
  }

}

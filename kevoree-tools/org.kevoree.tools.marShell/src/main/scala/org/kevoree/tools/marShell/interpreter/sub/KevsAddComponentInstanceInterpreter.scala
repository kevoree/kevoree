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

import org.kevoree.tools.marShell.ast.AddComponentInstanceStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.utils.Merger

import org.kevoree._
import org.slf4j.LoggerFactory

case class KevsAddComponentInstanceInterpreter(addCompo: AddComponentInstanceStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass);

  def interpret(context: KevsInterpreterContext): Boolean = {
    addCompo.cid.nodeName match {
      case Some(nodeID) => {
        //SEARCH NODE
        context.model.getNodes.find(n => n.getName == nodeID) match {
          case Some(targetNode) => {

            targetNode.getComponents.find(component => component.getName == addCompo.cid.componentInstanceName) match {

              case Some(previousComponent) => {
                logger.warn("Component already exist with name " + previousComponent.getName);
                if (previousComponent.getTypeDefinition.getName == addCompo.typeDefinitionName) {
                  Merger.mergeDictionary(previousComponent, addCompo.props)
                  true
                } else {
                  logger.error("Type != from previous created component")
                  false
                }
              }
              case None => {
                //SEARCH TYPE
                context.model.getTypeDefinitions.find(td => td.getName == addCompo.typeDefinitionName) match {
                  case Some(typeDef) if (typeDef.isInstanceOf[ComponentType]) => {
                    val componentDefinition = typeDef.asInstanceOf[ComponentType]
                    val newcomponent = KevoreeFactory.eINSTANCE.createComponentInstance
                    newcomponent.setTypeDefinition(typeDef)
                    newcomponent.setName(addCompo.cid.componentInstanceName)

                    //ADD PORTS
                    for (ref <- componentDefinition.getProvided) {
                      val port: Port = KevoreeFactory.eINSTANCE.createPort
                      newcomponent.getProvided.add(port)
                      port.setPortTypeRef(ref)
                    }
                    for (ref <- componentDefinition.getRequired) {
                      val port: Port = KevoreeFactory.eINSTANCE.createPort
                      newcomponent.getRequired.add(port)
                      port.setPortTypeRef(ref)
                    }

                    //MERGE DICTIONARY
                    Merger.mergeDictionary(newcomponent, addCompo.props)

                    targetNode.getComponents.add(newcomponent)

                  }
                  case Some(typeDef) if (!typeDef.isInstanceOf[ComponentType]) => {
                    logger.error("Type definition is not a componentType " + addCompo.typeDefinitionName);
                    false
                  }
                  case _ => {
                    logger.error("Type definition not found " + addCompo.typeDefinitionName);
                    false
                  }
                }
              }
            }
          }
          case None => {
            logger.error("Node not found " + nodeID);
            false
          }
        }
      }
      case None => {
        //TODO search to solve ambiguity
        false
      }
    }
  }

}

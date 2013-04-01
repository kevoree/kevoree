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
package org.kevoree.core.basechecker.nodechecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.kaspects.{TypeDefinitionAspect, ContainerNodeAspect}
import org.kevoree.{Channel, ContainerNode, ContainerRoot}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 30/08/11
 * Time: 16:46
 */

class NodeChecker extends CheckerService {
  val containerNodeAspect = new ContainerNodeAspect()
  val typeDefinitionAspect = new TypeDefinitionAspect()

  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: ListBuffer[CheckerViolation] = ListBuffer()
    model.getNodes.foreach {
      node => //For each Node
        val alreadyCheckedChannels: ListBuffer[Channel] = ListBuffer[Channel]()
        node.getComponents.foreach {
          component => //For each component of each node
            typeDefinitionAspect.foundRelevantDeployUnit(component.getTypeDefinition, node)
            match {
              case null => {
                val violation: CheckerViolation = new CheckerViolation
                violation.setMessage(component.getTypeDefinition.getName + " has no deploy unit for node type " +
                  node.getTypeDefinition.getName)
                violation.setTargetObjects(List(node) ++ List(component))
                violations += violation
              }
              case _ =>
            }
            // check channel fragment
            var subTempPorts = List[org.kevoree.Port]()
            subTempPorts = subTempPorts ++ component.getProvided
            subTempPorts = subTempPorts ++ component.getRequired


            subTempPorts.foreach {
              port => port.getBindings.foreach {
                mbinding => {
                  if (!alreadyCheckedChannels.contains(mbinding.getHub)) {
                    typeDefinitionAspect.foundRelevantDeployUnit(mbinding.getHub.getTypeDefinition, node)
                    match {
                      case null => {
                        val violation: CheckerViolation = new CheckerViolation
                        violation.setMessage(mbinding.getHub.getTypeDefinition.getName + " has no deploy unit for node type " + node.getTypeDefinition.getName)
                        violation.setTargetObjects(List(mbinding.getHub))
                        violations += violation
                      }
                      case _ =>
                    }
                  }
                }
              }
            }
        }
        // check groups
        containerNodeAspect.getGroups(node).foreach {
          group =>
            typeDefinitionAspect.foundRelevantDeployUnit(group.getTypeDefinition, node)
            match {
              case null => {
                val violation: CheckerViolation = new CheckerViolation
                violation.setMessage(group.getTypeDefinition.getName + " has no deploy unit for node type " + node.getTypeDefinition.getName)
                violation.setTargetObjects(List(group))
                violations += violation
              }
              case _ =>
            }
        }
        // check child nodes
        node.getHosts.foreach {
          child =>
            typeDefinitionAspect.foundRelevantDeployUnit(child.getTypeDefinition, node) match {
              case null => {
                val violation: CheckerViolation = new CheckerViolation
                violation.setMessage(child.getTypeDefinition.getName + " has no deploy unit for node type " +
                  node.getTypeDefinition.getName)
                violation.setTargetObjects(List(child))
                violations += violation

              }
              case _ =>
            }
        }
        // check node
        typeDefinitionAspect.foundRelevantDeployUnit(node.getTypeDefinition, node) match {
          case null => {
            val violation: CheckerViolation = new CheckerViolation
            violation.setMessage(node.getTypeDefinition.getName + " has no deploy unit for node type " +
              node.getTypeDefinition.getName)
            violation.setTargetObjects(List(node))
            violations += violation
            //println("fuck "+node.getTypeDefinition.getName)

          }
          case _ =>
        }
    }
    violations
  }
}
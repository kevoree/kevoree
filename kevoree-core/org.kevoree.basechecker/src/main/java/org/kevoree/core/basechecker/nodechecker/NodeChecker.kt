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

import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.*
import java.util.ArrayList

class NodeChecker : CheckerService {


  fun check(model: ContainerRoot): List<CheckerViolation> {
      var violations = ArrayList<CheckerViolation>()
      model.getNodes.forEach {
      node -> //For each Node
        val alreadyCheckedChannels: ListBuffer[Channel] = ListBuffer[Channel]()
        node.getComponents.foreach {
          component => //For each component of each node
            component.getTypeDefinition.foundRelevantDeployUnit(node)
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
            (component.getProvided ++ component.getRequired).foreach {
              port => port.getBindings.foreach {
                mbinding => {
                  if (!alreadyCheckedChannels.contains(mbinding.getHub)) {
                    mbinding.getHub.getTypeDefinition.foundRelevantDeployUnit(node)
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
        node.getGroups.foreach {
          group =>
            group.getTypeDefinition.foundRelevantDeployUnit(node)
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
            child.getTypeDefinition.foundRelevantDeployUnit(node) match {
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
        node.getTypeDefinition.foundRelevantDeployUnit(node) match {
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
    return violations
  }
}
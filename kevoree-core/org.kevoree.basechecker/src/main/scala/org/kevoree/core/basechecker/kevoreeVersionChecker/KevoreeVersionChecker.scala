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
package org.kevoree.core.basechecker.kevoreeVersionChecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree._
import collection.JavaConversions._
import collection.mutable.ListBuffer
import org.kevoree.framework.kaspects.{TypeDefinitionAspect, ContainerNodeAspect}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/04/12
 * Time: 15:21
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeVersionChecker extends CheckerService with KevoreeNodeVersion {
  val containerNodeAspect = new ContainerNodeAspect()
  val typeDefinitionAspect = new TypeDefinitionAspect()

  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new java.util.ArrayList[CheckerViolation]()

    val alreadyCheckedChannels: ListBuffer[Channel] = ListBuffer[Channel]()
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            val du = typeDefinitionAspect.foundRelevantDeployUnit(component.getTypeDefinition, node)
            if (du != null) {
              violations.addAll(check(component.getName, du, node))
            }

            var subTempList = List[Port]()
            subTempList = subTempList ++ component.getProvided
            subTempList = subTempList ++ component.getRequired

            subTempList.foreach {
              port => port.getBindings.foreach {mbinding => {
                  if (!alreadyCheckedChannels.contains(mbinding.getHub)) {
                    val du = typeDefinitionAspect.foundRelevantDeployUnit(mbinding.getHub.getTypeDefinition, node)
                    if (du != null) {
                      violations.addAll(check(mbinding.getHub.getName, du, node))
                      alreadyCheckedChannels += mbinding.getHub
                    }
                  }
                }
              }
            }
        }
        containerNodeAspect.getGroups(node).foreach {
          group =>
            val du = typeDefinitionAspect.foundRelevantDeployUnit(group.getTypeDefinition, node)
            if (du != null) {
              violations.addAll(check(group.getName, du, node))
            }
        }
    }
    violations
  }

  private def check(instanceName: String, deployUnit: DeployUnit, node: ContainerNode): java.util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new java.util.ArrayList[CheckerViolation]()
    val kevoreeNodeVersion = getKevoreeVersion(node)
    if (((deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.api")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.core")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.framework")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.kcl"))
      && deployUnit.getVersion != kevoreeNodeVersion) {
      val concreteViolation: CheckerViolation = new CheckerViolation()
      concreteViolation
        .setMessage("Component " + instanceName + " has a required deployUnit \"" + deployUnit.getGroupName + ":" + deployUnit.getUnitName + "\" which needs different version of Kevoree that the one provided (requiredVersion=" + deployUnit.getVersion +
        ",providedVersion=" + kevoreeNodeVersion)
      concreteViolation.setTargetObjects(List(node.asInstanceOf[AnyRef]))
      violations.add(concreteViolation)
      violations
    } else {
      deployUnit.getRequiredLibs.foreach {
        requiredDU =>
          violations.addAll(check(instanceName, requiredDU, node))
      }
      violations
    }
  }
}
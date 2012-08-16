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
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.core.basechecker.kevoreeVersionChecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{ContainerNode, DeployUnit, ContainerRoot}
import java.util.ArrayList
import collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/04/12
 * Time: 15:21
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeVersionChecker extends CheckerService {
  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new java.util.ArrayList[CheckerViolation]()
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            val du = component.getTypeDefinition.foundRelevantDeployUnit(node)
            if(du != null){
              violations.addAll(check(component.getName, du, node))
            }
        }
        node.getGroups.foreach {
          group =>
            val du = group.getTypeDefinition.foundRelevantDeployUnit(node)
            if(du != null){
              violations.addAll(check(group.getName, du, node))
            }
        }
        node.getChannelFragment.foreach {
          channel =>
            val du = channel.getTypeDefinition.foundRelevantDeployUnit(node)
            if(du != null){
              violations.addAll(check(channel.getName, du, node))
            }
        }
    }
    violations
  }

  private def check (instanceName: String, deployUnit: DeployUnit, node: ContainerNode): java.util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new java.util.ArrayList[CheckerViolation]()
    if (((deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.api")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.core")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.framework")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.kcl"))
      && deployUnit.getVersion != node.getKevoreeVersion) {
      val concreteViolation: CheckerViolation = new CheckerViolation()
      concreteViolation
        .setMessage("Component "+ instanceName + " has a required deployUnit \"" + deployUnit.getGroupName + ":" + deployUnit.getUnitName + "\" which needs different version of Kevoree that the one provided (requiredVersion=" + deployUnit.getVersion +
        ",providedVersion=" + node.getKevoreeVersion)
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
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
package org.kevoree.core.basechecker.bindingchecker

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 30/08/11
 * Time: 17:48
 */

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import collection.JavaConversions._
import org.kevoree.{ServicePortType, MBinding, ContainerRoot}

class BindingChecker extends CheckerService {

  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    violations = violations ++ checkHubBindingsHomogeneity(model)

    violations
  }

  private def checkHubBindingsHomogeneity(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()

    model.getHubs.foreach {
      hub =>
        val bindingsOnHub = model.getMBindings.filter(mb => mb.getHub.equals(hub))
        var synchBindings: List[MBinding] = List()
        var asynchBindings: List[MBinding] = List()
        bindingsOnHub.foreach {
          binding =>
            if (binding.getPort.getPortTypeRef.getRef.isInstanceOf[ServicePortType]) {
              synchBindings = synchBindings ++ List(binding)
            } else {
              asynchBindings = asynchBindings ++ List(binding)
            }
        }
        if (!synchBindings.isEmpty && !asynchBindings.isEmpty) {
          val violation = new CheckerViolation
          violation.setMessage("Ports of both Service and Message kinds are connected to the same hub : " + hub.getName)
          if (synchBindings.size > asynchBindings.size) {
            violation.setTargetObjects(asynchBindings ++ List(hub))
          } else if (synchBindings.size < asynchBindings.size) {
            violation.setTargetObjects(synchBindings ++ List(hub))
          } else {
            violation.setTargetObjects(asynchBindings ++ synchBindings ++ List(hub))
          }

          violations = violations ++ List(violation)
        }
    }
    violations
  }
}
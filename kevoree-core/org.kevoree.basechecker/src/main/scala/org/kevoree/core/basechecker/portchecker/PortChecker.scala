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
package org.kevoree.core.basechecker.portchecker

import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._
import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.{ComponentInstance, ContainerRoot}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/06/11
 * Time: 09:23
 */
class PortChecker extends CheckerService {
  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            component.getRequired.foreach {
              port =>
                if (!port.getPortTypeRef.getOptional && !port.isBind) {
                  val concreteViolation: CheckerViolation = new CheckerViolation()
                  concreteViolation
                    .setMessage("Required port (" + port.eContainer().asInstanceOf[ComponentInstance].getName + "." +
                    port.getPortTypeRef.getName + ") is not bind")
                  concreteViolation.setTargetObjects(List(port))
                  violations = violations ++ List(concreteViolation)
                }
            }
        }
    }
    violations
  }
}
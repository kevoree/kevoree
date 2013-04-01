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
package org.kevoree.core.basechecker.portchecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import collection.JavaConversions._
import org.kevoree.framework.kaspects.PortAspect
import org.kevoree.{ComponentInstance, ContainerRoot}
import java.util

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/06/11
 * Time: 09:23
 */
class PortChecker extends CheckerService {

  private val portAspect = new PortAspect()

  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    violations = violations ++ portCheckOnInstance(model)
    violations
  }

  def portCheckOnInstance(model: ContainerRoot): java.util.List[CheckerViolation] = {

    var violations: List[CheckerViolation] = List()
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            component.getRequired.foreach {
              port =>
                if (!port.getPortTypeRef.getOptional && !portAspect.isBound(port)) {
                  val concreteViolation: CheckerViolation = new CheckerViolation()
                  concreteViolation.setMessage("Required port (" + port.eContainer.asInstanceOf[ComponentInstance].getName + "." +port.getPortTypeRef.getName + ") is not bound")
                  concreteViolation.setTargetObjects(List(port.eContainer))
                  violations = violations ++ List(concreteViolation)
                }

                if(port.getBindings.size > 1){
                  //TWICE BINDING !!!
                  val concreteViolation: CheckerViolation = new CheckerViolation()
                  concreteViolation.setMessage("Required port (" + port.eContainer.asInstanceOf[ComponentInstance].getName + "." +port.getPortTypeRef.getName + ") is bound multiple times !")
                  concreteViolation.setTargetObjects(List(port.eContainer))
                  concreteViolation.setTargetObjects(List(port))
                  concreteViolation.setTargetObjects(List(port.getBindings))
                  violations = violations ++ List(concreteViolation)
                }
            }
        }
    }
    violations
  }

}
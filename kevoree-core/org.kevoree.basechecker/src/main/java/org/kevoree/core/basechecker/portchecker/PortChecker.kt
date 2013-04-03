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
package org.kevoree.core.basechecker.portchecker

import java.util.ArrayList
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.container.KMFContainer
import org.kevoree.framework.kaspects.PortAspect

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/06/11
 * Time: 09:23
 */
class PortChecker: CheckerService {

    private val portAspect = PortAspect()

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation>? {
        val violations = ArrayList<CheckerViolation>()
        if (model != null) {
            violations.addAll(portCheckOnInstance(model))
        }
        return violations
    }

    fun portCheckOnInstance(model: ContainerRoot): List<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        for (node in model.getNodes()) {
            for (component in node.getComponents()) {
                for (port in component.getRequired()) {
                    if (!port.getPortTypeRef()!!.getOptional() && !portAspect.isBound(port)) {
                        val concreteViolation: CheckerViolation = CheckerViolation()
                        concreteViolation.setMessage("Required port (" + (port.eContainer() as ComponentInstance).getName() + "." + port.getPortTypeRef()!!.getName() + ") is not bound")
                        val targetObjects = ArrayList<KMFContainer>()
                        targetObjects.add(port.eContainer()!!)
                        concreteViolation.setTargetObjects(targetObjects)
                        violations.add(concreteViolation)
                    }

                    if(port.getBindings().size() > 1){
                        //TWICE BINDING !!!
                        val concreteViolation: CheckerViolation = CheckerViolation()
                        concreteViolation.setMessage("Required port (" + (port.eContainer() as ComponentInstance).getName() + "." + port.getPortTypeRef()!!.getName() + ") is bound multiple times !")
                        val targetObjects = ArrayList<KMFContainer>()
                        targetObjects.add(port.eContainer()!!)
                        concreteViolation.setTargetObjects(targetObjects)
                        violations.add(concreteViolation)
                    }
                }
            }
        }
        return violations
    }

}
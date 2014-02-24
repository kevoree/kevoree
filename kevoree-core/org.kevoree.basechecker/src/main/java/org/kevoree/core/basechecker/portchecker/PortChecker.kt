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
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.api.service.core.checker.CheckerContext

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/06/11
 * Time: 09:23
 */
class PortChecker: CheckerService {
    override fun check(element: KMFContainer?, context : CheckerContext?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (element != null && element is ComponentInstance) {
            portCheckOnInstance(element, violations)
        }
        return violations;
    }

    private fun portCheckOnInstance(component : ComponentInstance, violations : MutableList<CheckerViolation>) {
        for (port in component.required) {
            if (!port.portTypeRef!!.optional!! && port.bindings.isEmpty()) {
                val concreteViolation: CheckerViolation = CheckerViolation()
                concreteViolation.setMessage("Required port (" + (port.eContainer() as ComponentInstance).name + "." + port.portTypeRef!!.name + ") is not bound")
                val targetObjects = ArrayList<String>()
                targetObjects.add(port.eContainer()!!.path()!!)
                concreteViolation.setTargetObjects(targetObjects)
                violations.add(concreteViolation)
            }
            /*
            if(port.bindings.size() > 1){
                //TWICE BINDING !!!
                val concreteViolation: CheckerViolation = CheckerViolation()
                concreteViolation.setMessage("Required port (" + (port.eContainer() as ComponentInstance).name + "." + port.portTypeRef!!.name + ") is bound multiple times !")
                val targetObjects = ArrayList<String>()
                targetObjects.add(port.eContainer()!!.path()!!)
                concreteViolation.setTargetObjects(targetObjects)
                violations.add(concreteViolation)
            }*/
        }
    }

}
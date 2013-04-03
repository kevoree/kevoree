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

package org.kevoree.core.basechecker.cyclechecker

import java.util.ArrayList
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.MBinding
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.framework.kaspects.ComponentInstanceAspect

class ComponentCycleChecker: CheckerService {

    private val componentInstanceAspect = ComponentInstanceAspect()

    override fun check (model: ContainerRoot?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (model != null) {
            for (node in model.getNodes()) {
                val graph = KevoreeComponentDirectedGraph(model, node.getName())

                for (violation in CheckCycle(graph).check()) {
                    val concreteViolation = CheckerViolation()
                    concreteViolation.setMessage(violation.getMessage())
                    var bindings = ArrayList<MBinding>()
                    for (instance in violation.getTargetObjects()!!.toList()) {
                        if (instance is ComponentInstance) {
                            val componentInstance = instance as ComponentInstance
                            for (binding in componentInstanceAspect.getRelatedBindings(componentInstance)) {
                                if (violation.getTargetObjects()!!.contains(binding.getHub())) {
                                    bindings.add(binding)
                                }
                            }
                        }
                    }
                    concreteViolation.setTargetObjects(bindings)
                    violations.add(concreteViolation)
                }
            }
        }
        return violations
    }
}

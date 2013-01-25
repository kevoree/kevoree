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

package org.kevoree.core.basechecker.bindingchecker

import org.kevoree.api.service.core.checker.*
import org.kevoree.*
import org.slf4j.LoggerFactory
import java.util.ArrayList

class BindingChecker: CheckerService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    public override fun check(val model: ContainerRoot?): MutableList<CheckerViolation?>? {
        return checkHubBindingsHomogeneity(model)
    }

    private fun checkHubBindingsHomogeneity(model: ContainerRoot?): MutableList<CheckerViolation?> {
        var violations = ArrayList<CheckerViolation?>()
        model?.getHubs()?.forEach { hub ->
            val synchBindings = ArrayList<MBinding>()
            val asynchBindings = ArrayList<MBinding>()
            hub.getBindings().forEach { binding ->
                if (binding.getPort()?.getPortTypeRef()?.getRef() is ServicePortType) {
                    synchBindings.add(binding)
                } else {
                    asynchBindings.add(binding)
                }
            }
            if (!synchBindings.isEmpty() && !asynchBindings.isEmpty()) {
                val violation = CheckerViolation()
                var violationObj = ArrayList<Any?>()
                violation.setMessage("Ports of both Service and Message kinds are connected to the same hub : " + hub.getName())
                if (synchBindings.size > asynchBindings.size) {
                    violationObj.addAll(asynchBindings)
                    violationObj.add(hub)
                    violation.setTargetObjects( violationObj)
                } else if (synchBindings.size < asynchBindings.size) {
                    violationObj.addAll(synchBindings)
                    violationObj.add(hub)
                    violation.setTargetObjects(violationObj)
                } else {
                    violationObj.addAll(synchBindings)
                    violationObj.addAll(asynchBindings)
                    violationObj.add(hub)
                    violation.setTargetObjects(violationObj)
                }
                violations.add(violation)
            }
        }
        return violations
    }
}
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

import java.util.ArrayList
import org.kevoree.ContainerRoot
import org.kevoree.MBinding
import org.kevoree.ServicePortType
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.container.KMFContainer
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 30/08/11
 * Time: 17:48
 */
class BindingChecker: CheckerService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
        if (model != null) {
            return checkHubBindingsHomogeneity(model)
        } else {
            return ArrayList<CheckerViolation>()
        }
    }

    private fun checkHubBindingsHomogeneity(model: ContainerRoot): MutableList<CheckerViolation> {
        var violations = ArrayList<CheckerViolation>()
        for (hub in model.getHubs()) {
            val synchBindings = ArrayList<MBinding>()
            val asynchBindings = ArrayList<MBinding>()
            for (binding in hub.getBindings()) {
                if (binding.getPort()!!.getPortTypeRef()!!.getRef() is ServicePortType) {
                    synchBindings.add(binding)
                } else {
                    asynchBindings.add(binding)
                }
            }
            if (!synchBindings.isEmpty() && !asynchBindings.isEmpty()) {
                val violation = CheckerViolation()
                violation.setMessage("Ports of both Service and Message kinds are connected to the same hub : " + hub.getName())
                if (synchBindings.size > asynchBindings.size) {
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(hub)
                    targetObjects.addAll(asynchBindings)
                    violation.setTargetObjects(targetObjects)
                } else if (synchBindings.size < asynchBindings.size) {
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(hub)
                    targetObjects.addAll(synchBindings)
                    violation.setTargetObjects(targetObjects)
                } else {
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(hub)
                    targetObjects.addAll(synchBindings)
                    targetObjects.addAll(asynchBindings)
                    violation.setTargetObjects(targetObjects)
                }

                violations.add(violation)
            }
        }
        return violations
    }
}